package mrtech.smarthome.router;

import android.content.Context;
import android.util.Log;

import com.google.protobuf.ExtensionRegistry;
import com.orm.SugarContext;
import com.orm.SugarRecord;
import com.stream.NewAllStreamParser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import mrtech.smarthome.ipc.IPCManager;
import mrtech.smarthome.rpc.Messages;
import mrtech.smarthome.rpc.Models;
import mrtech.smarthome.util.Constants;
import mrtech.smarthome.router.Models.*;
import rx.Subscription;
import rx.functions.Action1;
import rx.subjects.PublishSubject;

/**
 * router connection manager
 * Created by sphynx on 2015/12/1.
 */
public class RouterManager {

    private static void trace(String msg) {
        Log.e(RouterManager.class.getName(), msg);
    }

    public final static ExtensionRegistry registry = ExtensionRegistry.newInstance();
    private static Map<Messages.Response.ErrorCode, String> errorMessageMap;
    private final RouterEventManager mEventManager;

    private static RouterManager ourInstance = new RouterManager();
    /**
     * 创建路由器管理单例对象。
     *
     * @return
     */
    public static RouterManager getInstance() {
        return ourInstance;
    }

    private final PublishSubject<RouterCallback<Boolean>> subjectRouterCreate = PublishSubject.create();

    private static int mP2PHandle;

    private ArrayList<Router> mRouters;

    private RouterManager() {
        mRouters = new ArrayList<>();
        mEventManager = new RouterEventManager(this);
    }

    private static boolean isP2PInitialized() {
        return mP2PHandle != 0;
    }

    private static void initExtensionRegistry() {
        Messages.registerAllExtensions(registry);
        Models.registerAllExtensions(registry);
        trace("registry extensions...");
    }

