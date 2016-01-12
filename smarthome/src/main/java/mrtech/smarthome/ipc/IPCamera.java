package mrtech.smarthome.ipc;

import mrtech.smarthome.ipc.Models.*;

/**
 * IP camera object
 * Created by zdqa1 on 2015/11/25.
 */
public class IPCamera {

    /**
     * 构建IPC对象
     *
     * @param tag      用户数据，作为与上层应用标识Camera的字段。
     * @param deviceId IPC P2P连接字符串。
     * @param userName 登录IPC的用户。
     * @param password 登录密码。
     */
    public IPCamera(Object tag, String deviceId, String userName, String password) {
        this.tag = tag;
        this.deviceId = deviceId;
        this.userName = userName;
        this.password = password;
    }

    /**
     * 获取IPC绑定数据。
     *
     * @return 版定的数据。
     */
    public Object getTag() {
        return tag;
    }

    /**
     * 获取设备ID。
     *
     * @return 设备ID。
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * 获取登录账户。
     *
     * @return 登录账户。
     */
    public String getUserName() {
        return userName;
    }

    /**
     * 获取登录密码。
     *
     * @return 登陆密码。
     */
    public String getPassword() {
        return password;
    }

    /**
     * 获取IPC运行上下文。
     *
     * @return 运行上下文。
     */
    public IPCContext getIpcContext() {
        return ipcContext;
    }

    void setIpcContext(IPCContext ipcContext) {
        this.ipcContext = ipcContext;
    }

    /**
     * user data
     */
    private final Object tag;
    /**
     * P2P connection string(deviceId)
     */
    private final String deviceId;
    /**
     * login user
     */
    private final String userName;
    /**
     * login password
     */
    private final String password;
    /**
     * camera status
     */
    private IPCContext ipcContext;

}
