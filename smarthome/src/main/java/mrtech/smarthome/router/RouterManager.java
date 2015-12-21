package mrtech.smarthome.router;

import android.app.usage.UsageEvents;
import android.util.Log;

import com.google.protobuf.ExtensionRegistry;
import com.stream.NewAllStreamParser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeoutException;

import mrtech.smarthome.rpc.Messages;
import mrtech.smarthome.rpc.Models;
import mrtech.smarthome.util.Constants;
import mrtech.smarthome.router.Models.*;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

/**
 * router connection manager
 * Created by sphynx on 2015/12/1.
 */
public class RouterManager {
    public final static ExtensionRegistry registry = ExtensionRegistry.newInstance();
    private static Map<Messages.Response.ErrorCode, String> errorMessageMap;
    private PublishSubject<Router> subjectRouterStatusChanged = PublishSubject.create();
    private PublishSubject<RouterCallback<Messages.Event>> subjectRouterEvents = PublishSubject.create();
    private final ArrayList<Messages.Event.EventType> mEventTypes;

    private static void trace(String msg) {
        Log.e(RouterManager.class.getName(), msg);
    }

    private static RouterManager ourInstance = new RouterManager();

    /**
     * 创建路由器管理单例对象。
     *
     * @return
     */
    public static RouterManager getInstance() {
        return ourInstance;
    }

    private static int mP2PHandle;

    private ArrayList<Router> mRouters;

    private RouterManager() {
        mRouters = new ArrayList<>();
        mEventTypes = new ArrayList<>();
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
     * 路由管理器初始化。
     */
    public static void init() {
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
        if (isP2PInitialized()) {
            NewAllStreamParser.DNPDestroyPortServer(mP2PHandle);
        }
    }

    /**
     * 添加一台路由器
     *
     * @param router 路由器对象。
     */
    public void addRouter(final Router router) {
        if (router == null || router.getSN() == null) return;
        final RouterClient innerRouter = new RouterClient(router, mP2PHandle);
        router.setRouterSession(innerRouter);
        innerRouter.subscribeRouterStatusChanged(new Action1<Router>() {
            @Override
            public void call(Router router) {
                subjectRouterStatusChanged.onNext(router);
            }
        });
        innerRouter.getEventObservable().map(new Func1<Messages.Event, RouterCallback<Messages.Event>>() {
            @Override
            public RouterCallback<Messages.Event> call(final Messages.Event event) {
                return new RouterCallback<Messages.Event>() {
                    @Override
                    public Router getRouter() {
                        return router;
                    }

                    @Override
                    public Messages.Event getData() {
                        return event;
                    }
                };
            }
        }).subscribe(subjectRouterEvents);
        subscribeEvents();
        innerRouter.init();
        mRouters.add(router);
        trace("add router :" + router.getSN());
    }

    /**
     * 删除一台路由器。
     *
     * @param router 路由器对象。
     */
    public void removeRouter(Router router) {
        if (router == null) return;
        if (mRouters.remove(router)) {
            ((RouterClient) router.getRouterSession()).destroy();
        }
        ;
    }

    /**
     * 获取指定路由器。
     *
     * @param sn 路由器SN码。
     * @return 路由器对象。
     */
    public Router getRouter(String sn) {
        for (Router mRouter : mRouters) {
            if (mRouter.getSN().equals(sn))
                return mRouter;
        }
        return null;
    }

    /**
     * 获取当前路由器列表。
     *
     * @return 路由器列表。只读，请勿执行增删操作。
     */
    public List<Router> getRouterList() {
        return mRouters;
    }

    /**
     * 获取指定类型的路由器列表。
     *
     * @param valid 路由器是否可以通讯。
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
    public void removeAll() {
        final Collection<Router> routerList = getRouterList();
        for (Router router : routerList) {
            removeRouter(router);
        }
    }

    /**
     * 订阅所有路由器状态变更事件。
     *
     * @param callback 事件回调。
     * @return 事件订阅句柄。注意：在不使用事件的时候，需要调用Subscription.unsubscribe()注销事件。
     */
    public Subscription subscribeRouterStatusChanged(Action1<Router> callback) {
        return subjectRouterStatusChanged.subscribe(callback);
    }

    /**
     * 订阅所有路由器回调事件
     *
     * @param eventType
     * @param callbackAction
     * @return 事件订阅句柄。注意：在不使用事件的时候，需要调用Subscription.unsubscribe()注销事件。
     */
    public Subscription subscribeRouterEvent(final Messages.Event.EventType eventType,
                                             final Action1<RouterCallback<Messages.Event>> callbackAction) {
        if (!mEventTypes.contains(eventType))
            mEventTypes.add(eventType);
        subscribeEvents();
        return subjectRouterEvents.filter(new Func1<RouterCallback<Messages.Event>, Boolean>() {
            @Override
            public Boolean call(RouterCallback<Messages.Event> eventRouterCallback) {
                return eventRouterCallback.getData().getType() == eventType;
            }
        }).subscribe(callbackAction);
    }

    private void subscribeEvents() {
        for (final Router router : getRouterList()) {
            for (Messages.Event.EventType eventType : mEventTypes) {
                router.getRouterSession().subscribeEvent(eventType, null);
            }
        }
    }
}
