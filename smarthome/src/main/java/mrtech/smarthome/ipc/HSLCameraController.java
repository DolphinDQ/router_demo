package mrtech.smarthome.ipc;

import android.os.AsyncTask;
import android.util.Log;

import hsl.p2pipcam.nativecaller.DeviceSDK;

/**
 * Created by sphynx on 2015/12/8.
 */
class HSLCameraController implements IPCController {
    private static void trace(String msg) {
        Log.e(HSLCameraController.class.getName(), msg);
    }

    private static void trace(String msg, Throwable ex) {
        Log.d(IPCManager.class.getName(), msg, ex);
    }

    private static final int PTZ_CMD_DELAY = 1000;

    private final IPCamera mCurrent;

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

    @Override
    public void ptzUp() {
        ptzControl(0);
    }

    @Override
    public void ptzDown() {
        ptzControl(2);
    }

    @Override
    public IPCamera getCurrent() {
        return mCurrent;
    }

    @Override
    public void ptzLeft() {
        ptzControl(4);
    }

    @Override
    public void ptzRight() {
        ptzControl(6);
    }

}
