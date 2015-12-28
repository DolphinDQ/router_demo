package mrtech.smarthome.ipc;

import android.bluetooth.BluetoothClass;
import android.util.Log;

import mrtech.smarthome.ipc.IPCModels.*;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

/**
 * Created by sphynx on 2015/12/8.
 */
class HSLEventController implements IPCEventController {
    private static void trace(String msg) {
        Log.e(IPCManager.class.getName(), msg);
    }
    public static IPCStatus getStatus(int code) {
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

    private IPCamera getCamera(long userId) {
        return IPCManager.getInstance().getCamera(userId);
    }


    private PublishSubject<IPCAlarm> subjectAlarm = PublishSubject.create();
    private PublishSubject<IPCStateChanged> subjectState = PublishSubject.create();
    private PublishSubject<IPCAudioFrame> subjectAudioFrame = PublishSubject.create();
    private PublishSubject<IPCVideoFrame> subjectVideoFrame = PublishSubject.create();
    private PublishSubject<IPCGetParameter> subjectGetParam = PublishSubject.create();
    private PublishSubject<IPCSetParameter> subjectSetParam = PublishSubject.create();

    private void callback(Runnable runnable) {
        runnable.run();
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
            public long getCameraId() {
                return UserID;
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
            public long getCameraId() {
                return UserID;
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
        trace("callback status id=" + UserID + " type=" + status);
        subjectState.onNext(new IPCStateChanged() {
            @Override
            public IPCStatus getStatus() {
                return HSLEventController.getStatus(status);
            }
            @Override
            public long getCameraId() {
                return UserID;
            }
        });
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
            public long getCameraId() {
                return nUserID;
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

    public void CallBack_VideoData(final long UserID, final byte[] data, final int type, final int size) {
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
            public long getCameraId() {
                return UserID;
            }
        });
    }

    public void CallBack_AlarmMessage(final long UserID, final int nType) {
        subjectAlarm.onNext(new IPCAlarm() {
            @Override
            public int getAlarmType() {
                return nType;
            }

            @Override
            public long getCameraId() {
                return UserID;
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
    public Subscription subscribeCameraStatus(Action1<IPCStateChanged> onNext) {
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

