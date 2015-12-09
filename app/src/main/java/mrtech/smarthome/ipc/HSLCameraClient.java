package mrtech.smarthome.ipc;

import android.util.Log;

import hsl.p2pipcam.nativecaller.DeviceSDK;
import mrtech.smarthome.ipc.IPCModels.*;
import rx.Subscription;
import rx.functions.Action1;
import rx.subjects.PublishSubject;

/**
 * Created by sphynx on 2015/12/8.
 */
class HSLCameraClient implements IPCContext {
    private final IPCManager mManager;
    private final IPCamera mIPCamera;
    private final IPCEventController mEventController;
    private Subscription mAlarmEventHandle;
    private PublishSubject<IPCContext> subjectReconnectionStatus = PublishSubject.create();
    private PublishSubject<IPCContext> subjectPlayStatus = PublishSubject.create();
    public long mHandle;
    private int statusCode;
    private boolean isPlaying;
    private boolean reconnecting;

    private static void trace(String msg) {
        Log.e(HSLCameraClient.class.getName(), msg);
    }

    private void setReconnecting(boolean reconnecting) {
        if (reconnecting != this.reconnecting) {
            this.reconnecting = reconnecting;
            subjectReconnectionStatus.onNext(this);
        }
    }

    public void setIsPlaying(boolean isPlaying) {
        if (isPlaying == this.isPlaying) {
            this.isPlaying = isPlaying;
            subjectPlayStatus.onNext(this);
        }
    }

    public HSLCameraClient(IPCManager manager, IPCamera camera) {
        mManager = manager;
        mIPCamera = camera;
        mEventController = mManager.createEventController();
        init();
    }

    private void setAutoReconnect() {
        mAlarmEventHandle = mEventController.subscribeCameraStatus(new Action1<IPCEventData>() {
            @Override
            public void call(IPCEventData ipcEventData) {
                if (ipcEventData.equals(mIPCamera)) {
                    final int time = getReconnectTime(getStatus());
                    synchronized (HSLCameraClient.this) {
                        if (time > 0) {
                            setReconnecting(true);
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    disconnect();
                                    try {
                                        Thread.sleep(time);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    } finally {
                                    }
                                    connect();
                                    setReconnecting(false);
                                }
                            }).start();
                        }
                    }
                }
            }
        });
    }

    private int getReconnectTime(IPCStatus status) {
        if (status == IPCStatus.DEVICE_OFFLINE) return 10000;
        if (status == IPCStatus.CONNECT_ERROR) return 10000;
        if (status == IPCStatus.CONNECT_TIMEOUT) return 3000;
        if (status == IPCStatus.DISCONNECT) return 3000;
        if (status == IPCStatus.ERROR_MAX_USER) return 5000;
        return -1;
    }

    private void disconnect() {
        if (mHandle > 0) {
            DeviceSDK.closeDevice(mHandle);
            DeviceSDK.destoryDevice(mHandle);
        }
    }

    private void connect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                IPCamera cam = mIPCamera;
                trace("linking camera :" + cam);
                if (cam.getIpcContext().getHandle() == 0)
                    ((HSLCameraClient) cam.getIpcContext()).mHandle = DeviceSDK.createDevice(cam.getUserName(), cam.getPassword(), "", 0, cam.getDeviceId(), 1);
                trace("create camera " + cam + "->" + cam.getIpcContext().getHandle());
                if (cam.getIpcContext().getHandle() > 0) {
                    long open = DeviceSDK.openDevice(cam.getIpcContext().getHandle());
                    trace("open camera:" + open);
                }
            }
        }).start();
    }

    @Override
    public long getHandle() {
        return mHandle;
    }

    public void setStatusCode(int statusCode) {
        final IPCStatus status = getStatus(statusCode);
        if (status != IPCStatus.CONNECTED) {
            setIsPlaying(false);
        }
        trace("camera " + mIPCamera + " state code changed to " + statusCode);
        this.statusCode = statusCode;
    }

    @Override
    public IPCStatus getStatus() {
        return getStatus(statusCode);
    }

    private IPCStatus getStatus(int code) {
        switch (code) {
            case 0:
                return IPCStatus.CONNECTING;
            case 100:
                return IPCStatus.CONNECTED;
            case 102:
                return IPCStatus.CONNECT_ERROR;
            case 1:
                return IPCStatus.ERROR_USER_PWD;
            case 2:
                return IPCStatus.ERROR_MAX_USER;
            case 4:
                return IPCStatus.ERROR_VIDEO_LOST;
            case 5:
                return IPCStatus.ERROR_INVALID_ID;
            case 9:
                return IPCStatus.DEVICE_OFFLINE;
            case 10:
                return IPCStatus.CONNECT_TIMEOUT;
            case 11:
                return IPCStatus.DISCONNECT;
            case 12:
                return IPCStatus.CHECK_ACCOUNT;
            default:
                return IPCStatus.UNKNOWN;
        }
    }

    @Override
    public boolean isPlaying() {
        return isPlaying;
    }

    @Override
    public boolean isReconnecting() {
        return reconnecting;
    }

    @Override
    public void destroy() {
        disconnect();
        if (mAlarmEventHandle != null && !mAlarmEventHandle.isUnsubscribed()) {
            mAlarmEventHandle.unsubscribe();
        }
    }

    @Override
    public void init() {
        destroy();
        connect();
        setAutoReconnect();
    }

    @Override
    public Subscription subscribePlayStatus(Action1<IPCContext> callback) {
        return subjectPlayStatus.subscribe(callback);
    }

    @Override
    public Subscription subscribeReconnectionStatus(Action1<IPCContext> callback) {
        return subjectReconnectionStatus.subscribe(callback);
    }

}
