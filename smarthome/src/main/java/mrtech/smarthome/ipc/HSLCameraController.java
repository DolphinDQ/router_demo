package mrtech.smarthome.ipc;

import android.os.AsyncTask;
import android.util.Log;

import hsl.p2pipcam.nativecaller.DeviceSDK;
import mrtech.smarthome.BuildConfig;

/**
 * IPC控制器
 */
class HSLCameraController implements IPCController {
    private static void trace(String msg) {
        Log.d(HSLCameraController.class.getName(), msg);
    }

    private static void trace(String msg, Throwable ex) {
        if (BuildConfig.DEBUG)
            Log.d(IPCManager.class.getName(), msg, ex);
    }

    private static final int PTZ_CMD_DELAY = 1000;

    private final IPCamera mCurrent;

    /**
     * IPC控制器对象
     * @param camera 要控制的IPC对象
     */
    public HSLCameraController(IPCamera camera) {
        mCurrent = camera;
    }

    private void ptzControl(final int cmd) {
        if (mCurrent == null) return;
        if (!mCurrent.getIpcContext().isPlaying()) {
            trace("device must playing...");
            return;
        }
        synchronized (mCurrent) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    synchronized (HSLCameraController.this) {
                        try {
                            trace("ptz command:" + cmd);
                            DeviceSDK.ptzControl(mCurrent.getIpcContext().getHandle(), cmd);
                            Thread.sleep(PTZ_CMD_DELAY);
                            DeviceSDK.ptzControl(mCurrent.getIpcContext().getHandle(), cmd + 1);
                        } catch (Exception ex) {
                            trace("ptz control error", ex);
                        }
                    }
                    return null;
                }
            }.execute();
        }
    }

    /**
     * IPC向上转
     */
    @Override
    public void ptzUp() {
        ptzControl(0);
    }

    /**
     * IPC向下转
     */
    @Override
    public void ptzDown() {
        ptzControl(2);
    }

    /**
     * 获取控制器当前控制的IPC对象
     * @return 当前控制的IPC对象
     */
    @Override
    public IPCamera getCurrent() {
        return mCurrent;
    }

    /**
     * IPC向左转
     */
    @Override
    public void ptzLeft() {
        ptzControl(4);
    }

    /**
     * IPC向右转
     */
    @Override
    public void ptzRight() {
        ptzControl(6);
    }

}