    private static void initErrorMessageMap() {
        errorMessageMap = new HashMap<Messages.Response.ErrorCode, String>();
        errorMessageMap.put(Messages.Response.ErrorCode.UNKNOWN_PROTOCOL, "未知协议。");
        errorMessageMap.put(Messages.Response.ErrorCode.UNSUPPORTED_VERSION, "版本不支持。");
        errorMessageMap.put(Messages.Response.ErrorCode.SERVER_BUSY, "服务器忙，请稍后再试。");
        errorMessageMap.put(Messages.Response.ErrorCode.INVALID_API_KEY, "无效ApiKey。");
        errorMessageMap.put(Messages.Response.ErrorCode.INCORRECT_PASSWORD, "密码错误。");
        errorMessageMap.put(Messages.Response.ErrorCode.INVALID_ARGUMENT, "参数错误。");
        errorMessageMap.put(Messages.Response.ErrorCode.PACKET_SIZE_OVERFLOW, "数据包大小溢出。");
        errorMessageMap.put(Messages.Response.ErrorCode.DATABASE_ERROR, "服务器数据库错误。");
        errorMessageMap.put(Messages.Response.ErrorCode.ALREADY_ENROLLED, "已经申报了。");
        errorMessageMap.put(Messages.Response.ErrorCode.NOT_ZIGBEE_IAS_WIDGET, "设备无法注册未报警设备。");
        errorMessageMap.put(Messages.Response.ErrorCode.DUPLICATE_ALIAS, "别名重复。");
        errorMessageMap.put(Messages.Response.ErrorCode.NOT_READY_TO_ARM, "系统未能报警。");
        errorMessageMap.put(Messages.Response.ErrorCode.ALREADY_ARMED, "系统已经报警。");
        errorMessageMap.put(Messages.Response.ErrorCode.DUPLICATE_NAME, "用户名重复。");
        errorMessageMap.put(Messages.Response.ErrorCode.MAX_CONNECTIONS_ERROR, "超过最大连接数。");
        errorMessageMap.put(Messages.Response.ErrorCode.NOT_AUTHENTICATED, "没有授权。");
        errorMessageMap.put(Messages.Response.ErrorCode.ALREADY_AUTHENTICATED, "已经授权。");
        errorMessageMap.put(Messages.Response.ErrorCode.INVALID_CHARACTER, "无效字符。");
        errorMessageMap.put(Messages.Response.ErrorCode.INVALID_PHONE_NUMBER, "无效手机号码。");
        errorMessageMap.put(Messages.Response.ErrorCode.MAX_DIAL_NUMBER_EXCEEDED, "超过最大拨号数量。");
        errorMessageMap.put(Messages.Response.ErrorCode.MAX_SMS_RECIPIENT_EXCEEDED, "超过最大短信收件人。");
        errorMessageMap.put(Messages.Response.ErrorCode.TELEPHONY_SERVICE_BUSY, "电话服务忙。");
        errorMessageMap.put(Messages.Response.ErrorCode.SYSTEM_ARMED, "系统报警。");
        errorMessageMap.put(Messages.Response.ErrorCode.EZMODE_ON, "搜索zigbee设备。");
        errorMessageMap.put(Messages.Response.ErrorCode.PERMIT_JOIN_ON, "搜索zigbee设备。");
        errorMessageMap.put(Messages.Response.ErrorCode.WLAN_IN_USE, "wifi正在使用，不能关闭wifi。");
        errorMessageMap.put(Messages.Response.ErrorCode.NOT_AUTHORIZED, "没有授权。");
        errorMessageMap.put(Messages.Response.ErrorCode.BAD_CONFIG_FILE, "配置文件错误。");
        errorMessageMap.put(Messages.Response.ErrorCode.SIGNATURE_MISMATCH, "信号不匹配。");
        errorMessageMap.put(Messages.Response.ErrorCode.NO_UPDATE_AVAILABLE, "没有更新。");
        errorMessageMap.put(Messages.Response.ErrorCode.UPDATE_CHECK_FAILED, "检查更新错误。");
        errorMessageMap.put(Messages.Response.ErrorCode.CAMERA_NOT_ONLINE, "摄像头离线。");
        errorMessageMap.put(Messages.Response.ErrorCode.CAMERA_AUTHENTICATION_ERROR, "没有授权。登陆了不上，");
        errorMessageMap.put(Messages.Response.ErrorCode.CAMERA_INTERNAL_ERROR, "摄像头内部错误。");
        errorMessageMap.put(Messages.Response.ErrorCode.MAX_GROUPS_REACHED, "到达分组最大数量。");
        errorMessageMap.put(Messages.Response.ErrorCode.DUPLICATE_GROUP_NAME, "分组名重复。");
        errorMessageMap.put(Messages.Response.ErrorCode.GROUP_NOT_FOUND, "分组找不大。。");
        errorMessageMap.put(Messages.Response.ErrorCode.DEVICE_NOT_FOUND, "不存在该设备。");
        errorMessageMap.put(Messages.Response.ErrorCode.ON_OFF_NOT_SUPPORTED, "设备不支持开关。");
        errorMessageMap.put(Messages.Response.ErrorCode.CAMERA_SD_CANNOT_FORMAT, "摄像头的sd卡无法格式化。");
        errorMessageMap.put(Messages.Response.ErrorCode.CONFLICT_ACTION, "行动冲突。");
        errorMessageMap.put(Messages.Response.ErrorCode.TOO_MANY_ACTIONS, "行动过多。");
        errorMessageMap.put(Messages.Response.ErrorCode.DUPLICATE_SCENE_NAME, "情景名称重复。");
        errorMessageMap.put(Messages.Response.ErrorCode.INVALID_ACTION_PARAM, "无效的action参数。");
        errorMessageMap.put(Messages.Response.ErrorCode.TOO_MANY_SCENES, "情景模式过多，不能超过10个。");
        errorMessageMap.put(Messages.Response.ErrorCode.NO_ACTION_SPECIFIED, "情景模式没有指定对应操作。");
        errorMessageMap.put(Messages.Response.ErrorCode.SCENE_IN_USE, "该情景正在使用，不能删除。");
        errorMessageMap.put(Messages.Response.ErrorCode.DUPLICATE_PLAN_NAME, "计划任务名字重复。");
        errorMessageMap.put(Messages.Response.ErrorCode.TOO_MANY_PLANS, "创建太多计划了，不能超过20个。");
        errorMessageMap.put(Messages.Response.ErrorCode.PLAN_NOT_FOUND, "该计划不存在。");
        errorMessageMap.put(Messages.Response.ErrorCode.INVALID_PLAN_PARAM, "计划参数无效。");
        errorMessageMap.put(Messages.Response.ErrorCode.OPERATION_NOT_SUPPORTED, "操作不支持。");
        errorMessageMap.put(Messages.Response.ErrorCode.PASSPHRASE_LENGTH_INVALID, "密码长度超长，最多64个字符。");
        errorMessageMap.put(Messages.Response.ErrorCode.NO_SUCH_PORT, "无效端口。");
        errorMessageMap.put(Messages.Response.ErrorCode.BACKUP_RESTORE_IN_PROGRESS, "备份/恢复正在进行中。");
        errorMessageMap.put(Messages.Response.ErrorCode.INVALID_STREAM, "无法获取输入输出流。");
        errorMessageMap.put(Messages.Response.ErrorCode.TOO_MANY_CAMERA, "摄像头过多，最多4个。");
        errorMessageMap.put(Messages.Response.ErrorCode.NO_SUCH_SAMBA_MODE, "不支持samba协议。");
        errorMessageMap.put(Messages.Response.ErrorCode.ZIGBEE_WIDGET_NOT_ONLINE, "设备离线。");
        errorMessageMap.put(Messages.Response.ErrorCode.INTERNAL_ERROR, "服务器内部错误。");
    }

    /**
     * 获取路由器通讯错误码解释文本。
     *
     * @param errorCode
     * @return
     */
    public static String getErrorMessage(Messages.Response.ErrorCode errorCode) {
        String errorMessage = "";
        errorMessage = errorMessageMap.get(errorCode);
        if (errorMessage == null)
            errorMessage = "未知错误";
        return errorMessage;
    }

