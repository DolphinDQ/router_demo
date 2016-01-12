package mrtech.smarthome.router.Models;


import mrtech.smarthome.router.Router;
import mrtech.smarthome.rpc.Messages;
import rx.Subscription;
import rx.functions.Action1;

/**
 * 事件管理器。订阅所有路由器发出的事件。
 */
public interface EventManager {

    /**
     * 订阅所有路由器
     * @param callback 事件回调。
     * @return 订阅句柄，句柄在事件不再使用时候，必须执行反订阅（即unsubscribe）。
     */
    Subscription subscribeRouterStatusChangedEvent(Action1<Router> callback);

    /**
     * 订阅路由器回调事件。新建路由器将自动订阅指定事件。
     * 如果在路由器A通讯管理中关闭指定事件（调用CommunicationManager.unsubscribeEvent），本接口同样无法接收到路由器A的指定事件。
     * @param eventType 指定订阅事件类型。
     * @param callback 事件回调。
     * @return 订阅句柄，句柄在事件不再使用时候，必须执行反订阅（即unsubscribe）。
     */
    Subscription subscribeRouterEvent(Messages.Event.EventType eventType, Action1<RouterCallback<Messages.Event>> callback);

    /**
     * @param callback 事件回调。
     * @return 订阅句柄，句柄在事件不再使用时候，必须执行反订阅（即unsubscribe）。
     */
    Subscription subscribeTimelineEvent(Action1<RouterCallback<mrtech.smarthome.rpc.Models.Timeline>> callback);

}