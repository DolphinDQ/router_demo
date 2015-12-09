package mrtech.smarthome.ipc;

import android.bluetooth.BluetoothClass;
import android.util.Log;

import mrtech.smarthome.ipc.IPCModels.*;

import rx.Subscription;
import rx.functions.Action1;
import rx.subjects.PublishSubject;

/**
 * Created by sphynx on 2015/12/8.
 */
class HSLEventController implements IPCEventController {
    private static void trace(String msg) {
        Log.e(IPCManager.class.getName(), msg);
    }

    private final IPCManager mManager;
//    private SettingsListener settingsListener;
//    private RecorderListener recorderListener;
//    private PlayListener playListener;
//    private DeviceStatusListener deviceStatusListener;

    private IPCamera getCamera(long userId) {
        return mManager.getCamera(userId);
    }

    private PublishSubject<IPCEventData> subjectState = PublishSubject.create();
    private PublishSubject<IPCAudioFrame> subjectAudioFrame = PublishSubject.create();
    private PublishSubject<IPCVideoFrame> subjectVideoFrame = PublishSubject.create();
    private PublishSubject<IPCAlarm> subjectAlarm = PublishSubject.create();
    private PublishSubject<IPCGetParameter> subjectGetParam = PublishSubject.create();
    private PublishSubject<IPCSetParameter> subjectSetParam = PublishSubject.create();

    private void callback(Runnable runnable) {
        runnable.run();
    }

    public HSLEventController(IPCManager manager) {
        mManager = manager;
    }

    public void CallBack_SnapShot(final long UserID, final byte[] buff, final int len) {
    }

    public void CallBack_GetParam(final long UserID, final long nType, final String param) {
        subjectGetParam.onNext(new IPCGetParameter() {
            @Override
            public int getParamType() {
                return new Long(nType).intValue();
            }

            @Override
            public Object getParamData() {
                return param;
            }

            @Override
            public IPCamera getCamera() {
                return HSLEventController.this.getCamera(UserID);
            }
        });
//        callback(new Runnable() {
//            @Override
//            public void run() {
//                if (settingsListener != null)
//                    settingsListener.callBack_getParam(UserID, nType, param);
//            }
//        });
    }

    public void CallBack_SetParam(final long UserID, final long nType, final int nResult) {
        subjectSetParam.onNext(new IPCSetParameter() {
            @Override
            public int getParamType() {
                return new Long(nType).intValue();
            }

            @Override
            public Object getResult() {
                return nResult;
            }

            @Override
            public IPCamera getCamera() {
                return HSLEventController.this.getCamera(UserID);
            }
        });
//        callback(new Runnable() {
//            @Override
//            public void run() {
//                if (settingsListener != null)
//                    settingsListener.callBack_setParam(UserID, nType, nResult);
//            }
//        });
    }

    public void CallBack_Event(final long UserID, long nType) {
        final IPCamera cam;
        final int status = new Long(nType).intValue();
        if ((cam = getCamera(UserID)) != null) {
            final HSLCameraClient cameraStatus = (HSLCameraClient) cam.getIpcContext();
            cameraStatus.setStatusCode(status);
            subjectState.onNext(new IPCEventData() {
                @Override
                public IPCamera getCamera() {
                    return HSLEventController.this.getCamera(UserID);
                }
            });
        }
//
//        final int status = new Long(nType).intValue();
//        final IPCamera cam;
//        if ((cam = mManager.getCamera(UserID)) != null) {
//            final HSLCameraClient cameraStatus = (HSLCameraClient) cam.getIpcContext();
//            cameraStatus.setStatusCode(status);
//            if (status != 100) {
//                cameraStatus.isPlaying = false;
//            }
//            trace("camera " + cam.getDeviceId() + " state changed to " + status);
//            if (status == 101 || status == 10 || status == 11 || status == 9 || status == 2) {
//                synchronized (cameraStatus) {
//                    if (cameraStatus.reconnecting) return;
//                    cameraStatus.reconnecting = true;
//                }
//                new AsyncTask<IPCamera, Void, Void>() {
//                    @Override
//                    protected Void doInBackground(IPCamera... params) {
//                        try {
//                            Thread.sleep(5000);
//                            mManager.removeCamera(params[0]);
//                            mManager.addCamera(params[0]);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        } finally {
//                            cameraStatus.reconnecting = false;
//                        }
//                        return null;
//                    }
//                }.execute(cam);
//            }
//        }
//        callback(new Runnable() {
//            @Override
//            public void run() {
//                if (deviceStatusListener != null)
//                    deviceStatusListener.receiveDeviceStatus(UserID, status);
//            }
//        });
    }

