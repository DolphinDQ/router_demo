package mrtech.smarthome.ipc.Models;

/**
 * IPC状态改变回调数据
 */
public interface IPCStateChanged extends IPCEventData {
    /**
     * 获取IPC即将改变的状态
     * @return IPC状态
     */
    IPCStatus getStatus();
}
