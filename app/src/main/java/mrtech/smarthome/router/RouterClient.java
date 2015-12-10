package mrtech.smarthome.router;

import android.util.Log;

import com.squareup.okhttp.Route;
import com.stream.NewAllStreamParser;

import org.xmlpull.v1.XmlPullParser;

import mrtech.smarthome.router.Models.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Observable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.prefs.InvalidPreferencesFormatException;

import javax.net.ssl.SSLSocket;

import mrtech.smarthome.interf.ResponseThreadListener;
import mrtech.smarthome.rpc.Messages;
import mrtech.smarthome.util.NetUtil;
import mrtech.smarthome.util.NumberUtil;
import mrtech.smarthome.util.RequestUtil;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

/**
 * Created by sphynx on 2015/12/2.
 */
class RouterClient implements RouterContext {
    private static final int ROUTER_REQUEST_TIMEOUT = 2000;
    private static final int ROUTER_KEEP_ALIVE_DELAY = 20000;
    private static final int ROUTER_RECONNECTION_DELAY = 5000;
    private static final int ROUTER_ADD_PORT_DELAY = 5000;
    private static final int ROUTER_AUTH_DELAY = 5000;
    private static final int ROUTER_READ_INTERVAL = 1000;

    private static void trace(String msg) {
        Log.e(RouterClient.class.getName(), msg);
    }

    private final Router mRouter;
    private final RouterManager mManager;
    private final HashMap<Integer, Messages.Request> mSubscribeMap;
    private final ConcurrentHashMap<Integer, Messages.Response> mResponseMap;
    private final String mSN;
    private int mP2PHandle;
    private SocketListeningTask readSocketTask;
    private String p2pSN;
    private String apiKey;
    private boolean invalidSN;
    private int port;
    private SSLSocket socket;
    private boolean authenticated;
    private ResponseThreadListener mResponseListener;
    private boolean destroyed = false;
    private PublishSubject<Router> subjectRouterStatusChanged = PublishSubject.create();
    private PublishSubject<Messages.Callback> subjectCallback = PublishSubject.create();
    private rx.Observable<Messages.Response> subjectResponse;

