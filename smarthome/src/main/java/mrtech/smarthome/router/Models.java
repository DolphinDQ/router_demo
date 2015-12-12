package mrtech.smarthome.router;

import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import mrtech.smarthome.rpc.Messages;
import rx.functions.Action2;

/**
 * Created by sphynx on 2015/12/9.
 */
public class Models {

    /**
     * router running session context
     */
    public interface RouterSession {
        int ROUTER_REQUEST_TIMEOUT = 2000;
        int ROUTER_KEEP_ALIVE_DELAY = 20000;
        int ROUTER_RECONNECTION_DELAY = 5000;
        int ROUTER_ADD_PORT_DELAY = 5000;
        int ROUTER_AUTH_DELAY = 5000;
        int ROUTER_READ_INTERVAL = 1000;

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
         * 获取路由器状态。
         *
         * @return
         */
        RouterStatus getRouterStatus();

        /**
         * 异步向路由器发送请求，默认请求超时为 RouterSession.ROUTER_REQUEST_TIMEOUT,不启用缓存。
         *
         * @param request  请求包
         * @param callback 请求回调
         */
        void postRequestAsync(Messages.Request request, Action2<Messages.Response, Throwable> callback);

        /**
         * 异步向路由器发送请求，默认请求超时为 RouterSession.ROUTER_REQUEST_TIMEOUT
         *
         * @param request  请求包
         * @param callback 请求回调
         * @param cache    是否启用缓存。true启用缓存，则首次发送会网络请求给服务器。
         */
        void postRequestAsync(Messages.Request request, Action2<Messages.Response, Throwable> callback, boolean cache);

        /**
         * 异步向路由器发送请求，默认不启用缓存。
         *
         * @param request  请求包
         * @param callback 请求回调
         * @param timeout  请求超时，毫秒
         */
        void postRequestAsync(Messages.Request request, Action2<Messages.Response, Throwable> callback, int timeout);

        /**
         * 异步向路由器发送请求。
         *
         * @param request  请求包
         * @param callback 请求回调
         * @param timeout  请求超时，毫秒
         * @param cache    是否启用缓存。true启用缓存，则首次发送会网络请求给服务器。
         */
        void postRequestAsync(Messages.Request request, Action2<Messages.Response, Throwable> callback, int timeout, boolean cache);

        /**
         * 向路由器发送一个请求。默认不启用缓存。.
         *
         * @param request 用户请求。
         * @param timeout 超时时间。毫秒
         * @return
         * @throws TimeoutException
         */
        Messages.Response postRequest(Messages.Request request, int timeout) throws TimeoutException;

        /**
         * 向路由器发送一个请求。
         *
         * @param request 用户请求。
         * @param timeout 超时时间。毫秒
         * @param cache   是否启用缓存。true启用缓存，则首次发送会网络请求给服务器。
         * @return
         * @throws TimeoutException
         */
        Messages.Response postRequest(Messages.Request request, int timeout, boolean cache) throws TimeoutException;

        /**
         * 向路由器发送一个请求，默认请求超时为 RouterSession.ROUTER_REQUEST_TIMEOUT,不启用缓存。
         *
         * @param request 用户请求。
         * @return
         * @throws TimeoutException
         */
        Messages.Response postRequest(Messages.Request request) throws TimeoutException;

        /**
         * 向路由器发送一个请求，默认请求超时为 RouterSession.ROUTER_REQUEST_TIMEOUT
         *
         * @param request 用户请求。
         * @param cache   是否启用缓存。true启用缓存，则首次发送会网络请求给服务器。
         * @return
         * @throws TimeoutException
         */
        Messages.Response postRequest(Messages.Request request, boolean cache) throws TimeoutException;

        /**
         * 获取路由器基础配置信息。
         *
         * @param cache true 为使用缓存中的信息。
         * @return
         */
        Messages.GetSystemConfigurationResponse getRouterConfiguration(boolean cache);
    }

    public enum RouterStatus {
        CREATED("未初始化"),
        INITIALIZED("初始化完毕"),
        SN_DECODING( "序列码解析中"),
        SN_INVALID("序列码无效"),
        SN_DECODED("序列码解析成功"),
        P2P_CONNECTING("P2P连接中"),
        P2P_DISCONNECTED("P2P连接失败"),
        P2P_CONNECTED("P2P连接成功"),
        ROUTER_CONNECTING( "路由器连接中"),
        ROUTER_DISCONNECTED( "路由器连接失败"),
        ROUTER_CONNECTED("路由器已连接"),
        API_AUTH( "通讯授权中"),
        API_UNAUTHORIZED( "通讯未授权"),
        API_AUTH_SUCCESS( "通讯授权成功");

        private final String description;
        RouterStatus( String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return description;
        }
    }
}
