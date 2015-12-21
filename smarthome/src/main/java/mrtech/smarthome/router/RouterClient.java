package mrtech.smarthome.router;

import android.util.Log;

import com.stream.NewAllStreamParser;

import mrtech.smarthome.ipc.IPCManager;
import mrtech.smarthome.ipc.IPCamera;
import mrtech.smarthome.router.Models.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidObjectException;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLSocket;

import mrtech.smarthome.rpc.Messages;
import mrtech.smarthome.rpc.Messages.Request.RequestType;
import mrtech.smarthome.util.NetUtil;
import mrtech.smarthome.util.NumberUtil;
import mrtech.smarthome.util.RequestUtil;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Action2;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

/**
 * Created by sphynx on 2015/12/2.
 */
class RouterClient implements RouterSession {
    private static void trace(String msg) {
        Log.e(RouterClient.class.getName(), msg);
    }

    private final IPCManager mIPCManager;
    private final Router mRouter;
    private final RouterManager mManager;
    private final HashMap<Integer, Messages.Request> mSubscribeMap;
    private final ConcurrentHashMap<Integer, Messages.Response> mResponseMap;
    private final String mSN;
    private final PublishSubject<Router> subjectRouterStatusChanged = PublishSubject.create();
    private final PublishSubject<Messages.Callback> subjectCallback = PublishSubject.create();
    private final ArrayList<Messages.Event.EventType> mEventTypes;
    private final ConcurrentHashMap<Messages.Request, Action1<Messages.Response>> mPostQueue;
    private Thread mPostTask;
    private boolean invalidSN;
    private boolean authenticated;
    private boolean initialized;
    private int mP2PHandle;
    private int port;
    private SocketListeningTask readSocketTask;
    private String p2pSN;
    private String apiKey;
    private RouterStatus routerStatus;
    private SSLSocket socket;
    private rx.Observable<Messages.Response> subjectResponse;
    private rx.Observable<Messages.Event> subjectEvent;
    private Messages.GetSystemConfigurationResponse systemConfigurationResponse;

    private RouterCacheProvider routerCacheProvider;

    public RouterClient(Router router, int p2pHandle) {
        mManager = RouterManager.getInstance();
        mSN = router.getSN();
        mP2PHandle = p2pHandle;
        mRouter = router;
        mSubscribeMap = new HashMap<>();
        mResponseMap = new ConcurrentHashMap<>();
        mEventTypes = new ArrayList<>();
        mIPCManager = IPCManager.createNewManager();
        mPostQueue = new ConcurrentHashMap<>();
        setRouterStatus(RouterStatus.INITIAL);
        subjectResponse = subjectCallback.filter(new Func1<Messages.Callback, Boolean>() {
            @Override
            public Boolean call(Messages.Callback callback) {
                return callback.getType() == Messages.Callback.CallbackType.RESPONSE;
            }
        }).map(new Func1<Messages.Callback, Messages.Response>() {
            @Override
            public Messages.Response call(Messages.Callback callback) {
                return callback.getExtension(Messages.Response.callback);
            }
        }).onErrorResumeNext(new Func1<Throwable, Observable<? extends Messages.Response>>() {
            @Override
            public Observable<? extends Messages.Response> call(Throwable throwable) {
                return PublishSubject.create();
            }
        });
        subjectEvent = subjectCallback.filter(new Func1<Messages.Callback, Boolean>() {
            @Override
            public Boolean call(Messages.Callback callback) {
                return callback.getType() == Messages.Callback.CallbackType.EVENT;
            }
        }).map(new Func1<Messages.Callback, Messages.Event>() {
            @Override
            public Messages.Event call(Messages.Callback callback) {
                return callback.getExtension(Messages.Event.callback);
            }
        }).onErrorResumeNext(new Func1<Throwable, Observable<? extends Messages.Event>>() {
            @Override
            public Observable<? extends Messages.Event> call(Throwable throwable) {
                return PublishSubject.create();
            }
        });
        subjectResponse.subscribe(new Action1<Messages.Response>() {
            @Override
            public void call(Messages.Response response) {
                int requestId = response.getRequestId();
                if (mSubscribeMap.containsKey(requestId))
                    mResponseMap.put(requestId, response);
            }
        });
    }

    @Override
    public String toString() {
        return "RouterClient:" + mSN;
    }

