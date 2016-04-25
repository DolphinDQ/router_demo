package mrtech.smarthome.ipc.Models;

/**
 * 执行获取IPC参数操作后的回调信息
 */
public interface IPCGetParameter extends IPCEventData {
    /**
     * 获取参数类型
     * @return 参数类型
     */
    int getParamType();

    /**
     * 获取参数数据
     * @return 参数数据
     */
    Object getParamData();
}
