package mrtech.smarthome.router;

import android.os.AsyncTask;

import java.util.concurrent.TimeoutException;

import mrtech.smarthome.interf.ResponseThreadListener;
import mrtech.smarthome.rpc.Messages;
import mrtech.smarthome.rpc.Messages.Response;
import mrtech.smarthome.rpc.Messages.Request;

/**
 * router model object
 * Created by sphynx on 2015/12/1.
 */
public class Router {
    private final String SN;
    private final Object Source;
    private RouterContext Context;

    public RouterContext getContext() {
        return Context;
    }

    public void setContext(RouterContext context) {
        if (context != null)
            Context = context;
    }

    /**
     * create router object
     *
     * @param source source data
     * @param sn     router serial number
     */
    public Router(Object source, String sn) {
        SN = sn;
        Source = source;
    }

    /**
     * get router serial number
     *
     * @return sn
     */
    public String getSN() {
        return SN;
    }

    /**
     * get router source data
     *
     * @return data
     */
    public Object getSource() {
        return Source;
    }

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
        void addRequest(Request request);

        /**
         * add a request to router request queue , and waiting for response .
         *
         * @param request
         * @param timeout MILLISECONDS
         * @return
         * @throws TimeoutException
         */
        Response addRequestSync(Request request, int timeout) throws TimeoutException;

        /**
         * add a request to router request queue , and waiting for response .default timeout RouterManager.ROUTER_REQUEST_TIMEOUT
         *
         * @param request
         * @return
         * @throws TimeoutException
         */
        Response addRequestSync(Request request) throws TimeoutException;

        /**
         * set the router callback message listener
         *
         * @param listener
         */
        void setResponseListener(ResponseThreadListener listener);
    }

    @Override
    public String toString() {
        return "Router:" + SN;
    }
}
