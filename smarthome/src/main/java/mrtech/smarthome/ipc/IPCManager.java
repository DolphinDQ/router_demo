package mrtech.smarthome.ipc;

import android.opengl.GLSurfaceView;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import hsl.p2pipcam.nativecaller.DeviceSDK;
import mrtech.smarthome.SmartHomeApp;
import rx.Subscription;
import rx.functions.Action1;
import rx.subjects.PublishSubject;


/**
 * IPC管理器，实现IPC功能调度
 */
public class IPCManager {
    private static IPCManager ourInstance = new IPCManager();
    private static boolean isInit;
    private final ArrayList<IPCamera> mCameras = new ArrayList<>();
    private static final HSLEventReceiver hslEventController = new HSLEventReceiver();
    private PublishSubject<List<IPCamera>> mCameraListChanged = PublishSubject.create();

    private static void trace(String msg) {

        if (SmartHomeApp.DEBUG)
            Log.d(IPCManager.class.getName(), msg);
    }

    /**
     * 获取单例对象
     *
     * @return IPCManager单例对象
     */
    public static IPCManager getInstance() {
        return ourInstance;
    }

    /**
     * 创建新IPC管理器
     *
     * @return 新建的IPCManager对象
     */
    public static IPCManager createNewManager() {
        return new IPCManager();
    }

    public static VideoRenderer initGLSurfaceView(GLSurfaceView surfaceView) {
        VideoRenderer videoRenderer = new VideoRenderer(surfaceView);
        surfaceView.setRenderer(videoRenderer);
        return videoRenderer;
    }

    /**
     * 初始化IPCManager运行环境
     */
    public static void init() {
        if (isInit) return;
        isInit = true;
        trace("DeviceSDK init...");
        DeviceSDK.initialize("");
        DeviceSDK.setCallback(hslEventController);
        DeviceSDK.networkDetect();
    }

    /**
     * 销毁IPCManager运行环境
     */
    public static void destroy() {
        if (isInit) {
            isInit = false;
            trace("DeviceSDK destroy...");
            DeviceSDK.unInitSearchDevice();
        }
    }

    /**
     * 添加IPC至管理器
     *
     * @param cam 指定IPC
     */
    public void addCamera(IPCamera... cameras) {
        if (cameras == null || cameras.length == 0) return;
        for (IPCamera cam : cameras) {
            if (getCamera(cam.getDeviceId()) == null) {
                final HSLCameraClient ipcContext = new HSLCameraClient(this, cam);
                cam.setIpcContext(ipcContext);
                mCameras.add(cam);
                ipcContext.init();
            }
        }
        mCameraListChanged.onNext(mCameras);
    }

    /**
     * 获取当前管理器中的IPC信息
     *
     * @return IPC列表
     */
    public List<IPCamera> getCameraList() {
        return mCameras;
    }

    /**
     * 删除指定IPC
     *
     * @param cam 指定IPC
     */
    public void removeCamera(IPCamera cam) {
        if (cam == null) return;
        cam.getIpcContext().destroy();
        mCameras.remove(cam);
        mCameraListChanged.onNext(mCameras);
    }

    /**
     * 删除管理器中所有IPC
     */
    public void removeAll() {
        IPCamera[] cameraList = mCameras.toArray(new IPCamera[mCameras.size()]);
        for (IPCamera camera : cameraList) {
            camera.getIpcContext().destroy();
            mCameras.remove(camera);
        }
        mCameraListChanged.onNext(mCameras);
    }

//    public void removeCamera(String deviceId) {
//        for (IPCamera cam : mCameras) {
//            if (cam.getDeviceId().equals(deviceId)) {
//                mCameras.remove(cam);
//                return;
//            }
//        }
//    }

    /**
     * 订阅摄像头列表变更事件。
     *
     * @param callback 摄像头列表回调。
     * @return 订阅句柄。
     */
    public Subscription subscribeCameraListChanged(Action1<List<IPCamera>> callback) {
        return mCameraListChanged.subscribe(callback);
    }

    /**
     * 通过IPC设备ID获取IPC
     *
     * @param deviceId IPC设备ID
     * @return 指定IPC对象，如果找不到返回null
     */
    public IPCamera getCamera(String deviceId) {
        for (IPCamera cam : mCameras) {
            if (cam.getDeviceId().equals(deviceId))
                return cam;
        }
        return null;
    }

    /**
     * 通过IPC连接句柄获取IPC
     *
     * @param handle IPC连接句柄
     * @return 指定IPC对象，如果找不到返回null
     */
    public IPCamera getCamera(long handle) {
        for (IPCamera cam : mCameras) {
            if (cam.getIpcContext().getHandle() == handle) {
                return cam;
            }
        }
        return null;
    }

    /**
     * 创建播放器
     *
     * @param glSurfaceView 指定OpenGL控件
     * @return IPC播放器
     */
    public IPCPlayer createCameraPlayer(GLSurfaceView glSurfaceView) {
        return createCameraPlayer(initGLSurfaceView(glSurfaceView));
    }

    /**
     * 使用已经创建的渲染器创建播放器
     *
     * @param videoRenderer 视频渲染器
     * @return 播放器
     */
    public IPCPlayer createCameraPlayer(VideoRenderer videoRenderer) {
        return new HSLPlayer(videoRenderer, this);
    }

    /**
     * 创建IPC控制器。用于控制IPC云台，以及其他操作
     *
     * @param camera 指定要控制的IPC
     * @return 返回指定IPC控制器
     */
    public IPCController createController(IPCamera camera) {
        return new HSLCameraController(camera);
    }

    /**
     * 创建IPC事件管理器，用于订阅IPC事件
     *
     * @param camera 指定IPC。null为所有IPC统一事件
     * @return IPC事件控制器
     */
    public IPCEventManager createEventManager(@Nullable IPCamera camera) {
        return camera == null
                ? hslEventController.createEventManager(null)
                : hslEventController.createEventManager(camera.getIpcContext().getHandle());
    }

}