    public RouterClient(Router router, int p2pHandle) {
        mManager = RouterManager.getInstance();
        mSN = router.getSN();
        mP2PHandle = p2pHandle;
        mRouter = router;
        mSubscribeMap = new HashMap<>();
        mResponseMap = new ConcurrentHashMap<>();
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
        });
    }

    private boolean decodeSN() {
        if (p2pSN == null && apiKey == null) {
            try {
                trace("decoding sn:" + mSN);
                final String code = NumberUtil.decodeQRCode(mSN);
                trace("decoded sn:" + mSN + " -> " + code);
                final String[] strings = code.split("@");
                if (strings.length == 2) {
                    apiKey = strings[0];
                    p2pSN = strings[1];
                    return true;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                invalidSN = true;
                trace("decoded sn:" + this + "failed...");
            }
        }
        return false;
    }

    public void init() {
        decodeSN();
        subjectRouterStatusChanged = PublishSubject.create();
        new Thread(new RouterConnectionTask()).start();
    }

    @Override
    public String toString() {
        return "RouterClient:" + mSN;
    }

    //====================================================================================
    private void disconnect() {
        if (socket != null) {
            if (!socket.isClosed()) {
                try {
                    readSocketTask.cancel();
                    socket.close();
                    socket = null;
                    authenticated = false;
                    subjectRouterStatusChanged.onNext(mRouter);

                } catch (IOException e) {
                    e.printStackTrace();
                    trace(this + "socket close failed..");
                }
            }
            socket = null;
        }
    }

    private boolean connect() {
        disconnect();
        trace(this + " creating ssl socket.");
        try {
            SSLSocket tempSocket = NetUtil.createSocket("localhost", port);
            if (tempSocket == null || !tempSocket.getSession().isValid())
                return false;
            socket = tempSocket;
            readSocketTask = new SocketListeningTask();
            new Thread(readSocketTask).start();
            subjectRouterStatusChanged.onNext(mRouter);
            trace(this + " created ssl socket." + isConnected());
        } catch (SocketException e) {
            trace(this + "SocketException.!!!!removePort" + e.getMessage());
            removePort();
        } catch (Exception e) {
            e.printStackTrace();
            trace(this + "create ssl socket error." + e.getMessage());
            disconnect();
        }
        return isConnected();
    }

    //====================================================================================
    private boolean addPort() {
        synchronized (mManager) {
            try {
                removePort();
                trace(this + " adding port...");
                if (NewAllStreamParser.DNPCheckSrvConnState(mP2PHandle) != 2) return false;
                port = NewAllStreamParser.DNPAddPort(mP2PHandle, p2pSN);
                subjectRouterStatusChanged.onNext(mRouter);

                trace(this + " p2p port:" + port);
            } catch (Exception ex) {
                ex.printStackTrace();
                port = 0;
                trace(this + "add port failed..");
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
            subjectRouterStatusChanged.onNext(mRouter);

        } catch (Exception e) {
            e.printStackTrace();
            trace(this + " remove port failed...");
        }
        return;
    }

    //====================================================================================
    private boolean doAuth() {
        if (!isConnected()) return false;
        try {
            trace(RouterClient.this + " authentication!!!");
            Messages.Response resp = addRequestSync(RequestUtil.getAuthRequest(apiKey));
            final Messages.Response.ErrorCode code = resp.getErrorCode();
            authenticated =
                    code == Messages.Response.ErrorCode.SUCCESS ||
                            code == Messages.Response.ErrorCode.ALREADY_AUTHENTICATED;
            subjectRouterStatusChanged.onNext(mRouter);

            trace(RouterClient.this + " authentication result :" + code);
        } catch (TimeoutException e) {
            e.printStackTrace();
            trace("auth time out");
        }
        return authenticated;
    }

    private void keepAlive() {
        if (!isAuthenticated()) return;
        trace(RouterClient.this + " keep alive..");
        try {
            addRequestSync(RequestUtil.getKeepAliveRequest());
        } catch (TimeoutException e) {
//            e.printStackTrace();
            trace("keep alive failed!!");
            disconnect();
        }
    }

    public void destroy() {
        if (destroyed) return;
        destroyed = true;
        subjectRouterStatusChanged.onCompleted();
        new Thread(new Runnable() {
            @Override
            public void run() {
                removePort();
            }
        }).start();
    }

    @Override
    public void addRequest(final Messages.Request request, final Action1<Messages.Response> callback) {
        if (request != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (request != null) {
                        OutputStream os = null;
                        if (isConnected()) {
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
                                trace(RouterClient.this + " socket IO exception ,socket will be reset..");
                            } finally {
                                try {
                                    os.close();
                                } catch (Exception e2) {
                                    e2.printStackTrace();
                                }
                            }
                        } else {
                            if (socket == null) {
                                trace("socket is null");
                            } else {
                                disconnect();
                            }
                        }
                    }
                }
            }).start();
            if (callback != null)
                subjectResponse.first(new Func1<Messages.Response, Boolean>() {
                    @Override
                    public Boolean call(Messages.Response resp) {
                        return resp.getRequestId() == request.getRequestId();
                    }
                }).subscribe(callback);
        }
    }

    @Override
    public Messages.Response addRequestSync(Messages.Request request, final int timeout) throws TimeoutException {
        if (request == null && isConnected()) return null;
        mSubscribeMap.put(request.getRequestId(), request);
        addRequest(request, null);
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
    public Messages.Response addRequestSync(Messages.Request request) throws TimeoutException {
        return addRequestSync(request, ROUTER_REQUEST_TIMEOUT);
    }

    @Override
    public void setResponseListener(ResponseThreadListener listener) {
        mResponseListener = listener;
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

    private Messages.Callback pullCallback(final SSLSocket sslSocket) throws IOException, InvalidPreferencesFormatException {
        InputStream in = null;
        in = sslSocket.getInputStream();
        byte[] prefix = new byte[2];
        int received = 0;
        in.read(prefix);
        if (prefix[0] == 0 && prefix[1] == 0)
            throw new InvalidPreferencesFormatException("invalid package header..");
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
                if (!addPort()) return ROUTER_ADD_PORT_DELAY;
            if (!isConnected())
                if (!connect()) return ROUTER_RECONNECTION_DELAY;
            if (!isAuthenticated())
                if (!doAuth()) return ROUTER_AUTH_DELAY;
            keepAlive();
        } catch (Exception ex) {
            ex.printStackTrace();
            trace(this + " check status failed.." + ex.getMessage());
        } finally {
            return ROUTER_KEEP_ALIVE_DELAY;
        }
    }

    public Subscription subscribeRouterStatusChanged(Action1<Router> callback) {
        return subjectRouterStatusChanged.subscribe(callback);
    }

    private class RouterConnectionTask implements Runnable {
        @Override
        public void run() {
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
            } while (!destroyed);
        }
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
                    } catch (IOException e) {
                        e.printStackTrace();
                        trace(RouterClient.this + " read stream error");
                        if (mResponseListener != null) {
                            mResponseListener.onRequestFailure(e.getMessage(), e);
                        }
                        disconnect();
                        continue;
                    } catch (InvalidPreferencesFormatException e) {
                        continue;
                    }

                    if (callback == null) continue;
                    processCallback(callback);
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

        private void processCallback(Messages.Callback callback) {
            trace(RouterClient.this + " callback packet :" + callback);
            if (callback.getType() == Messages.Callback.CallbackType.RESPONSE) {
                final Messages.Response response = callback.getExtension(Messages.Response.callback);
                if (response == null) {
                    trace(RouterClient.this + " unknown response data..");
                    return;
                }
                final Messages.Response.ErrorCode errorCode = response.getErrorCode();
//                trace(RouterClient.this + " received package requestId:" + response.getRequestId() + " code " + response.getErrorCode());
                if (mSubscribeMap.containsKey(response.getRequestId())) {
                    mResponseMap.put(response.getRequestId(), response);
                }
                try {
                    if (mResponseListener != null) {
                        if (errorCode == Messages.Response.ErrorCode.SUCCESS) {
                            mResponseListener.onRequestSuccess(callback);
                        } else {
                            mResponseListener.onRequestFailure(mManager.getErrorMessage(errorCode), null);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    trace(RouterClient.this + "callback failed");
                }
            }
        }
    }


}
