package mrtech.smarthome.ipc;

import android.util.Log;

import hsl.p2pipcam.nativecaller.DeviceSDK;
import mrtech.smarthome.SmartHomeApp;
import mrtech.smarthome.ipc.Models.*;
import rx.Subscription;
import rx.functions.Action1;
import rx.subjects.PublishSubject;

/**
 * HSLCameraClient
 * Created by sphynx on 2015/12/8.
 */
class HSLCameraClient implements IPCContext {
    private final IPCManager mManager;
    private final IPCamera mIPCamera;
    private final IPCEventManager mEventController;
    private Subscription mAlarmEventHandle;


    private PublishSubject<IPCContext> subjectReconnectionStatus = PublishSubject.create();
    private PublishSubject<IPCContext> subjectPlayStatus = PublishSubject.create();
    public long mHandle;
    private IPCStatus statusCode;
    private boolean isPlaying;
    private boolean reconnecting;
    private boolean autoReconnect;

    private static void trace(String msg) {
        if (SmartHomeApp.DEBUG)
            Log.d(HSLCameraClient.class.getName(), msg);
    }

    private void setReconnecting(boolean reconnecting) {
        if (reconnecting != this.reconnecting) {
            this.reconnecting = reconnecting;
            subjectReconnectionStatus.onNext(this);
        }
    }

    /**
     * 设置IPC播放状态
     *
     * @param isPlaying IPC播放状态
     */
    public void setIsPlaying(boolean isPlaying) {
        if (isPlaying != this.isPlaying) {
            this.isPlaying = isPlaying;
            subjectPlayStatus.onNext(this);
        }
    }


    public HSLCameraClient(IPCManager manager, IPCamera camera) {
        mManager = manager;
        mIPCamera = camera;
        mEventController = mManager.createEventManager(null);
    }

    private void setAutoReconnect() {
        if (autoReconnect)return;
        autoReconnect=true;
        mAlarmEventHandle = mEventController.subscribeCameraStatus(new Action1<IPCStateChanged>() {
            @Override
            public void call(IPCStateChanged ipcStateChanged) {
                if (!autoReconnect)return;
                final IPCamera camera = mManager.getCamera(ipcStateChanged.getCameraId());
                if (camera != null && camera.equals(mIPCamera)) {
                    trace("camera " + mIPCamera + " state code changed to " + ipcStateChanged.getStatus());
                    statusCode = ipcStateChanged.getStatus();
                    if (statusCode != IPCStatus.CONNECTED) {
                        setIsPlaying(false);
                    }

                    final int delay = getReconnectDelay(statusCode);
                    if (delay > 0)
                        reconnect(delay);
                }
            }
        });
    }

    /**
     * IPC重连
     *
     * @param delay delay 大于0则根据delay毫秒数设置重连时间，小于零则不重连
     */
    public synchronized void reconnect(final int delay) {
        if (delay > 0) {
            setReconnecting(true);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    disconnect();
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    connect();
                    setReconnecting(false);
                }
            }).start();
        }
    }


    public rx.Observable<IPCContext> getObservableReconnectionStatus() {
        return subjectReconnectionStatus;
    }


    public rx.Observable<IPCContext> getObservablePlayStatus() {
        return subjectPlayStatus;
    }

    /**
     * 获取指定状态摄像头重连时间
     *
     * @param status 状态
     * @return 不重连返回-1
     */
    private int getReconnectDelay(IPCStatus status) {
        if (status == IPCStatus.DEVICE_OFFLINE) return 10000;
        if (status == IPCStatus.CONNECT_ERROR) return 10000;
        if (status == IPCStatus.CONNECT_TIMEOUT) return 3000;
        if (status == IPCStatus.DISCONNECT) return 3000;
        if (status == IPCStatus.ERROR_MAX_USER) return 5000;
        return -1;
    }

    private void disconnect() {
        if (mHandle > 0) {
            //DeviceSDK.closeDevice(mHandle);
            DeviceSDK.destoryDevice(mHandle);
            mHandle = 0;
        }
    }

    private void connect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                IPCamera cam = mIPCamera;
                trace("linking camera :" + cam);
                if (getCameraId() == 0) {
                    long handle = DeviceSDK.createDevice(cam.getUserName(), cam.getPassword(), "", 0, cam.getDeviceId(), 1);
                    trace("create camera " + cam + "->" + handle);
                    if (handle > 0) {
                        long open = DeviceSDK.openDevice(handle);
                        trace("open camera:" + open);
                        if (open == 1) {
                            mHandle = handle;
                        }else {
                            DeviceSDK.destoryDevice(handle);
                        }
                    }
                }
            }
        }).start();
    }

    /**
     * 获取IPC对象
     *
     * @return IPC对象
     */
    public IPCamera getIPCamera() {
        return mIPCamera;
    }


    @Override
    public long getCameraId() {
        return mHandle;
    }

    /**
     * 获取IPC状态
     *
     * @return 摄像头状态
     */
    @Override
    public IPCStatus getStatus() {
        return statusCode;
    }

    /**
     * 判断IPC的播放状态
     *
     * @return 播放状态
     */
    @Override
    public boolean isPlaying() {
        return isPlaying;
    }

    /**
     * 判断IPC的重连状态
     *
     * @return 重连状态
     */
    @Override
    public boolean isReconnecting() {
        return reconnecting;
    }

    /**
     * 销毁
     */
    @Override
    public void destroy() {
        disconnect();
        if (mAlarmEventHandle != null ) {
            autoReconnect=false;
            mAlarmEventHandle.unsubscribe();
        }
    }

    /**
     * 初始化
     */
    @Override
    public void init() {
        destroy();
        connect();
        setAutoReconnect();
    }

    /**
     * 订阅IPC播放状态回调事件
     *
     * @param callback 播放状态变化回调
     * @return 订阅事件
     */
    @Override
    public Subscription subscribePlayStatus(Action1<IPCContext> callback) {
        return getObservablePlayStatus().subscribe(callback);
    }

    /**
     * 订阅IPC重连回调事件
     *
     * @param callback IPC重连回调
     * @return 订阅事件
     */
    @Override
    public Subscription subscribeReconnectionStatus(Action1<IPCContext> callback) {
        return getObservableReconnectionStatus().subscribe(callback);
    }
}
