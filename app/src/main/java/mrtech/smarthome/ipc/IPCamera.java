package mrtech.smarthome.ipc;

import mrtech.smarthome.ipc.IPCModels.*;

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

    public Object getTag() {
        return tag;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public IPCContext getIpcContext() {
        return ipcContext;
    }

    void setIpcContext(IPCContext ipcContext) {
        if (ipcContext != null) return;
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
