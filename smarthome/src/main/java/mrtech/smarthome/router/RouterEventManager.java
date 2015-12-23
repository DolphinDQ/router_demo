package mrtech.smarthome.router;

import android.database.Observable;
import android.util.Log;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeoutException;

import mrtech.smarthome.router.Models.*;
import mrtech.smarthome.rpc.Messages;
import mrtech.smarthome.util.RequestUtil;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

/**
 * Created by sphynx on 2015/12/22.
 */
class RouterEventManager implements EventManager {

    private static void trace(String msg) {
        Log.e(RouterEventManager.class.getName(), msg);
    }

    private final Thread mQueryTimelineTask;
    private final RouterManager mManager;
    private final PublishSubject<Router> subjectRouterStatusChanged = PublishSubject.create();
    private final PublishSubject<RouterCallback<Messages.Event>> subjectRouterEvents = PublishSubject.create();
    private final PublishSubject<RouterCallback<mrtech.smarthome.rpc.Models.Timeline>> subjectTimeLine = PublishSubject.create();
    private final ArrayList<Messages.Event.EventType> mEventTypes;

    public RouterEventManager(RouterManager routerManager) {
        mManager = routerManager;
        mEventTypes = new ArrayList<>();
        subjectRouterEvents.filter(new Func1<RouterCallback<Messages.Event>, Boolean>() {
            @Override
            public Boolean call(RouterCallback<Messages.Event> eventRouterCallback) {
                return eventRouterCallback.getData().getType() == Messages.Event.EventType.NEW_TIMELINE;
            }
        }).map(new Func1<RouterCallback<Messages.Event>, RouterCallback<mrtech.smarthome.rpc.Models.Timeline>>() {
            @Override
            public RouterCallback<mrtech.smarthome.rpc.Models.Timeline> call(final RouterCallback<Messages.Event> eventRouterCallback) {
                return new RouterCallback<mrtech.smarthome.rpc.Models.Timeline>() {
                    @Override
                    public Router getRouter() {
                        return eventRouterCallback.getRouter();
                    }

                    @Override
                    public mrtech.smarthome.rpc.Models.Timeline getData() {
                        return eventRouterCallback.getData().getExtension(Messages.NewTimelineEvent.event).getTimeline();
                    }
                };
            }
        }).subscribe(subjectTimeLine);
        mQueryTimelineTask = new Thread(new QueryTimeLineTask());
        mQueryTimelineTask.start();
        subjectRouterStatusChanged.subscribe(new Action1<Router>() {
            @Override
            public void call(Router router) {
                if (router.getRouterSession().isAuthenticated()) {
                    queryTimeline(router,System.currentTimeMillis()-getDefaultSince());
                }
            }
        });
    }

    /**
     * 订阅所有路由器状态变更事件。
     *
     * @param callback 事件回调。
     * @return 事件订阅句柄。注意：在不使用事件的时候，需要调用Subscription.unsubscribe()注销事件。
     */
    @Override
    public Subscription subscribeRouterStatusChangedEvent(Action1<Router> callback) {
        return subjectRouterStatusChanged.subscribe(callback);
    }

    /**
     * 订阅所有路由器回调事件
     *
     * @param eventType
     * @param callbackAction
     * @return 事件订阅句柄。注意：在不使用事件的时候，需要调用Subscription.unsubscribe()注销事件。
     */
    @Override
    public Subscription subscribeRouterEvent(final Messages.Event.EventType eventType, final Action1<RouterCallback<Messages.Event>> callbackAction) {
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

    @Override
    public Subscription subscribeTimelineEvent(Action1<RouterCallback<mrtech.smarthome.rpc.Models.Timeline>> callback) {
        return subjectTimeLine.subscribe(callback);
    }

    private void subscribeEvents() {
        for (final Router router : mManager.getRouterList()) {
            for (Messages.Event.EventType eventType : mEventTypes) {
                router.getRouterSession().getDataChannel().subscribeEvent(eventType, null);
            }
        }
    }

    public void setRouter(final RouterClient client) {
        client.subscribeRouterStatusChanged(new Action1<Router>() {
            @Override
            public void call(Router router) {
                subjectRouterStatusChanged.onNext(router);
            }
        });
        ((RouterCommunicationManager) client.getDataChannel()).getEventObservable().map(new Func1<Messages.Event, RouterCallback<Messages.Event>>() {
            @Override
            public RouterCallback<Messages.Event> call(final Messages.Event event) {
                return new RouterCallback<Messages.Event>() {
                    @Override
                    public Router getRouter() {
                        return client.getRouter();
                    }

                    @Override
                    public Messages.Event getData() {
                        return event;
                    }
                };
            }
        }).subscribe(subjectRouterEvents);
    }

    private class QueryTimeLineTask implements Runnable {

        @Override
        public void run() {
            Thread.currentThread().setName("QueryTimelineTask");
            long since =getDefaultSince();
            do {
                final List<Router> routerList = mManager.getRouterList(true);
                for (final Router router : routerList) {
                    if (queryTimeline(router, since)) {
                        since = System.currentTimeMillis();
                    }
                }
                try {
                    Thread.sleep(QUERY_TIMELINE_INTERVAL);
                } catch (InterruptedException e) {
                    trace(Thread.currentThread().getName() + " wakeup!");
                }
            } while (true);
        }
    }
    private long getDefaultSince(){
       return   System.currentTimeMillis() - 1000 * 3600 * 24;
    }

    private boolean queryTimeline(final Router router, long since) {
        try {
            final Messages.Response response = router.getRouterSession()
                    .getDataChannel()
                    .postRequest(RequestUtil.getTimeline(mrtech.smarthome.rpc.Models.TimelineQuery
                            .newBuilder()
                            .setPageSize(100)
                            .setPage(0)
                            .setSince(since)
                            .setLevel(mrtech.smarthome.rpc.Models.TimelineLevel.TIMELINE_LEVEL_ALARM)
                            .build()));
            if (response.getErrorCode() == Messages.Response.ErrorCode.SUCCESS) {
                final List<mrtech.smarthome.rpc.Models.Timeline> resultsList = response.getExtension(Messages.QueryTimelineResponse.response).getResultsList();
                for (final mrtech.smarthome.rpc.Models.Timeline timeline : resultsList) {
                    subjectTimeLine.onNext(new RouterCallback<mrtech.smarthome.rpc.Models.Timeline>() {
                        @Override
                        public Router getRouter() {
                            return router;
                        }

                        @Override
                        public mrtech.smarthome.rpc.Models.Timeline getData() {
                            return timeline;
                        }
                    });
                }
                return true;
            }

        } catch (TimeoutException e) {
        }
        return false;
    }
}