    private boolean decodeSN() {
        if (p2pSN == null && apiKey == null) {
            try {
                setRouterStatus(RouterStatus.SN_DECODING);
                trace("decoding sn:" + mSN);
                final String code = NumberUtil.decodeQRCode(mSN);
                trace("decoded sn:" + mSN + " -> " + code);
                final String[] strings = code.split("@");
                if (strings.length == 2) {
                    apiKey = strings[0];
                    p2pSN = strings[1];
                    invalidSN = false;
                    setRouterStatus(RouterStatus.SN_DECODED);
                    return true;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        trace("decoded sn:" + this + "failed...");
        invalidSN = true;
        setRouterStatus(RouterStatus.SN_INVALID);
        return false;
    }

    public void init() {
        if (initialized) return;
        initialized = true;
        mIPCManager.removeAll();
        setRouterStatus(RouterStatus.INITIALIZED);
        mPostTask = new Thread(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setName(RouterClient.this + "RouterPostTask");
                trace(RouterClient.this + " start post task...");
                do {
                    if (mPostQueue != null && mPostQueue.size() > 0 && isAuthenticated()) {
                        for (final Messages.Request request : getRequestQueue()) {
                            try {
                                trace("post reg event request!!!");
                                final Messages.Response response = postRequest(request);
                                trace("post reg event success:" + response);
                                mPostQueue.remove(request).call(response);
                            } catch (TimeoutException e) {
                                trace("request post failed:" + request);
                                continue;
                            }
                        }
                        continue;
                    }
                    try {
                        Thread.sleep(1000 * 3600);
                    } catch (InterruptedException e) {
                        //线程醒来。进入下一轮循环。
                    }
                } while (initialized);
            }
        });
        mPostTask.start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setName("checkRouterStatus:" + this);
                decodeSN();
                do {
                    int delay = 0;
                    try {
                        delay = checkRouterStatus();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (delay < 0) {
                                destroy();
                                break;
                            }
                            Thread.sleep(delay);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } while (initialized);
            }
        }).start();
    }


    //====================================================================================
    private boolean addPort(int delay) {
        synchronized (mManager) {
            try {
                removePort();
                trace(this + " adding port...");
                setRouterStatus(RouterStatus.P2P_CONNECTING);
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int tmp = NewAllStreamParser.DNPAddPort(mP2PHandle, p2pSN);
                        trace(RouterClient.this + "return port:" + tmp);
                        if (tmp > 0) {
                            if (port > 0) {
                                //cancel result
                                NewAllStreamParser.DNPDelPort(mP2PHandle, tmp);
                            } else {
                                port = tmp;
                            }
                        }
                    }
                });
                thread.start();
                thread.join(delay);
            } catch (Exception ex) {
                ex.printStackTrace();
                port = 0;
                trace(this + "add port failed..");
            } finally {
                setRouterStatus(isPortValid() ? RouterStatus.P2P_CONNECTED : RouterStatus.P2P_DISCONNECTED);
                trace(this + " p2p port:" + port);
            }
            return port != 0;
        }
    }

    private void removePort() {
        if (port == 0) return;
        disconnect();
        try {
            trace(this + " remove port...");
            NewAllStreamParser.DNPDelPort(mP2PHandle, port);
            port = 0;
            setRouterStatus(RouterStatus.P2P_DISCONNECTED);
        } catch (Exception e) {
            e.printStackTrace();
            trace(this + " remove port failed...");
        }
    }

    //====================================================================================
    private void disconnect() {
        if (socket != null) {
            if (!socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    trace(this + "socket close failed..");
                }
            }
            readSocketTask.cancel();
            socket = null;
            authenticated = false;
            setRouterStatus(RouterStatus.ROUTER_DISCONNECTED);
        }
    }

    private boolean connect() {
        disconnect();
        trace(this + " creating ssl socket.");
        try {
            setRouterStatus(RouterStatus.ROUTER_CONNECTING);
            SSLSocket tempSocket = NetUtil.createSocket("localhost", port);
            if (tempSocket != null && tempSocket.getSession().isValid()) {
                socket = tempSocket;
                readSocketTask = new SocketListeningTask();
                new Thread(readSocketTask).start();
            }
            setRouterStatus(isConnected() ? RouterStatus.ROUTER_CONNECTED : RouterStatus.ROUTER_DISCONNECTED);
        } catch (SocketException e) {
            trace(this + "SocketException.!!!!removePort " + e.getMessage());
            removePort();
        } catch (Exception e) {
            trace(this + "create ssl socket error." + e.getMessage());
            e.printStackTrace();
            disconnect();
        }
        return isConnected();
    }

    //====================================================================================
    private boolean doAuth() {
        if (!isConnected()) return false;
        try {
            trace(RouterClient.this + " authentication!!!");
            Messages.Response resp = postRequest(RequestUtil.getAuthRequest(apiKey));
            final Messages.Response.ErrorCode code = resp.getErrorCode();
            authenticated =
                    code == Messages.Response.ErrorCode.SUCCESS ||
                            code == Messages.Response.ErrorCode.ALREADY_AUTHENTICATED;
            trace(RouterClient.this + " authentication result :" + code);
        } catch (TimeoutException e) {
            e.printStackTrace();
            trace("auth time out");
        } finally {
            setRouterStatus(authenticated ? RouterStatus.API_AUTH_SUCCESS : RouterStatus.API_UNAUTHORIZED);
        }
        return authenticated;
    }

    private void keepAlive() {
        if (!isAuthenticated()) return;
        trace(RouterClient.this + " keep alive..");
        try {
            postRequest(RequestUtil.getKeepAliveRequest());
        } catch (TimeoutException e) {
//            e.printStackTrace();
            trace("keep alive failed!!");
            disconnect();
        }
    }

    public void destroy() {
        if (!initialized) return;
        initialized = false;
        mPostTask.interrupt();
        mIPCManager.removeAll();
//        subjectRouterStatusChanged.onCompleted();
        new Thread(new Runnable() {
            @Override
            public void run() {
                removePort();
            }
        }).start();
        setRouterStatus(RouterStatus.INITIAL);
    }

    @Override
    public void postRequestAsync(Messages.Request request, Action2<Messages.Response, Throwable> callback) {
        postRequestAsync(request, callback, ROUTER_REQUEST_TIMEOUT);
    }

    @Override
    public void postRequestAsync(Messages.Request request, Action2<Messages.Response, Throwable> callback, boolean cache) {
        postRequestAsync(request, callback, ROUTER_REQUEST_TIMEOUT, cache);
    }

    @Override
    public void postRequestAsync(final Messages.Request request, final Action2<Messages.Response, Throwable> callback, int timeout) {
        postRequestAsync(request, callback, timeout, false);
    }

    @Override
    public void postRequestAsync(final Messages.Request request, final Action2<Messages.Response, Throwable> callback, int timeout, boolean cache) {
        if (request != null && isConnected()) {
            if (callback != null) {
                if (cache && routerCacheProvider != null) {
                    final Messages.Response response = routerCacheProvider.getResponseCache(request.getType());
                    if (response != null) {
                        callback.call(response, null);
                        return;
                    }
                }
                subjectResponse.timeout(timeout, TimeUnit.MILLISECONDS).doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        callback.call(null, throwable);
                    }
                }).first(new Func1<Messages.Response, Boolean>() {
                    @Override
                    public Boolean call(Messages.Response resp) {
                        return resp.getRequestId() == request.getRequestId();
                    }
                }).subscribe(new Action1<Messages.Response>() {
                    @Override
                    public void call(Messages.Response response) {
                        callback.call(response, null);
                    }
                });
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    OutputStream os = null;
                    try {
                        os = socket.getOutputStream();
                        int requestLength = request.getSerializedSize();
                        byte heightLevelBit = (byte) ((requestLength & 0xff00) >> 8);
                        byte lowLevelBit = (byte) (requestLength & 0x00ff);
                        os.write(heightLevelBit);
                        os.write(lowLevelBit);
                        request.writeTo(os);
                    } catch (IOException e) {
                        disconnect();
                        if (callback != null) {
                            callback.call(null, e);
                        }
                        trace(RouterClient.this + " socket IO exception ,socket will be reset..");
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            os.close();
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                    }
                }
            }).start();
        } else {
            if (callback != null)
                callback.call(null, new InvalidObjectException("invalid connection..."));
            if (socket == null) {
                trace("socket is null");
            } else {
                disconnect();
            }
        }
    }

    @Override
    public Messages.Response postRequest(Messages.Request request, int timeout) throws TimeoutException {
        return postRequest(request, timeout, false);
    }

    @Override
    public Messages.Response postRequest(Messages.Request request, int timeout, boolean cache) throws TimeoutException {
        if (request == null) return null;
        if (cache && routerCacheProvider != null) {
            final Messages.Response response = routerCacheProvider.getResponseCache(request.getType());
            if (response != null) return response;
        }
        if (!isConnected()) return null;
        mSubscribeMap.put(request.getRequestId(), request);
        postRequestAsync(request, null, timeout, cache);
        try {
            int requestId = request.getRequestId();
            int delay = 100;
            int retryTimes = timeout / delay;
            do {
                if (mResponseMap.containsKey(requestId)) {
                    return mResponseMap.remove(requestId);
                }
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (retryTimes-- > 0);
            throw new TimeoutException(this + "request " + requestId + " timeout...");
        } finally {
            mSubscribeMap.remove(request.getRequestId());
        }
    }

    @Override
    public Messages.Response postRequest(Messages.Request request) throws TimeoutException {
        return postRequest(request, ROUTER_REQUEST_TIMEOUT);
    }

    @Override
    public Messages.Response postRequest(Messages.Request request, boolean cache) throws TimeoutException {
        return postRequest(request, ROUTER_REQUEST_TIMEOUT, cache);
    }

    @Override
    public Messages.GetSystemConfigurationResponse getRouterConfiguration(boolean refreshCache) {
        if (systemConfigurationResponse == null) {
            try {
                systemConfigurationResponse = postRequest(RequestUtil.getSysConfig()).getExtension(Messages.GetSystemConfigurationResponse.response);
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
        }
        return systemConfigurationResponse;
    }

    @Override
    public boolean isConnected() {
        return socket != null && socket.getSession().isValid();
    }

    @Override
    public boolean isSNValid() {
        return !invalidSN;
    }

    @Override
    public boolean isPortValid() {
        return port != 0;
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public RouterStatus getRouterStatus() {
        return routerStatus;
    }

    @Override
    public Collection<Messages.Request> getRequestQueue() {
        return mPostQueue.keySet();
    }

    @Override
    public void cancelRequestFromQueue(Messages.Request request) {
        if (mPostQueue.containsKey(request)) {
            mPostQueue.remove(request);
        }
    }

    @Override
    public void postRequestToQueue(Messages.Request request, Action1<Messages.Response> callback) {
        if (!mPostQueue.containsKey(request)) {
            trace("put to queue:" + request);
            mPostQueue.put(request, callback);
        }
        mPostTask.interrupt();
    }

    @Override
    public IPCManager getIPCManager() {
        return mIPCManager;
    }

    @Override
    public void reloadIPCAsync(boolean cache, final Action1<Throwable> exception) {
        try {
            postRequestAsync(RequestUtil.getDevices(mrtech.smarthome.rpc.Models.DeviceQuery.newBuilder()
                    .setType(mrtech.smarthome.rpc.Models.DeviceType.DEVICE_TYPE_CAMERA)
                    .setPage(0)
                    .setPageSize(100)
                    .build()), new Action2<Messages.Response, Throwable>() {
                @Override
                public void call(Messages.Response response, Throwable throwable) {
                    if (response != null) {
                        final List<mrtech.smarthome.rpc.Models.Device> result = response.getExtension(Messages.QueryDeviceResponse.response).getResultsList();
                        if (result != null && result.size() > 0) {
                            mIPCManager.removeAll();
                            for (mrtech.smarthome.rpc.Models.Device device : result) {
                                final mrtech.smarthome.rpc.Models.CameraDevice cameraDevice = device.getExtension(mrtech.smarthome.rpc.Models.CameraDevice.detail);
                                mIPCManager.addCamera(new IPCamera(device, cameraDevice.getDeviceid(), cameraDevice.getUser(), cameraDevice.getPassword()));
                            }
                        } else {
                            throwable = new NoSuchElementException("未添加摄像头");
                        }
                    }
                    if (exception != null) exception.call(throwable);
                }
            }, cache);
        } catch (Exception ex) {
            if (exception != null) exception.call(ex);
        }
    }

    @Override
    public Subscription subscribeEvent(Messages.Event.EventType eventType, Action1<Messages.Event> eventAction) {
//        EventType.DISCONNECT
//        EventType.SYS_CONFIG_CHANGED
//        EventType.EZMODE_STATUS_CHANGED
//        EventType.PERMIT_JOIN_STATUS_CHANGED
//        EventType.NEW_TIMELINE
//        EventType.ON_OFF_STATE_CHANGED
//        EventType.SCENE_CHANGED
//        EventType.PPPOE_STATE_CHANGED
        if (!mEventTypes.contains(eventType)) {
            trace("subscribe event:" + eventType);
            mEventTypes.add(eventType);
            postEvents();
        }
        return eventAction == null ? null : getEventObservable(eventType).subscribe(eventAction);
    }

    private void postEvents() {
        for (Messages.Request request : getRequestQueue()) {
            if (request.getType() == Messages.Request.RequestType.SET_EVENTS) {
                mPostTask.interrupt();
                return;
            }
        }
        postRequestToQueue(RequestUtil.setEvent(mEventTypes.toArray(new Messages.Event.EventType[0])), new Action1<Messages.Response>() {
            @Override
            public void call(Messages.Response response) {
                if (response.getErrorCode() != Messages.Response.ErrorCode.SUCCESS) postEvents();
            }
        });
    }

    @Override
    public void unsubscribeEvent(Messages.Event.EventType eventType) {
        if (mEventTypes.contains(eventType)) {
            trace("unsubscribe event:" + eventType);
            mEventTypes.remove(eventType);
            postEvents();
        }
    }

    @Override
    public List<Messages.Event.EventType> getEventTypes() {
        return mEventTypes;
    }

    @Override
    public Subscription subscribeRouterStatusChanged(Action1<Router> callback) {
        return subjectRouterStatusChanged.subscribe(callback);
    }

    private void setRouterStatus(RouterStatus routerStatus) {
        this.routerStatus = routerStatus;
        if (isAuthenticated()) {
            postEvents();
        }
        subjectRouterStatusChanged.onNext(mRouter);
    }

    private Messages.Callback pullCallback(final SSLSocket sslSocket) throws IOException {
        InputStream in = null;
        in = sslSocket.getInputStream();
        byte[] prefix = new byte[2];
        int received = 0;
        in.read(prefix);
        if (prefix[0] == 0 && prefix[1] == 0)
            throw new IOException("invalid package header..");
        else {
            int length = ((prefix[0] & 0xff) << 8) + (prefix[1] & 0xff);
            received = 0;
            byte[] buffer = new byte[length];
            while (received < length) {
                received += in.read(buffer, received, length - received);
            }
            return Messages.Callback.parseFrom(buffer, RouterManager.registry);
        }
    }

    private int checkRouterStatus() {
        try {
            if (!isSNValid()) return -1;
            if (!isPortValid())
                if (!addPort(ROUTER_ADD_PORT_DELAY)) return ROUTER_ADD_PORT_DELAY;
            if (!isConnected())
                if (!connect()) return ROUTER_RECONNECTION_DELAY;
            if (!isAuthenticated())
                if (!doAuth()) return ROUTER_AUTH_DELAY;
            keepAlive();
        } catch (Exception ex) {
            ex.printStackTrace();
            trace(this + " check status failed.." + ex.getMessage());
        }
        return ROUTER_KEEP_ALIVE_DELAY;
    }

    private class SocketListeningTask implements Runnable {
        private boolean cancel;

        public boolean isCancelled() {
            return cancel;
        }

        public void cancel() {
            cancel = true;
        }

        @Override
        public void run() {
            final SSLSocket sslSocket = socket;
            while (!isCancelled()) {
                if (isConnected()) {
                    Messages.Callback callback = null;
                    try {
                        callback = pullCallback(sslSocket);
                        if (callback == null) continue;
                        trace(RouterClient.this + " callback packet :" + callback);
                        subjectCallback.onNext(callback);
                    } catch (IOException e) {
                        e.printStackTrace();
                        trace(RouterClient.this + " read stream error");
                        disconnect();
                        continue;
                    }
                } else {
                    try {
                        trace("waiting for connective...");
                        Thread.sleep(ROUTER_READ_INTERVAL);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    Observable<Messages.Event> getEventObservable(final Messages.Event.EventType eventType) {
        return subjectEvent.filter(new Func1<Messages.Event, Boolean>() {
            @Override
            public Boolean call(Messages.Event event) {
                return event.getType() == eventType;
            }
        });
    }

    Observable<Messages.Event> getEventObservable() {
        return subjectEvent;
    }

    @Override
    public void alarmTest() {
        subjectCallback.onNext(Messages.Callback.newBuilder()
                .setType(Messages.Callback.CallbackType.EVENT)
                .setExtension(Messages.Event.callback, Messages.Event.newBuilder()
                        .setType(Messages.Event.EventType.NEW_TIMELINE)
                        .setExtension(Messages.NewTimelineEvent.event, Messages.NewTimelineEvent.newBuilder()
                                .setTimeline(mrtech.smarthome.rpc.Models.Timeline.newBuilder()
                                        .setLevel(mrtech.smarthome.rpc.Models.TimelineLevel.TIMELINE_LEVEL_ALARM)
                                        .setTimestamp(123l)
                                        .setParameter("{'name':'test alarm'}")
                                        .build())
                                .build())
                        .build())
                .build());
    }
}
