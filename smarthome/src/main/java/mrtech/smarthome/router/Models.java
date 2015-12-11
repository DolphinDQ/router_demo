package mrtech.smarthome.router;

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
         * 发送请求，默认超时为 ROUTER_READ_INTERVAL
         *
         * @param request  请求包
         * @param callback 请求回调
         */
        void postRequestAsync(Messages.Request request,Action2<Messages.Response,Throwable> callback);

        /**
         * 发送请求
         *
         * @param request 请求包
         * @param callback 请求回调
         * @param timeout 请求超时，毫秒
         */
        void postRequestAsync(Messages.Request request, Action2<Messages.Response,Throwable> callback, int timeout);

        /**
         * add a request to router request queue , and waiting for response .
         *
         * @param request
         * @param timeout MILLISECONDS
         * @return
         * @throws TimeoutException
         */
        Messages.Response postRequest(Messages.Request request, int timeout) throws TimeoutException;

        /**
         * add a request to router request queue , and waiting for response .default timeout RouterManager.ROUTER_REQUEST_TIMEOUT
         *
         * @param request
         * @return
         * @throws TimeoutException
         */
        Messages.Response postRequest(Messages.Request request) throws TimeoutException;


    }
}