    public void VideoData(long UserID, byte[] VideoBuf, int h264Data, int nLen, int Width, int Height, int time) {

    }

    public void callBackAudioData(final long nUserID, final byte[] pcm, final int size) {
        subjectAudioFrame.onNext(new IPCAudioFrame() {
            @Override
            public byte[] getPcm() {
                return pcm;
            }

            @Override
            public int getPcmSize() {
                return size;
            }

            @Override
            public IPCamera getCamera() {
                return HSLEventController.this.getCamera(nUserID);
            }
        });
    }

    public void CallBack_RecordFileList(final long UserID, final int filecount, final String fname, final String strDate, final int size) {
//        callback(new Runnable() {
//            @Override
//            public void run() {
//                if (settingsListener != null)
//                    settingsListener.recordFileList(UserID, filecount, fname, strDate, size);
//            }
//        });
    }

    public void CallBack_P2PMode(long UserID, int nType) {
    }

    public void CallBack_RecordPlayPos(long userid, int pos) {
    }

    public void CallBack_VideoData(final long UserID,final byte[] data,final int type,final int size) {
        subjectVideoFrame.onNext(new IPCVideoFrame() {
            @Override
            public byte[] getFrameData() {
                return data;
            }

            @Override
            public int getFrameType() {
                return type;
            }

            @Override
            public int getFrameSize() {
                return size;
            }

            @Override
            public IPCamera getCamera() {
                return HSLEventController.this.getCamera(UserID);
            }
        });
    }

    public void CallBack_AlarmMessage(final long UserID,final int nType) {
        subjectAlarm.onNext(new IPCAlarm() {
            @Override
            public int getAlarmType() {
                return nType;
            }

            @Override
            public IPCamera getCamera() {
                return HSLEventController.this.getCamera(UserID);
            }
        });
    }

    public void showNotification(String message, BluetoothClass.Device device, int nType) {
    }

//    void setDeviceStatusListener(DeviceStatusListener listener) {
//        deviceStatusListener = listener;
//    }
//
//    void setPlayListener(PlayListener listener) {
//        playListener = listener;
//    }
//
//    void setRecorderListener(RecorderListener listener) {
//        recorderListener = listener;
//    }
//
//    void setSettingsListener(SettingsListener listener) {
//        settingsListener = listener;
//    }

    //=============IPCEventController====================
    @Override
    public Subscription subscribeCameraStatus(Action1<IPCEventData> onNext) {
        return subjectState.subscribe(onNext);
    }

    @Override
    public Subscription subscribeIPCAudioFrame(Action1<IPCAudioFrame> callback) {
        return subjectAudioFrame.subscribe(callback);
    }

    @Override
    public Subscription subscribeIPCVideoFrame(Action1<IPCVideoFrame> callback) {
        return subjectVideoFrame.subscribe(callback);
    }

    @Override
    public Subscription subscribeGetParam(Action1<IPCGetParameter> callback) {
        return subjectGetParam.subscribe(callback);
    }

    @Override
    public Subscription subscribeSetParam(Action1<IPCSetParameter> callback) {
        return subjectSetParam.subscribe(callback);
    }

    @Override
    public Subscription subscribeAlarm(Action1<IPCAlarm> callback) {
        return subjectAlarm.subscribe(callback);
    }


}

