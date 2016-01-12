package mrtech.smarthome.ipc;

import rx.Subscription;
import rx.functions.Action1;
import mrtech.smarthome.ipc.IPCModels.*;

/**
 * Created by sphynx on 2015/12/8.
 */
public interface IPCEventManager {

    /**
     * 订阅IPC状态。
     * @param callback IPC状态发送变化时回调。
     * @return 订阅句柄，句柄在事件不再使用时候，必须执行反订阅（即unsubscribe）。
     */
    Subscription subscribeCameraStatus(Action1<IPCStateChanged> callback);

    /**
     *订阅IPC音频帧。
     * @param callback 接收到音频帧时回调。
     * @return 订阅句柄，句柄在事件不再使用时候，必须执行反订阅（即unsubscribe）。
     */
    Subscription subscribeIPCAudioFrame(Action1<IPCAudioFrame> callback);

    /**
     * 订阅IPC视频帧。
     * @param callback 接收到视频侦时回调。
     * @return 订阅句柄，句柄在事件不再使用时候，必须执行反订阅（即unsubscribe）。
     */
    Subscription subscribeIPCVideoFrame(Action1<IPCVideoFrame> callback);

//    /**
//     * 订阅IPC获取参数回调。
//     * @param callback 接收到IPC设置参数时回调。
//     * @return 订阅句柄，句柄在事件不再使用时候，必须执行反订阅（即unsubscribe）。
//     */
//    Subscription subscribeGetParam(Action1<IPCGetParameter> callback);
//
//    /**
//     * 订阅IPC设置参数回调。
//     * @param callback 接收IPC
//     * @return 订阅句柄，句柄在事件不再使用时候，必须执行反订阅（即unsubscribe）。
//     */
//    Subscription subscribeSetParam(Action1<IPCSetParameter> callback);
//
//    /**
//     * @param callback
//     * @return 订阅句柄，句柄在事件不再使用时候，必须执行反订阅（即unsubscribe）。
//     */
//    Subscription subscribeAlarm(Action1<IPCAlarm> callback);
}
