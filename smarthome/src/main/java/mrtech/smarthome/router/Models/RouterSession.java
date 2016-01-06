package mrtech.smarthome.router.Models;

import mrtech.smarthome.router.Router;
import rx.Subscription;
import rx.functions.Action1;

/**
 * 路由器连接会话。
 */
public interface RouterSession {
    int ROUTER_KEEP_ALIVE_DELAY = 30000;
    int ROUTER_RECONNECTION_DELAY = 5000;
    int ROUTER_ADD_PORT_DELAY = 10000;
    int ROUTER_AUTH_DELAY = 5000;

    /**
     * get sn decode result
     *
     * @return
     */
    boolean isSNValid();

    /**
     * get the status of connection
     *
     * @return
     */
    boolean isConnected();

    /**
     * get the status P2P port
     *
     * @return
     */
    boolean isPortValid();

    /**
     * get the status of communication permission
     *
     * @return
     */
    boolean isAuthenticated();

    /**
     * 会话是否初始化。
     *
     * @return
     */
    boolean isInitialized();

    /**
     * 获取路由器状态。
     *
     * @return
     */
    RouterStatus getRouterStatus();

    /**
     * 获取路由器基础配置信息。
     *
     * @param cache true 为使用缓存中的信息。
     * @return
     */
    mrtech.smarthome.rpc.Models.SystemConfiguration getRouterConfiguration(boolean cache);

    /**
     * 获取 路由器视频管理器。
     * @return
     */
    CameraManager getCameraManager();

    /**
     * 获取数据通道。
     *
     * @return
     */
    CommunicationManager getCommunicationManager();

    /**
     * 订阅路由器状态变更事件。
     *
     * @param callback 事件回调。
     * @return 事件订阅句柄。注意：在不使用事件的时候，需要调用Subscription.unsubscribe()注销事件。
     */
    Subscription subscribeRouterStatusChanged(Action1<Router> callback);

}
