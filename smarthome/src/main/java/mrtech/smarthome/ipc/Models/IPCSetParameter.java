package mrtech.smarthome.ipc.Models;

/**
 * 执行设置IPC参数操作后的回调信息
 */
public interface IPCSetParameter extends IPCEventData {
    /**
     * 获取参数类型
     * @return 参数类型
     */
    int getParamType();

    /**
     * 获取返回结果
     * @return 返回结果
     */
    Object getResult();
}
