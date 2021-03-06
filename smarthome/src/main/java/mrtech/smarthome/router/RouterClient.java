package mrtech.smarthome.router;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.stream.NewAllStreamParser;

import mrtech.smarthome.SmartHomeApp;
import mrtech.smarthome.ipc.IPCManager;
import mrtech.smarthome.router.Models.*;

import java.io.IOException;
import java.net.SocketException;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLSocket;

import mrtech.smarthome.rpc.Messages;
import mrtech.smarthome.util.NetUtil;
import mrtech.smarthome.util.CharUtil;
import mrtech.smarthome.util.RequestUtil;
import rx.Subscription;
import rx.functions.Action1;
import rx.subjects.PublishSubject;

/**
 * Created by sphynx on 2015/12/2.
 */
class RouterClient implements RouterSession {
    private final RouterClient mContext;
    private final RouterCameraDataManager mCameraManager;
    private CheckStatusTask mCheckStatusTask;

    private static void trace(String msg) {
        if (SmartHomeApp.DEBUG)
            Log.d(RouterClient.class.getName(), msg);
    }

    private final Router mRouter;
    private final RouterManager mManager;
    private final String mSN;
    private final PublishSubject<Router> subjectRouterStatusChanged = PublishSubject.create();
    private final RouterCommunicationManager mCommunicationManager;
    private boolean invalidSN;
    private boolean authenticated;
    private boolean initialized;
    private int mP2PHandle;
    private int port;
    private String p2pSN;
    private String apiKey;
    private RouterStatus routerStatus;
    private SSLSocket socket;

    public RouterClient(Router router, int p2pHandle) {
        mContext = RouterClient.this;
        mManager = RouterManager.getInstance();
        mSN = router.getSn();
        mP2PHandle = p2pHandle;
        mRouter = router;
        mCommunicationManager = new RouterCommunicationManager(this);
        mCameraManager = new RouterCameraDataManager(mCommunicationManager);
        setRouterStatus(RouterStatus.INITIAL);
    }

    @Override
    public String toString() {
        return "RouterClient:" + mSN;
    }

    public void init() {
        if (initialized) return;
        initialized = true;
        setRouterStatus(RouterStatus.INITIALIZED);

        mCheckStatusTask = new CheckStatusTask();
        new Thread(mCheckStatusTask).start();
    }

