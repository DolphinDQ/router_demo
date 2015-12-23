package mrtech.smarthome.router;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLSocket;

import mrtech.smarthome.rpc.Messages;
import mrtech.smarthome.util.RequestUtil;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Action2;
import rx.functions.Func1;
import mrtech.smarthome.router.Models.*;
import rx.subjects.PublishSubject;

/**
 * Created by sphynx on 2015/12/22.
 */
class RouterCommunicationManager implements CommunicationManager {
    private static void trace(String msg) {
        Log.e(RouterClient.class.getName(), msg);
    }

    @Override
    protected void finalize() throws Throwable {
        subscribeRouterStatusChanged.unsubscribe();
        super.finalize();
    }

    private final Subscription subscribeRouterStatusChanged;
    private final HashMap<Integer, Messages.Request> mSubscribeMap;
    private final ConcurrentHashMap<Messages.Request, Action1<Messages.Response>> mPostQueue;
    private final RouterClient mClient;
    private final RouterCacheProvider routerCacheProvider;
    private final PublishSubject<Messages.Callback> subjectCallback = PublishSubject.create();
    private final rx.Observable<Messages.Response> subjectResponse;
    private final ArrayList<Messages.Event.EventType> mEventTypes;
    private final ConcurrentHashMap<Integer, Messages.Response> mResponseMap;
    private boolean destroyed;
    private SSLSocket socket;
    private rx.Observable<Messages.Event> subjectEvent;
    private Thread mPostTask;
    private Thread mReadTask;

    public RouterCommunicationManager(RouterClient client) {
        mPostQueue = new ConcurrentHashMap<>();
        mClient = client;
        routerCacheProvider = null;
        mResponseMap = new ConcurrentHashMap<>();
        mEventTypes = new ArrayList<>();
        mSubscribeMap = new HashMap<>();
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
        subscribeRouterStatusChanged = mClient.subscribeRouterStatusChanged(new Action1<Router>() {
            @Override
            public void call(Router router) {
                if (mClient.isAuthenticated()) postEvents();
            }
        });
    }

    public void init(SSLSocket socket) {
        destroy();
        destroyed = false;
        this.socket = socket;
        mPostTask = new Thread(new RequestQueuePostTask());
        mPostTask.start();
        mReadTask = new Thread(new SocketListeningTask());
        mReadTask.start();
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
        if (request != null && mClient.isConnected()) {
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
                        if (callback != null) {
                            callback.call(null, e);
                        }
                        mClient.disconnect();
                        trace(mClient + " socket IO exception ,socket will be reset..");
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
                callback.call(null, new IOException("invalid connection..."));
            if (socket != null) {
                mClient.disconnect();
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
        if (!mClient.isConnected()) return null;
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
            throw new TimeoutException(mClient + "request " + requestId + " timeout...");
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
        flushRequestQueue();
    }

    public void flushRequestQueue() {
        if (mPostTask != null)
            mPostTask.interrupt();
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
    public void test() {
        postEvents();
    }

    private Observable<Messages.Event> getEventObservable(final Messages.Event.EventType eventType) {
        return subjectEvent.filter(new Func1<Messages.Event, Boolean>() {
            @Override
            public Boolean call(Messages.Event event) {
                return event.getType() == eventType;
            }
        });
    }

    public Observable<Messages.Event> getEventObservable() {
        return subjectEvent;
    }

    public void destroy() {
        if (destroyed)
            destroyed = true;
        flushRequestQueue();
        if (mReadTask != null) {
            mReadTask.interrupt();
        }
    }

    private void postEvents() {
        for (Messages.Request request : getRequestQueue()) {
            if (request.getType() == Messages.Request.RequestType.SET_EVENTS) {
                flushRequestQueue();
                return;
            }
        }
        postRequestToQueue(RequestUtil.setEvent(mEventTypes.toArray(new Messages.Event.EventType[mEventTypes.size()])), new Action1<Messages.Response>() {
            @Override
            public void call(Messages.Response response) {
                if (response.getErrorCode() != Messages.Response.ErrorCode.SUCCESS) postEvents();
            }
        });
    }

    private Messages.Callback pullCallback(final SSLSocket sslSocket) throws IOException {
        InputStream in = sslSocket.getInputStream();
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

    private class RequestQueuePostTask implements Runnable {
        @Override
        public void run() {
            Thread.currentThread().setName(mClient + " RequestQueuePostTask");
            trace(mClient + " start post task...");
            while (mClient.isConnected()) {
                if (mPostQueue != null && mPostQueue.size() > 0 && mClient.isAuthenticated()) {
                    for (final Messages.Request request : getRequestQueue()) {
                        try {
                            final Messages.Response response = postRequest(request);
                            mPostQueue.remove(request).call(response);
                        } catch (TimeoutException e) {
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
            }
        }
    }

    private class SocketListeningTask implements Runnable {
        @Override
        public void run() {
            Thread.currentThread().setName(mClient + " SocketListeningTask");
            final SSLSocket sslSocket = socket;
            while (mClient.isConnected()) {
                try {
                    Messages.Callback callback = pullCallback(sslSocket);
                    if (callback == null) continue;
                    trace(mClient + " callback packet :" + callback);
                    subjectCallback.onNext(callback);
                } catch (IOException e) {
                    e.printStackTrace();
                    trace(mClient + " read stream error");
                    mClient.disconnect();
                    break;
                }
            }
        }
    }
}