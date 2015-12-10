package mrtech.smarthome.router;

import java.util.concurrent.TimeoutException;

import mrtech.smarthome.interf.ResponseThreadListener;
import mrtech.smarthome.rpc.Messages;
import rx.functions.Action1;

/**
 * Created by sphynx on 2015/12/9.
 */
public class Models {

    /**
     * router running context
     */
    public interface RouterContext {
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
         * add a request to router request queue.
         *
         * @param request
         */
        void addRequest(Messages.Request request,Action1<Messages.Response> callback);

        /**
         * add a request to router request queue , and waiting for response .
         *
         * @param request
         * @param timeout MILLISECONDS
         * @return
         * @throws TimeoutException
         */
        Messages.Response addRequestSync(Messages.Request request, int timeout) throws TimeoutException;

        /**
         * add a request to router request queue , and waiting for response .default timeout RouterManager.ROUTER_REQUEST_TIMEOUT
         *
         * @param request
         * @return
         * @throws TimeoutException
         */
        Messages.Response addRequestSync(Messages.Request request) throws TimeoutException;

        /**
         * set the router callback message listener
         *
         * @param listener
         */
        void setResponseListener(ResponseThreadListener listener);
    }
}
