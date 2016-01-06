package mrtech.smarthome.router.Models;


/**
 * 路由器通讯状态。
 */
public enum RouterStatus {
    INITIAL("未初始化"),
    INITIALIZED("初始化完毕"),
    SN_DECODING("序列码解析中"),
    SN_INVALID("序列码无效"),
    SN_DECODED("序列码解析成功"),
    P2P_CONNECTING("P2P连接中"),
    P2P_DISCONNECTED("P2P连接失败"),
    P2P_CONNECTED("P2P连接成功"),
    ROUTER_CONNECTING("路由器连接中"),
    ROUTER_DISCONNECTED("路由器连接失败"),
    ROUTER_CONNECTED("路由器已连接"),
    API_AUTH("通讯授权中"),
    API_UNAUTHORIZED("通讯未授权"),
    API_AUTH_SUCCESS("通讯授权成功");

    private final String description;

    RouterStatus(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }
}