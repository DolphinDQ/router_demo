package mrtech.smarthome.router.Models;


import mrtech.smarthome.router.Router;
import mrtech.smarthome.rpc.Messages;
import rx.Subscription;
import rx.functions.Action1;

/**
 * 事件管理器。
 */
public interface EventManager {

    Subscription subscribeRouterStatusChangedEvent(Action1<Router> callback);

    Subscription subscribeRouterEvent(Messages.Event.EventType eventType, Action1<RouterCallback<Messages.Event>> callbackAction);

    Subscription subscribeTimelineEvent(Action1<RouterCallback<mrtech.smarthome.rpc.Models.Timeline>> callback);

}