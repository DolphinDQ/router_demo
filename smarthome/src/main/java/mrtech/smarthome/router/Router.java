package mrtech.smarthome.router;


import android.util.Log;

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
    private final String Name;
    private Object Source;
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
            final mrtech.smarthome.rpc.Models.SystemConfiguration routerConfiguration = routerSession.getRouterConfiguration(true);
            if (routerConfiguration != null)
                return routerConfiguration.getDeviceName();
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

    public void setSource(Object source) {
        Source = source;
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
        long id = SugarRecord.save(config);
        Log.d("idididi!!!!!","id:"+id);
//        config.id = id;
    }

    @Override
    public String toString() {
        return "Router:" + SN;
    }
}
