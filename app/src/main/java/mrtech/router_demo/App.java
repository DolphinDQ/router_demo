package mrtech.router_demo;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.multidex.MultiDex;
import android.util.Log;
import android.widget.RemoteViews;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import mrtech.smarthome.auth.UserManager;
import mrtech.smarthome.router.RouterManager;
import mrtech.smarthome.rpc.Models;
import rx.Subscription;
import rx.functions.Action1;


/**
 * Created by sphynx on 2015/12/11.
 */
public class App extends Application {
    private static App instance;
    private Subscription alarmHandle;
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

    private void pushAlarmNotification(String mes, String time) {
        Notification notification = new Notification();
        notification.icon = R.drawable.ic_notifications_black_24dp;
        notification.tickerText = mes;
        Intent routerActivityIntent = new Intent(this, RouterSettingsActivity.class);
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
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static class A<T> {
        public String W;
        public T[] E;
    }

    public static class HH {
        public String HH;
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        String json = "{\"W\":\"123\",\"E\":[{\"HH\":\"3333\"}]}";
//        final A<HH> a = new Gson().fromJson(json, new TypeToken<A<HH>>(){}.getType());
//        Log.e("ddd",a.E[0].HH);


        RouterManager.init(this);
        UserManager.getInstance().init(this);
        startService(new Intent(getBaseContext(), RouterQueryTimelineService.class));
        if (alarmHandle != null) alarmHandle.unsubscribe();
        alarmHandle = RouterManager.getInstance().getEventManager().subscribeTimelineEvent(new Action1<mrtech.smarthome.router.Models.RouterCallback<Models.Timeline>>() {
            @Override
            public void call(mrtech.smarthome.router.Models.RouterCallback<Models.Timeline> timelineRouterCallback) {
                try {
                    final Models.Timeline timeline = timelineRouterCallback.getData();
                    if (timeline.getLevel() != Models.TimelineLevel.TIMELINE_LEVEL_ALARM)
                        return;
                    JSONObject object = new JSONObject(timeline.getParameter());
                    final Object name = object.get("name");
                    pushAlarmNotification(name + "报警", new Date(timeline.getTimestamp() * 1000).toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        RouterManager.getInstance().refreshRouters();
    }

    @Override
    public void onTerminate() {
        alarmHandle.unsubscribe();
        RouterManager.destroy();
        super.onTerminate();
    }
}
