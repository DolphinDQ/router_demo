package mrtech.smarthome.router.Models;

import mrtech.smarthome.router.Router;
import rx.Subscription;
import rx.functions.Action1;

/**
 * 路由器连接会话
 */
public interface RouterSession {
    /**
     * 路由器发送Keep alive 数据包时间间隔。（毫秒）
     */
    int ROUTER_KEEP_ALIVE_DELAY = 30000;
    /**
     * 路由器连接失败重连时间间隔。（毫秒）
     */
    int ROUTER_RECONNECTION_DELAY = 5000;
    /**
     * 路由器映射端口失败重新映射时间间隔。（毫秒）
     */
    int ROUTER_ADD_PORT_DELAY = 10000;
    /**
     * 路由器登录验证失败重新验证时间间隔。（毫秒）
     */
    int ROUTER_AUTH_DELAY = 5000;

    /**
     * 获取路由器序列号是否有效
     * @return true为有效
     */
    boolean isSNValid();

    /**
     * 获取路由器是否已经连接
     * @return true为已连接
     */
    boolean isConnected();

    /**
     * 获取路由器是否已经建立陪p2p通道
     * @return true为已经建立通道
     */
    boolean isPortValid();

    /**
     * 获取路由器通讯授权
     * @return true为取得路由器通讯授权
     */
    boolean isAuthenticated();

    /**
     * 强制将会话重新连接
     */
    void  reconnect();

    /**
     * 获取路由器状态
     * @return 路由器当前连接状态
     */
    RouterStatus getRouterStatus();

    /**
     * 获取当前路由器的摄像头管理
     * @return 路由器的摄像头管理
     */
    CameraDataManager getCameraManager();

    /**
     * 获取当前路由器的通讯管理器，用于收发路由器数据
     * @return 通讯管理器
     */
    CommunicationManager getCommunicationManager();

    /**
     * 订阅路由器状态变更事件
     * @param callback 事件回调
     * @return 事件订阅句柄。注意：在不使用事件的时候，需要调用Subscription.unsubscribe()注销事件
     */
    Subscription subscribeRouterStatusChanged(Action1<Router> callback);

}
