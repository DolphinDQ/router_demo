package mrtech.smarthome.router;


import java.util.concurrent.TimeoutException;

import mrtech.smarthome.router.Models.*;
import mrtech.smarthome.rpc.Messages;

/**
 * router model object
 * Created by sphynx on 2015/12/1.
 */
public class Router {
    private final String SN;
    private final Object Source;
    private final String Name;
    private RouterSession routerSession;

    public RouterSession getRouterSession() {
        return routerSession;
    }

    void setRouterSession(RouterSession routerSession) {
        this.routerSession = routerSession;
    }

    /**
     * create router object
     *
     * @param source source data
     * @param sn     router serial number
     */
    public Router(Object source, String name, String sn) {
        SN = sn;
        Source = source;
        Name = name;
    }

    /**
     * get router serial number
     *
     * @return sn
     */
    public String getSN() {
        return SN;
    }

    public String getName() {
        final RouterSession routerSession = getRouterSession();
        if (routerSession != null && routerSession.isAuthenticated()) {
            final Messages.GetSystemConfigurationResponse routerConfiguration = routerSession.getRouterConfiguration(false);
            if (routerConfiguration != null)
                return routerConfiguration.getConfiguration().getDeviceName();
        }
        return Name;
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