    /**
     * 路由管理器初始化：
     * 1.IPCManager，IPC管理模块。
     * 2.SugarContext，数据库插件。
     * 3.ProtoBuf数据对象，路由器通讯对象。
     * 4.P2P组件，路由器连接组件。
     */
    public static void init(Context context) {
        IPCManager.init();
        SugarContext.init(context);
        if (isP2PInitialized()) return;
        initExtensionRegistry();
        initErrorMessageMap();
        final String sevc = Constants.server;
        final int port = Constants.port;
        final String user = "testsdk";
        final String password = "testsdk";
        mP2PHandle = NewAllStreamParser.DNPCreatePortServer(sevc, port, user, password);
        trace("inited....p2p handle :" + mP2PHandle);
    }

    /**
     * 销毁路由管理器。
     */
    public static void destroy() {
        SugarContext.terminate();
        IPCManager.destroy();
        if (isP2PInitialized()) {
            NewAllStreamParser.DNPDestroyPortServer(mP2PHandle);
        }
    }

    /**
     * 刷新路由器列表，在本地缓存重新加载路由器列表。
     */
    public void loadRouters() {
        final Iterator<RouterConfig> all = SugarRecord.findAll(RouterConfig.class);
        if (all != null)
            while (all.hasNext()) {
                final RouterConfig next = all.next();
                if (next != null)
                    addRouter(new Router("路由器", next.getSn()));
            }
    }

    /**
     * 添加路由器后，管理器会自动连接路由器，获取基础信息，并保持通讯连接。
     * 连接成功的路由器，将会被缓存在本地数据库，同时上传至用户云端数据（如果用户已登录）。
     * 相同路由器无法重复添加。
     *
     * @param router Router对象，可以直接new构造对象，构造对象需要路由器序列号，与路由器名称。相同序列号视为相同路由器。
     */
    public void addRouter(final Router router) {
        if (router == null || router.getSn() == null || getRouter(router.getSn()) != null) return;
        final RouterClient innerRouter = new RouterClient(router, mP2PHandle);
        router.setRouterSession(innerRouter);
        innerRouter.init();
        mRouters.add(router);
        mEventManager.setRouterEvent(innerRouter);
        router.loadConfig();
        subjectRouterCreate.onNext(new RouterCallback<Boolean>() {
            @Override
            public Router getRouter() {
                return router;
            }

            @Override
            public Boolean getData() {
                return true;
            }
        });
        trace("add router :" + router.getSn());
    }

    /**
     * 删除一台路由器。默认不删除数据库缓存路由器信息。
     *
     * @param router 路由器对象。路由器对象可以通过，getRouterList或getRouter获得。
     */
    public void removeRouter(Router router) {
        removeRouter(router, false);
    }

    /**
     * 删除一台路由器。
     *
     * @param router      路由器对象。路由器对象可以通过，getRouterList或getRouter获得。
     * @param removeCache 指定是否不删除数据库缓存路由器信息。true为删除。
     */
    public void removeRouter(final Router router, boolean removeCache) {
        if (router == null) return;
        if (mRouters.remove(router)) {
            ((RouterClient) router.getRouterSession()).destroy();
            if (removeCache) {
                SugarRecord.deleteAll(RouterConfig.class, "sn = ?", new String(router.getConfig().getSn()));
                subjectRouterCreate.onNext(new RouterCallback<Boolean>() {
                    @Override
                    public Router getRouter() {
                        return router;
                    }

                    @Override
                    public Boolean getData() {
                        return false;
                    }
                });
            }
        }
    }

    /**
     * 获取指定路由器，即通过路由器序列号获取对应的路由器。
     *
     * @param sn 路由器序列码。
     * @return 路由器对象。
     */
    public Router getRouter(String sn) {
        if (sn != null)
            for (Router mRouter : mRouters) {
                if (mRouter.getSn().equals(sn))
                    return mRouter;
            }
        return null;
    }

    /**
     * 获取当前路由器列表。
     *
     * @return 路由器列表。（源列表对象，请勿执行增删操作。)
     */
    public List<Router> getRouterList() {
        return mRouters;
    }

    /**
     * 获取指定类型的路由器列表。
     *
     * @param valid 路由器是否可以通讯。true为可以正常通讯。
     * @return
     */
    public List<Router> getRouterList(boolean valid) {
        ArrayList<Router> routers = new ArrayList<>();
        for (Router mRouter : mRouters) {
            if (mRouter.getRouterSession().isAuthenticated() == valid) {
                routers.add(mRouter);
            }
        }
        return routers;
    }

    /**
     * 清除当期添加的所有路由器。
     */
    public void removeAllRouters(boolean removeCache) {
        final Router[] routers = getRouterList().toArray(new Router[getRouterList().size()]);
        for (Router router : routers) {
            removeRouter(router);
        }
    }

    /**
     * 获取事件管理器。事件管理器为所有路由器全局事件。
     *
     * @return
     */
    public EventManager getEventManager() {
        return mEventManager;
    }


    public Subscription subscribeRouterCreateEvent(Action1<RouterCallback<Boolean>> callback) {
        return subjectRouterCreate.subscribe(callback);
    }
}
