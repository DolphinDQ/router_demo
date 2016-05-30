package mrtech.smarthome.ipc;
import mrtech.smarthome.ipc.Models.IPCStatus;
import rx.Subscription;
import rx.functions.Action1;

/**
 * IPC状态描述
 */
public interface IPCContext {
    /**
     * 获取IPC连接句柄
     * @return 连接句柄
     */
    long getCameraId();

    /**
     * IPC当前状态
     * @return IPCStatus
     */
    IPCStatus getStatus();

    /**
     * IPC是否处于播放状态
     * @return true为是
     */
    boolean isPlaying();

    /**
     * IPC是否处于重连状态
     * @return true为是
     */
    boolean isReconnecting();

    /**
     * 销毁IPC连接会话
     */
    void destroy();

    /**
     * 初始化IPC连接会话，即重新连接
     */
    void init();

    /**
     * 订阅IPC播放状态变化事件
     * @param callback 播放状态变化回调
     * @return 订阅句柄，句柄在事件不再使用时候，必须执行反订阅（即unsubscribe）
     */
    Subscription subscribePlayStatus(Action1<IPCContext> callback);

    /**
     * 订阅IPC重连回调
     * @param callback IPC重连回调
     * @return 订阅句柄，句柄在事件不再使用时候，必须执行反订阅（即unsubscribe）
     */
    Subscription subscribeReconnectionStatus(Action1<IPCContext> callback);
}