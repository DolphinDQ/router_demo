package mrtech.smarthome.router;


import com.orm.SugarRecord;

import java.util.List;

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
    private RouterConfig config;
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

    public RouterConfig getConfig() {
        if (config == null) loadConfig();
        return config;
    }

    public void loadConfig() {
        final List<RouterConfig> routerConfigs = SugarRecord.find(RouterConfig.class, "sn = ?", SN);
        if (routerConfigs.size() == 0) {
            saveConfig();
        } else {
            config = routerConfigs.get(0);
        }
    }

    public void saveConfig() {
        if (config == null) {
            config = new RouterConfig(SN);
        }
        SugarRecord.save(config);
    }

    @Override
    public String toString() {
        return "Router:" + SN;
    }
}