    public boolean decodeSN() {
        if (p2pSN == null && apiKey == null) {
            try {
                setRouterStatus(RouterStatus.SN_DECODING);
                trace("decoding sn:" + mSN);
                final String code = CharUtil.decodeQRCode(mSN);
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
        trace("decoded sn:" + mContext + "failed...");
        invalidSN = true;
        setRouterStatus(RouterStatus.SN_INVALID);
        return false;
    }

    public boolean createPort(int delay) {
        synchronized (mManager) {
            try {
                removePort();
                trace(mContext + " adding port...");
                setRouterStatus(RouterStatus.P2P_CONNECTING);
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int tmp = NewAllStreamParser.DNPAddPort(mP2PHandle, p2pSN);
                        trace(mContext + "return port:" + tmp);
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
//                ex.printStackTrace();
                port = 0;
                trace(mContext + "add port failed..");
            } finally {
                setRouterStatus(isPortValid() ? RouterStatus.P2P_CONNECTED : RouterStatus.P2P_DISCONNECTED);
                trace(mContext + " p2p port:" + port);
            }
            return port != 0;
        }
    }

    public void removePort() {
        if (port == 0) return;
        disconnect();
        try {
            trace(mContext + " remove port...");
            NewAllStreamParser.DNPDelPort(mP2PHandle, port);
            port = 0;
            setRouterStatus(RouterStatus.P2P_DISCONNECTED);
        } catch (Exception e) {
            e.printStackTrace();
            trace(mContext + " remove port failed...");
        }
    }

    public void disconnect() {
        if (socket != null) {
            if (!socket.isClosed()) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            trace(mContext + "socket close failed..");
                        }
                    }
                });
            }
            socket = null;
            authenticated = false;
            setRouterStatus(RouterStatus.ROUTER_DISCONNECTED);
        }
    }

    public boolean connect() {
        disconnect();
        trace(mContext + " creating ssl socket.");
        try {
            setRouterStatus(RouterStatus.ROUTER_CONNECTING);
            SSLSocket tempSocket = NetUtil.createSocket("localhost", port);
            if (tempSocket != null && tempSocket.getSession().isValid()) {
                socket = tempSocket;
                mCommunicationManager.init(socket);
            }
            setRouterStatus(isConnected() ? RouterStatus.ROUTER_CONNECTED : RouterStatus.ROUTER_DISCONNECTED);
        } catch (SocketException e) {
            trace(mContext + "SocketException.!!!!removePort " + e.getMessage());
            removePort();
        } catch (Exception e) {
            trace(mContext + "create ssl socket error." + e.getMessage());
            e.printStackTrace();
            disconnect();
        }
        return isConnected();
    }

    public boolean authenticate() {
        if (!isConnected()) return false;
        try {
            trace(mContext + " authentication!!!");
            Messages.Response resp = mCommunicationManager.postRequest(RequestUtil.getAuthRequest(apiKey));
            final Messages.Response.ErrorCode code = resp.getErrorCode();
            authenticated =
                    code == Messages.Response.ErrorCode.SUCCESS ||
                            code == Messages.Response.ErrorCode.ALREADY_AUTHENTICATED;
            trace(mContext + " authentication result :" + code);
        } catch (TimeoutException e) {
            e.printStackTrace();
            trace("auth time out");
        } finally {
            setRouterStatus(authenticated ? RouterStatus.API_AUTH_SUCCESS : RouterStatus.API_UNAUTHORIZED);
        }
        return authenticated;
    }

    public void destroy() {
        if (!initialized) return;
        initialized = false;

        new Thread(new Runnable() {
            @Override
            public void run() {
                removePort();
                mCameraManager.getIPCManager().removeAll();
                mCommunicationManager.destroy();
                mCheckStatusTask.interrupt();
                setRouterStatus(RouterStatus.INITIAL);
            }
        }).start();
    }

    @Override
    public void reconnect() {
        disconnect();
        if (mCheckStatusTask != null) {
            trace("trying recheck status...");
            mCheckStatusTask.interrupt();
        }
    }

    @Override
    public CameraDataManager getCameraManager() {
        return mCameraManager;
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
    public CommunicationManager getCommunicationManager() {
        return mCommunicationManager;
    }

    @Override
    public Subscription subscribeRouterStatusChanged(Action1<Router> callback) {
        return subjectRouterStatusChanged.subscribe(callback);
    }

    private void setRouterStatus(RouterStatus routerStatus) {
        trace(mContext + " status changed to:" + routerStatus);
        mContext.routerStatus = routerStatus;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                subjectRouterStatusChanged.onNext(mRouter);
            }
        });
    }

    private class CheckStatusTask implements Runnable {

        private Thread currentThread;
        /**
         * 重连次数。
         */
        private int times = 0;
        /**
         * 每次重连延迟。
         */
        private int reconnectDelay = 5000;


        private int keepAlive() {
            if (isAuthenticated()) {
                trace(mContext + " keep alive..");
                try {
                    Messages.Response response = mCommunicationManager.postRequest(RequestUtil.getKeepAliveRequest());
                    if (response != null && response.getErrorCode() == Messages.Response.ErrorCode.SUCCESS) {
                        times = 0;//重置重连次数。
                        return ROUTER_KEEP_ALIVE_DELAY;
                    }
                } catch (TimeoutException e) {
                    trace("keep alive failed!!");
//                disconnect();
                }
            }
            int ret = times * reconnectDelay;
            if (times < 24) times++; //每次重连不成功延迟5秒，直到2分钟。
            return ret;
        }

        private int checkRouterStatus() {
            try {
                if (!isSNValid()) return -1;
                if (!isPortValid())
                    if (!createPort(ROUTER_ADD_PORT_DELAY)) return ROUTER_ADD_PORT_DELAY;
                if (!isConnected())
                    if (!connect()) return ROUTER_RECONNECTION_DELAY;
                if (!isAuthenticated())
                    if (!authenticate()) return ROUTER_AUTH_DELAY;
                return keepAlive();
            } catch (Exception ex) {
                trace(mContext + " check status failed.." + ex.getMessage());
                ex.printStackTrace();
            }
            return -1;
        }

        private boolean running;

        public void interrupt() {
            if (!running)
                currentThread.interrupt();
        }

        @Override
        public void run() {
            if (running) return;
            currentThread = Thread.currentThread();
            currentThread.setName("checkRouterStatus:" + mContext);
            decodeSN();
            do {
                int delay;
                try {
                    running = true;
                    delay = checkRouterStatus();
                    if (delay < 0) {
                        destroy();
                        break;
                    }
                    running = false;
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    trace(Thread.currentThread().getName() + " wakeup!");
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    running = false;
                }
            } while (initialized);
        }
    }

    public Router getRouter() {
        return mRouter;
    }


}
