package mrtech.smarthome.router.Models;


import java.util.Collection;
import java.util.concurrent.TimeoutException;

import mrtech.smarthome.rpc.Messages;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Action2;

/**
 * 路由通讯管理。实现用户与路由器的通讯功能
 */
public interface CommunicationManager {
    int ROUTER_REQUEST_TIMEOUT = 5000;

    /**
     * 异步向路由器发送请求，默认请求超时为 RouterSession.ROUTER_REQUEST_TIMEOUT,不启用缓存
     * @param request 路由器请求信息。可以通过RequestUtil创建
     * @param callback 请求回调，回调throwable对象如果不等于null，即表示当前请求存在错误
     */
    void postRequestAsync(Messages.Request request, Action2<Messages.Response, Throwable> callback);

    /**
     * 异步向路由器发送请求，默认请求超时为 RouterSession.ROUTER_REQUEST_TIMEOUT
     * @param request 路由器请求信息。可以通过RequestUtil创建
     * @param callback 请求回调，回调throwable对象如果不等于null，即表示当前请求存在错误
     * @param cache 是否启用缓存。true启用缓存，则首次发送会网络请求给服务器
     */
    void postRequestAsync(Messages.Request request, Action2<Messages.Response, Throwable> callback, boolean cache);

    /**
     * 异步向路由器发送请求，默认不启用缓存
     * @param request 路由器请求信息。可以通过RequestUtil创建
     * @param callback 请求回调，回调throwable对象如果不等于null，即表示当前请求存在错误
     * @param timeout 请求超时，毫秒
     */
    void postRequestAsync(Messages.Request request, Action2<Messages.Response, Throwable> callback, int timeout);

    /**
     * 异步向路由器发送请求
     * @param request 路由器请求信息。可以通过RequestUtil创建
     * @param callback 请求回调，回调throwable对象如果不等于null，即表示当前请求存在错误
     * @param timeout 请求超时，毫秒
     * @param cache 是否启用缓存。true启用缓存，则首次发送会网络请求给服务器
     */
    void postRequestAsync(Messages.Request request, Action2<Messages.Response, Throwable> callback, int timeout, boolean cache);

    /**
     * 向路由器发送一个请求。默认不启用缓存
     * @param request 路由器请求信息。可以通过RequestUtil创建
     * @param timeout 超时时间。毫秒
     * @return 请求回调
     * @throws TimeoutException
     */
    Messages.Response postRequest(Messages.Request request, int timeout) throws TimeoutException;

    /**
     * 向路由器发送一个请求
     * @param request 路由器请求信息。可以通过RequestUtil创建
     * @param timeout 超时时间。毫秒
     * @param cache 是否启用缓存。true启用缓存，则首次发送会网络请求给服务器
     * @return 请求回调
     * @throws TimeoutException
     */
    Messages.Response postRequest(Messages.Request request, int timeout, boolean cache) throws TimeoutException;

    /**
     * 向路由器发送一个请求，默认请求超时为 RouterSession.ROUTER_REQUEST_TIMEOUT,不启用缓存
     * @param request 路由器请求信息。可以通过RequestUtil创建
     * @return 请求回调
     * @throws TimeoutException
     */
    Messages.Response postRequest(Messages.Request request) throws TimeoutException;

    /**
     * 向路由器发送一个请求，默认请求超时为 RouterSession.ROUTER_REQUEST_TIMEOUT
     * @param request 路由器请求信息。可以通过RequestUtil创建
     * @param cache 是否启用缓存。true启用缓存，则首次发送会网络请求给服务器
     * @return 请求回调
     * @throws TimeoutException
     */
    Messages.Response postRequest(Messages.Request request, boolean cache) throws TimeoutException;

    /**
     * 订阅指定流。即路由器发送数据流。
     * 部分请求反馈数据超过协议规定长度0xFF（如：下载配置文件），需要通过流的方式接收数据。
     * 当前接口即封装该部分功能。
     * 使用方式：发送指定请求后得到流ID，通过当前接口订阅回调数据。
     * 回调完毕流自动关闭流。
     * @param streamId 流ID。
     * @param callback 回调数据。
     * @return 订阅对象。
     */
    Subscription subscribeStream(int streamId,Action2<Messages.StreamMultiplexingUnit,Throwable> callback);

    /**
     * 获取请求队列，调用postRequestToQueue方法后，请求将会进入请求等待队列。获取到对应的请求可以调用cancelRequestFromQueue，取消请求
     * @return 返回请求队列
     */
    Collection<Messages.Request> getRequestQueue();

    /**
     * 从队列取消指定请求
     * @param request 路由器请求信息。可以通过RequestUtil创建
     */
    void cancelRequestFromQueue(Messages.Request request);

    /**
     * 将指定请求添加进发送队列。发送队列，如果路由器尚未连接或推送不成功，将会等待只路由器连接后再次发送
     * @param request 路由器请求信息。可以通过RequestUtil创建
     * @param callback 请求回调
     */
    void postRequestToQueue(Messages.Request request, Action1<Messages.Response> callback);

    /**
     * 取消路由器事件订阅。调用此方法后，路由器将不再发送指定的事件回调到当前连接会话
     * @param eventType 指定事件
     */
    void unsubscribeEvent(Messages.Event.EventType eventType);

    /**
     * 订阅指定的路由器回调事件
     * @param eventType 指定路由器事件
     * @param eventAction 事件回调
     * @return 事件订阅句柄。注意：在不使用事件的时候，需要调用Subscription.unsubscribe()注销事件
     *
     */
    Subscription subscribeEvent(Messages.Event.EventType eventType, Action1<Messages.Event> eventAction);

    /**
     * 获取当前路由器所订阅的事件列表
     * @return 返回当前已经订阅的事件类型
     */
    Collection<Messages.Event.EventType> getEventTypes();

    /**
     * 订阅通讯模块的反馈数据（所有请求）
     * @param callback 回调数据
     * @return 事件订阅句柄。注意：在不使用事件的时候，需要调用Subscription.unsubscribe()注销事件
     */
    Subscription subscribeResponse(Action1<Messages.Response> callback);

}