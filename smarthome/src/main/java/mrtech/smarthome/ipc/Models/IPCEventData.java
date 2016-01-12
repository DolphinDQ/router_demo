package mrtech.smarthome.ipc.Models;


/**
 * IPC事件数据。
 */
public interface IPCEventData {
    /**
     * IPC连接句柄。
     *
     * @return 连接句柄。
     */
    long getCameraId();
}