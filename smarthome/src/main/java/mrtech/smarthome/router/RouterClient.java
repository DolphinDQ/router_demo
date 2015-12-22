package mrtech.smarthome.router;

import android.util.Log;

import com.stream.NewAllStreamParser;

import mrtech.smarthome.ipc.IPCManager;
import mrtech.smarthome.ipc.IPCamera;
import mrtech.smarthome.router.Models.*;

import java.io.IOException;
import java.net.SocketException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLSocket;

import mrtech.smarthome.rpc.Messages;
import mrtech.smarthome.util.NetUtil;
import mrtech.smarthome.util.NumberUtil;
import mrtech.smarthome.util.RequestUtil;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Action2;
import rx.subjects.PublishSubject;

/**
 * Created by sphynx on 2015/12/2.
 */
class RouterClient implements RouterSession {
    private final RouterClient mContext;
    private Thread mCheckStatusTask;
    private static void trace(String msg) {
        Log.e(RouterClient.class.getName(), msg);
    }
    private final IPCManager mIPCManager;
    private final Router mRouter;
    private final RouterManager mManager;
    private final String mSN;
    private final PublishSubject<Router> subjectRouterStatusChanged = PublishSubject.create();
    private final RouterDataChannel mDataChannel;
    private boolean invalidSN;
    private boolean authenticated;
    private boolean initialized;
    private int mP2PHandle;
    private int port;
    private String p2pSN;
    private String apiKey;
    private RouterStatus routerStatus;
    private SSLSocket socket;
    private Messages.GetSystemConfigurationResponse systemConfigurationResponse;

    public RouterClient(Router router, int p2pHandle) {
        mContext = RouterClient.this;
        mManager = RouterManager.getInstance();
        mSN = router.getSN();
        mP2PHandle = p2pHandle;
        mRouter = router;
        mIPCManager = IPCManager.createNewManager();
        mDataChannel = new RouterDataChannel(this);
        setRouterStatus(RouterStatus.INITIAL);
    }

    @Override
    public String toString() {
        return "RouterClient:" + mSN;
    }

    public void init() {
        if (initialized) return;
        initialized = true;
        mIPCManager.removeAll();
        setRouterStatus(RouterStatus.INITIALIZED);
        mCheckStatusTask = new Thread(new CheckStatusTask());
        mCheckStatusTask.start();
    }

    public boolean decodeSN() {
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
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    trace(mContext + "socket close failed..");
                }
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
                mDataChannel.init(socket);
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
            Messages.Response resp = mDataChannel.postRequest(RequestUtil.getAuthRequest(apiKey));
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
        mDataChannel.destroy();
        mCheckStatusTask.interrupt();
        mIPCManager.removeAll();
        new Thread(new Runnable() {
            @Override
            public void run() {
                removePort();
            }
        }).start();
        setRouterStatus(RouterStatus.INITIAL);
    }

    @Override
    public Messages.GetSystemConfigurationResponse getRouterConfiguration(boolean refreshCache) {
        if (systemConfigurationResponse == null) {
            try {
                systemConfigurationResponse = mDataChannel.postRequest(RequestUtil.getSysConfig()).getExtension(Messages.GetSystemConfigurationResponse.response);
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
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public RouterStatus getRouterStatus() {
        return routerStatus;
    }

    @Override
    public IPCManager getIPCManager() {
        return mIPCManager;
    }

    @Override
    public void reloadIPCAsync(boolean cache, final Action1<Throwable> exception) {
        try {
            mDataChannel.postRequestAsync(RequestUtil.getDevices(mrtech.smarthome.rpc.Models.DeviceQuery.newBuilder()
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
    public Models.DataChannel getDataChannel() {
        return mDataChannel;
    }

    @Override
    public Subscription subscribeRouterStatusChanged(Action1<Router> callback) {
        return subjectRouterStatusChanged.subscribe(callback);
    }

    private void setRouterStatus(RouterStatus routerStatus) {
        mContext.routerStatus = routerStatus;
        subjectRouterStatusChanged.onNext(mRouter);
    }

    private class CheckStatusTask implements Runnable {

        private void keepAlive() {
            if (!isAuthenticated()) return;
            trace(mContext + " keep alive..");
            try {
                mDataChannel.postRequest(RequestUtil.getKeepAliveRequest());
            } catch (TimeoutException e) {
                trace("keep alive failed!!");
                disconnect();
            }
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
                keepAlive();
            } catch (Exception ex) {
                ex.printStackTrace();
                trace(mContext + " check status failed.." + ex.getMessage());
            }
            return ROUTER_KEEP_ALIVE_DELAY;
        }

        @Override
        public void run() {
            Thread.currentThread().setName("checkRouterStatus:" + mContext);
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
                        trace(Thread.currentThread().getName() + " wakeup!");
                    }
                }
            } while (initialized);
        }
    }

}
