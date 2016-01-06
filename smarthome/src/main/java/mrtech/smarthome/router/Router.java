package mrtech.smarthome.router;


import com.orm.SugarRecord;

import java.util.HashMap;
import java.util.List;

import mrtech.smarthome.router.Models.*;

/**
 * router model object
 * Created by sphynx on 2015/12/1.
 */
public class Router {
    private final String sn;
    private final String name;
    private HashMap<String,Object> userData =new HashMap<>();
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
    public Router(String name, String sn) {
        this.sn = sn;
        this.name = name;

    }

    /**
     * get router serial number
     *
     * @return sn
     */
    public String getSn() {
        return sn;
    }

    public String getName() {
        final RouterSession routerSession = getRouterSession();
        if (routerSession != null && routerSession.isAuthenticated()) {
            final mrtech.smarthome.rpc.Models.SystemConfiguration routerConfiguration = routerSession.getRouterConfiguration(true);
            if (routerConfiguration != null)
                return routerConfiguration.getDeviceName();
        }
        return name;
    }

    /**
     * get router source data
     *
     * @return data
     */
    public Object getSource(String key) {
        return  userData.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T getSource(Class<T> cls){
        return (T)getSource(cls.getName());
    }

    public <T> void setSource(Class<T> cls,T source){
        setSource(cls.getName(),source);
    }

    public void setSource(String key, Object source) {
        userData.put(key,source);
    }

    public RouterConfig getConfig() {
        if (config == null) loadConfig();
        return config;
    }

    public void loadConfig() {
        final List<RouterConfig> routerConfigs = SugarRecord.find(RouterConfig.class, "sn = ?", sn);
        if (routerConfigs.size() == 0) {
            saveConfig();
        } else {
            config = routerConfigs.get(0);
        }
    }

    public void saveConfig() {
        if (config == null) {
            config = new RouterConfig(sn);
        }
        SugarRecord.save(config);
    }

    @Override
    public String toString() {
        return "Router:" + sn;
    }
}
