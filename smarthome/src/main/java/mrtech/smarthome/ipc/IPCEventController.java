package mrtech.smarthome.ipc;

import rx.Subscription;
import rx.functions.Action1;
import mrtech.smarthome.ipc.IPCModels.*;

/**
 * Created by sphynx on 2015/12/8.
 */
public interface IPCEventController {
    Subscription subscribeCameraStatus(Action1<IPCEventData> callback);
    Subscription subscribeIPCAudioFrame(Action1<IPCAudioFrame> callback);
    Subscription subscribeIPCVideoFrame(Action1<IPCVideoFrame> callback);
    Subscription subscribeGetParam(Action1<IPCGetParameter> callback);
    Subscription subscribeSetParam(Action1<IPCSetParameter> callback);
    Subscription subscribeAlarm(Action1<IPCAlarm> callback);
}
