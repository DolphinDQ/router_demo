package mrtech.smarthome.ipc;

import android.opengl.GLSurfaceView;
import android.util.Log;

import java.util.ArrayList;

import hsl.p2pipcam.nativecaller.DeviceSDK;
import mrtech.smarthome.ipc.IPCModels.*;

/**
 * manager of  IP camera . keep the connection
 * Created by zdqa1 on 2015/11/25.
 */
public class IPCManager {
    private static IPCManager ourInstance = new IPCManager();
    private boolean isInit;
    private final ArrayList<IPCamera> mCameras = new ArrayList<>();
    private final HSLEventController hslEventController;

    private static void trace(String msg) {
        Log.e(IPCManager.class.getName(), msg);
    }

    private static void trace(String msg, Throwable ex) {
        Log.d(IPCManager.class.getName(), msg, ex);
    }

    public static IPCManager getInstance() {
        return ourInstance;
    }

    private IPCManager() {
        hslEventController = new HSLEventController(this);
    }

    public void init() {
        if (isInit) return;
        isInit = true;
        trace("DeviceSDK init...");
        DeviceSDK.initialize("");
        DeviceSDK.setCallback(hslEventController);
        DeviceSDK.networkDetect();
    }

    public void destroy() {
        if (isInit) {
            isInit = false;
            trace("DeviceSDK destroy...");
            removeAll();
            DeviceSDK.unInitSearchDevice();
        }
    }

    public void addCamera(IPCamera cam) {
        if (getCamera(cam.getDeviceId()) == null) {
            cam.setIpcContext(new HSLCameraClient(this, cam));
            mCameras.add(cam);
        }
    }

    public IPCamera[] getCameraList() {
        return mCameras.toArray(new IPCamera[mCameras.size()]);
    }

    public void removeCamera(IPCamera cam) {
        cam.getIpcContext().destroy();
        mCameras.remove(cam);
    }

    public void removeAll() {
        IPCamera[] cameraList = getCameraList();
        if (cameraList != null)
            for (IPCamera hslCamera : cameraList) {
                removeCamera(hslCamera);
            }
    }

    public void removeCamera(String deviceId) {
        for (IPCamera cam : mCameras) {
            if (cam.getDeviceId().equals(deviceId)) {
                mCameras.remove(cam);
                return;
            }
        }
    }

    public IPCamera getCamera(String deviceId) {
        for (IPCamera cam : mCameras) {
            if (cam.getDeviceId().equals(deviceId))
                return cam;
        }
        return null;
    }

    public IPCamera getCamera(long handle) {

        for (IPCamera cam : mCameras) {
            if (cam.getIpcContext().getHandle() == handle)
                return cam;
        }
        return null;
    }

    public IPCPlayer createCameraPlayer(GLSurfaceView glSurfaceView) {
        return new HSLPlayer(glSurfaceView, this);
    }

    public IPCController createController(IPCamera camera) {
        return new HSLCameraController(this, camera);
    }

    public IPCEventController createEventController() {
        return hslEventController;
    }
}