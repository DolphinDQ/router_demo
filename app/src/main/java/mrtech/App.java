package mrtech;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import mrtech.activities.MainActivity;
import mrtech.activities.R;
import mrtech.services.RouterQueryTimelineService;
import mrtech.smarthome.SmartHomeApp;
import mrtech.smarthome.router.Router;
import mrtech.smarthome.router.RouterManager;
import mrtech.smarthome.rpc.Models;
import rx.functions.Action1;


/**
 * Created by sphynx on 2015/12/11.
 */
public class App extends SmartHomeApp {
    private static App instance;
    private int id;

    public static App getInstance() {
        return instance;
    }

    public App() {
        if (instance != null)
            Log.e("Application", "!!!!!!!!!!!");
        instance = this;
    }

    private boolean notificationColdTime = false;

    /**
     * 推送路由器报警信息。
     *
     * @param mes
     * @param time
     */
    private void pushAlarmNotification(String sn, String mes, String time) {
        Notification notification = new Notification();
        notification.icon = R.drawable.ic_notifications_black_24dp;
        notification.tickerText = mes;
        Intent routerActivityIntent = new Intent(this, MainActivity.class);
        routerActivityIntent.setAction(sn);
        routerActivityIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        notification.contentIntent = PendingIntent.getActivity(this, 0, routerActivityIntent, 0);
        notification.contentView = new RemoteViews(this.getPackageName(), R.layout.layout_notification);
        notification.contentView.setImageViewResource(R.id.status_icon, R.drawable.ic_notifications_black_24dp);
        notification.contentView.setTextViewText(R.id.status_text, mes);
        notification.contentView.setTextViewText(R.id.time_test, time);

        if (notificationColdTime) {
            notification.defaults = Notification.DEFAULT_LIGHTS;
        } else {
            notification.defaults = Notification.DEFAULT_ALL;
            notificationColdTime = true;
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    notificationColdTime = false;
                    return null;
                }
            }.execute();
        }
        notification.flags = notification.flags | Notification.FLAG_SHOW_LIGHTS | Notification.FLAG_AUTO_CANCEL;
        ((NotificationManager) this.getSystemService(NOTIFICATION_SERVICE)).notify(id++, notification);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startService(new Intent(getBaseContext(), RouterQueryTimelineService.class));
        final RouterManager routerManager = RouterManager.getInstance();
        //订阅路由器报警事件。
        routerManager.getEventManager().subscribeTimelineEvent(new Action1<mrtech.smarthome.router.Models.RouterCallback<Models.Timeline>>() {
            @Override
            public void call(mrtech.smarthome.router.Models.RouterCallback<Models.Timeline> timelineRouterCallback) {
                try {
                    final Models.Timeline timeline = timelineRouterCallback.getData();
                    if (timeline.getLevel() != Models.TimelineLevel.TIMELINE_LEVEL_ALARM)
                        return;
                    JSONObject object = new JSONObject(timeline.getParameter());
                    final Object name = object.get("name");
                    pushAlarmNotification(timelineRouterCallback.getRouter().getSn(), name + "报警", new Date(timeline.getTimestamp() * 1000).toLocaleString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        //路由器验证成功后自动加载摄像头。
        routerManager.getEventManager().subscribeRouterStatusChangedEvent(new Action1<Router>() {
            @Override
            public void call(final Router router) {
                if (router.getRouterSession().isAuthenticated()) {
                    router.getRouterSession().getCameraManager().reloadIPCAsync(false, new Action1<Throwable>() {
                        @Override
                        public void call(final Throwable throwable) {
                            new Handler(getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    if (throwable != null) {
                                        throwable.printStackTrace();
                                        Toast.makeText(App.this, router.getName() + "加载摄像头失败。" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(App.this, router.getName() + "加载摄像头完毕。", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    });
                }
            }
        });
        //加载本地路由器。
        routerManager.loadRouters();
    }

}
