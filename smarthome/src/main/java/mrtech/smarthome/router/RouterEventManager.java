package mrtech.smarthome.router;

import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import mrtech.smarthome.router.Models.*;
import mrtech.smarthome.rpc.Messages;
import mrtech.smarthome.util.RequestUtil;
import rx.Observable;
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

    //    private final Thread mQueryTimelineTask;
    private final RouterManager mManager;

    public rx.Observable<Router> getSubjectRouterStatusChanged() {
        return subjectRouterStatusChanged;
    }

    public rx.Observable<RouterCallback<mrtech.smarthome.rpc.Models.Timeline>> getSubjectTimeLine() {
        return subjectTimeLine;
    }

    private final PublishSubject<Router> subjectRouterStatusChanged = PublishSubject.create();

    private final PublishSubject<RouterCallback<Messages.Event>> subjectRouterEvents = PublishSubject.create();

    private final PublishSubject<RouterCallback<mrtech.smarthome.rpc.Models.Timeline>> subjectTimeLine = PublishSubject.create();

    private final ArrayList<Messages.Event.EventType> mEventTypes;

    public RouterEventManager(RouterManager routerManager) {
        mManager = routerManager;
        mEventTypes = new ArrayList<>();
//        mQueryTimelineTask = new Thread(new QueryTimeLineTask());
//        mQueryTimelineTask.start();
        getSubjectRouterStatusChanged().subscribe(new Action1<Router>() {
            @Override
            public void call(Router router) {
                if (router.getRouterSession().isAuthenticated()) {
                    queryTimeline(router);
                }
            }
        });
        getSubjectTimeLine().subscribe(new Action1<RouterCallback<mrtech.smarthome.rpc.Models.Timeline>>() {
            @Override
            public void call(RouterCallback<mrtech.smarthome.rpc.Models.Timeline> timelineRouterCallback) {
                final RouterConfig config = timelineRouterCallback.getRouter().getConfig();
                final long timestamp = timelineRouterCallback.getData().getTimestamp();
                if (timestamp >= config.getLastUpdateTime()) {
                    config.setLastUpdateTime(timestamp + 1); // 最后更新时间为最后报警的时间+1秒
                    trace("last update time update to :" + timestamp);
                    timelineRouterCallback.getRouter().saveConfig();
                    trace("last update time save completed!");
                }
            }
        });
        subscribeRouterEvent(Messages.Event.EventType.NEW_TIMELINE, new Action1<RouterCallback<Messages.Event>>() {
            @Override
            public void call(final RouterCallback<Messages.Event> eventRouterCallback) {
                subjectTimeLine.onNext(new RouterCallback<mrtech.smarthome.rpc.Models.Timeline>() {
                    @Override
                    public Router getRouter() {
                        return eventRouterCallback.getRouter();
                    }

                    @Override
                    public mrtech.smarthome.rpc.Models.Timeline getData() {
                        return eventRouterCallback.getData().getExtension(Messages.NewTimelineEvent.event).getTimeline();
                    }
                });
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
        return getSubjectRouterStatusChanged().subscribe(callback);
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
        return subjectRouterEvents.onErrorResumeNext(new Func1<Throwable, Observable<? extends RouterCallback<Messages.Event>>>() {
            @Override
            public Observable<? extends RouterCallback<Messages.Event>> call(Throwable throwable) {
                throwable.printStackTrace();
                return PublishSubject.create();
            }
        }).filter(new Func1<RouterCallback<Messages.Event>, Boolean>() {
            @Override
            public Boolean call(RouterCallback<Messages.Event> eventRouterCallback) {
                return eventRouterCallback.getData().getType() == eventType;
            }
        }).subscribe(callbackAction);
    }

    @Override
    public Subscription subscribeTimelineEvent(Action1<RouterCallback<mrtech.smarthome.rpc.Models.Timeline>> callback) {
        return getSubjectTimeLine().subscribe(callback);
    }

    private void subscribeEvents() {
        trace("subscribe events!!");
        for (final Router router : mManager.getRouterList()) {
            for (Messages.Event.EventType eventType : mEventTypes) {
                router.getRouterSession().getCommunicationManager().subscribeEvent(eventType, null);
            }
        }
    }

    public void setRouterEvent(final RouterClient client) {
        client.subscribeRouterStatusChanged(new Action1<Router>() {
            @Override
            public void call(Router router) {
                subjectRouterStatusChanged.onNext(router);
            }
        });
        ((RouterCommunicationManager) client.getCommunicationManager()).getEventObservable().map(new Func1<Messages.Event, RouterCallback<Messages.Event>>() {
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
        subscribeEvents();
    }

    private class QueryTimeLineTask implements Runnable {
        @Override
        public void run() {
            Thread.currentThread().setName("QueryTimelineTask");
            do {
                final List<Router> routerList = mManager.getRouterList(true);
                for (final Router router : routerList) {
                    queryTimeline(router);
                }
                try {
                    Thread.sleep(60 * 1000);
                } catch (InterruptedException e) {
                    trace(Thread.currentThread().getName() + " wakeup!");
                }
            } while (true);
        }
    }


    private void queryTimeline(final Router router) {
        try {
            final RouterConfig config = router.getConfig();
            long since = config.getLastUpdateTime();
            trace("query time line since:" + since);
            final Messages.Response response = router.getRouterSession()
                    .getCommunicationManager()
                    .postRequest(RequestUtil.getTimeline(mrtech.smarthome.rpc.Models.TimelineQuery
                            .newBuilder()
                            .setPageSize(100)
                            .setPage(0)
                            .setSince(since)
                            .setLevel(mrtech.smarthome.rpc.Models.TimelineLevel.TIMELINE_LEVEL_ALARM)
                            .build()));
            if (response != null && response.getErrorCode() == Messages.Response.ErrorCode.SUCCESS) {
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
            }
        } catch (TimeoutException e) {
        }
    }

}
