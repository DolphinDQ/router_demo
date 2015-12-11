package mrtech.smarthome.router;


import mrtech.smarthome.router.Models.*;
/**
 * router model object
 * Created by sphynx on 2015/12/1.
 */
public class Router {
    private final String SN;
    private final Object Source;
    private RouterSession routerSession;

    public RouterSession getRouterSession() {
        return routerSession;
    }

    void setRouterSession(RouterSession routerSession) {
        if (routerSession != null)
            this.routerSession = routerSession;
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

    @Override
    public String toString() {
        return "Router:" + SN;
    }
}
