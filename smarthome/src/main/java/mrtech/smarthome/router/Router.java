package mrtech.smarthome.router;


import android.support.annotation.Nullable;

import com.orm.SugarRecord;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeoutException;

import mrtech.smarthome.router.Models.*;
import mrtech.smarthome.rpc.Messages;
import mrtech.smarthome.rpc.Models;
import mrtech.smarthome.util.RequestUtil;

/**
 * 路由器对象，主要包含：路由器基础数据，路由器配置（RouterConfig），路由器连接会话（RouterSession）三个部分
 * Created by sphynx on 2015/12/1.
 */
public class Router {
    private final String sn;
    private final String name;
    private HashMap<String, Object> userData = new HashMap<>();
    private RouterConfig config;
    private RouterSession routerSession;

    public RouterSession getRouterSession() {
        return routerSession;
    }


    /**
     * 构建路由器对象。
     *
     * @param name 路由器初始化名称。
     * @param sn   路由器序列码。
     */
    public Router(String name, String sn) {
        if (sn == null || sn.equals(""))
            throw new IllegalArgumentException("参数sn不能为空。");
        this.sn = sn;
        this.name = name;
    }

    /**
     * 获取路由器序列码。
     *
     * @return sn 序列码。
     */
    public String getSn() {
        return sn;
    }

    /**
     * 获取路由器名称，如果路由器已经连接则返回路由器名称，如果未连接则返回对象初始化名称。
     *
     * @return
     */
    public String getName() {
        final RouterSession routerSession = getRouterSession();
        if (routerSession != null && routerSession.isAuthenticated()) {
            final Models.SystemConfiguration routerConfiguration;
            try {
                routerConfiguration = routerSession
                        .getCommunicationManager()
                        .postRequest(RequestUtil.getSysConfig(), true)
                        .getExtension(Messages.GetSystemConfigurationResponse.response)
                        .getConfiguration();
                if (routerConfiguration != null) {
                    final String deviceName = routerConfiguration.getDeviceName();
                    if (!deviceName.equals(getConfig().getName())) {
                        getConfig().setName(deviceName);
                        saveConfig();
                    }
                    return getConfig().getName();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return name;
    }

    /**
     * 获取用户数据，通过setUserData方法可以设置指定对象到Router数据列表。使用此方法可以将数据取出。
     *
     * @return 用户数据。
     */
    public Object getUserData(String key) {
        return userData.get(key);
    }

    /**
     * 获取用户数据，通过setUserData方法可以设置指定对象到Router数据列表。使用此方法可以将数据取出。
     *
     * @param cls 指定要存储的类型。
     * @param <T> 指定类型。
     * @return 要获取的数据。
     */
    @SuppressWarnings("unchecked")
    public <T> T getUserData(Class<T> cls) {
        return (T) getUserData(cls.getName());
    }

    /**
     * 设置指定数据到路由器数据列表中，即绑定数据。（用于绑定UI数据或数据库数据）
     * 使用类的名称作为数据的key。
     *
     * @param cls    指定要存储的类型。
     * @param source 需要绑定的数据。
     * @param <T>    指定类型。
     */
    public <T> void setUserData(Class<T> cls, @Nullable T source) {
        setUserData(cls.getName(), source);
    }

    /**
     * 设置指定数据到路由器数据列表中，即绑定数据。（用于绑定UI数据或数据库数据）
     *
     * @param key    数据指定的key，key是唯一，重复key会互相覆盖。
     * @param source 需要绑定的数据，如果source为null则删除指定key的数据对象。
     */
    public void setUserData(String key, @Nullable Object source) {
        if (userData.containsKey(key))
            userData.remove(key);
        if (source != null)
            userData.put(key, source);
    }

    /**
     * 获取路由器配置文件。
     *
     * @return 配置文件。
     */
    public RouterConfig getConfig() {
        if (config == null) loadConfig();
        return config;
    }

    void loadConfig() {
        final List<RouterConfig> routerConfigs = SugarRecord.find(RouterConfig.class, "sn = ?", sn);
        if (routerConfigs.size() == 0) {
            saveConfig();
        } else {
            config = routerConfigs.get(0);
        }
    }

    void saveConfig() {
        if (config == null) {
            config = new RouterConfig(sn);
        }
        SugarRecord.save(config);
    }

    void setRouterSession(RouterSession routerSession) {
        this.routerSession = routerSession;
    }

    @Override
    public String toString() {
        return "Router:" + sn;
    }
}
