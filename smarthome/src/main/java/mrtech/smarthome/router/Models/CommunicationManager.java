package mrtech.smarthome.router.Models;


import java.util.Collection;
import java.util.concurrent.TimeoutException;

import mrtech.smarthome.rpc.Messages;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Action2;

/**
 * 路由通讯管理。实现用户与路由器的通讯功能。
 */
public interface CommunicationManager {
    int ROUTER_REQUEST_TIMEOUT = 5000;

    /**
     * 异步向路由器发送请求，默认请求超时为 RouterSession.ROUTER_REQUEST_TIMEOUT,不启用缓存。
     *
     * @param request  请求包
     * @param callback 请求回调
     */
    void postRequestAsync(Messages.Request request, Action2<Messages.Response, Throwable> callback);

    /**
     * 异步向路由器发送请求，默认请求超时为 RouterSession.ROUTER_REQUEST_TIMEOUT
     *
     * @param request  请求包
     * @param callback 请求回调
     * @param cache    是否启用缓存。true启用缓存，则首次发送会网络请求给服务器。
     */
    void postRequestAsync(Messages.Request request, Action2<Messages.Response, Throwable> callback, boolean cache);

    /**
     * 异步向路由器发送请求，默认不启用缓存。
     *
     * @param request  请求包
     * @param callback 请求回调
     * @param timeout  请求超时，毫秒
     */
    void postRequestAsync(Messages.Request request, Action2<Messages.Response, Throwable> callback, int timeout);

    /**
     * 异步向路由器发送请求。
     *
     * @param request  请求包
     * @param callback 请求回调
     * @param timeout  请求超时，毫秒
     * @param cache    是否启用缓存。true启用缓存，则首次发送会网络请求给服务器。
     */
    void postRequestAsync(Messages.Request request, Action2<Messages.Response, Throwable> callback, int timeout, boolean cache);

    /**
     * 向路由器发送一个请求。默认不启用缓存。.
     *
     * @param request 用户请求。
     * @param timeout 超时时间。毫秒
     * @return
     * @throws TimeoutException
     */
    Messages.Response postRequest(Messages.Request request, int timeout) throws TimeoutException;

    /**
     * 向路由器发送一个请求。
     *
     * @param request 用户请求。
     * @param timeout 超时时间。毫秒
     * @param cache   是否启用缓存。true启用缓存，则首次发送会网络请求给服务器。
     * @return
     * @throws TimeoutException
     */
    Messages.Response postRequest(Messages.Request request, int timeout, boolean cache) throws TimeoutException;

    /**
     * 向路由器发送一个请求，默认请求超时为 RouterSession.ROUTER_REQUEST_TIMEOUT,不启用缓存。
     *
     * @param request 用户请求。
     * @return
     * @throws TimeoutException
     */
    Messages.Response postRequest(Messages.Request request) throws TimeoutException;

    /**
     * 向路由器发送一个请求，默认请求超时为 RouterSession.ROUTER_REQUEST_TIMEOUT
     *
     * @param request 用户请求。
     * @param cache   是否启用缓存。true启用缓存，则首次发送会网络请求给服务器。
     * @return
     * @throws TimeoutException
     */
    Messages.Response postRequest(Messages.Request request, boolean cache) throws TimeoutException;

    /**
     * 获取请求队列
     *
     * @return
     */
    Collection<Messages.Request> getRequestQueue();

    /**
     * 从队列取消指定请求。
     *
     * @param request
     */
    void cancelRequestFromQueue(Messages.Request request);

    /**
     * 将指定请求添加进发送队列。发送队列，如果路由器尚未连接或推送不成功，将会等待只路由器连接后在次发送。
     *
     * @param request
     * @param callback
     */
    void postRequestToQueue(Messages.Request request, Action1<Messages.Response> callback);

    /**
     * 取消路由器事件订阅 。
     *
     * @param eventType 指定事件。
     * @throws TimeoutException 与服务器通讯超时。
     */
    void unsubscribeEvent(Messages.Event.EventType eventType);

    /**
     * 订阅指定的路由器时间
     *
     * @param eventType   指定路由器事件
     * @param eventAction 事件回调。
     * @return 事件订阅句柄。注意：在不使用事件的时候，需要调用Subscription.unsubscribe()注销事件。
     * @throws TimeoutException
     */
    Subscription subscribeEvent(Messages.Event.EventType eventType, Action1<Messages.Event> eventAction);

    /**
     * 获取当前路由器所订阅的事件列表。
     *
     * @return
     */
    Collection<Messages.Event.EventType> getEventTypes();

    /**
     * 订阅通讯模块的反馈数据。（所有请求）
     * @param callback 回调数据。
     * @return
     */
    Subscription subscribeResponse(Action1<Messages.Response> callback);

    void test();
}