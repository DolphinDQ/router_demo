package mrtech.smarthome.ipc.Models;


/**
 * Created by sphynx on 2015/12/8.
 */
public enum IPCStatus {
    /**
     * 初始化
     */
    UNKNOWN("未知状态"),
    /**
     * 正在连接
     */
    CONNECTING("正在连接"),
    /**
     * 在线
     */
    CONNECTED("设备在线"),
    /**
     * 连接失败
     */
    CONNECT_ERROR("连接失败"),
    /**
     * 用户名密码错误
     */
    ERROR_USER_PWD("验证失败"),
    /**
     * 超过最大可连接用户数
     */
    ERROR_MAX_USER("连接数满"),
    /**
     * 视频丢失
     */
    ERROR_VIDEO_LOST("视频丢失"),
    /**
     * 不可用的设备ID
     */
    ERROR_INVALID_ID("ID不可用"),
    /**
     * 设备不在线。
     */
    DEVICE_OFFLINE("设备离线"),
    /**
     * 连接超时
     */
    CONNECT_TIMEOUT("连接超时"),
    /**
     * 断开连接
     */
    DISCONNECT("断开连接"),
    /**
     * 校验用户账号
     */
    CHECK_ACCOUNT("正在验证");

    private final String description;

    private IPCStatus(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }
}