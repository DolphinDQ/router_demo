package mrtech.smarthome.ipc.Models;

/**
 * IPC报警信息
 */
public interface IPCAlarm extends IPCEventData {
    /**
     * 获取报警类型
     * @return 报警类型
     */
    int getAlarmType();
}
