package mrtech.router_demo;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.multidex.MultiDex;
import android.util.Log;
import android.widget.RemoteViews;

import com.orm.SugarContext;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import mrtech.smarthome.ipc.IPCManager;
import mrtech.smarthome.router.Router;
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

    public void statusNotification(String mes, String time) {
        Notification mNotification = new Notification();
        Intent mIntent = new Intent(this, RouterSettingsActivity.class);
        //这里需要设置Intent.FLAG_ACTIVITY_NEW_TASK属性
        mIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        PendingIntent mContentIntent = PendingIntent.getActivity(this, 0, mIntent, 0);

        mNotification.tickerText = mes;
        mNotification.icon = R.drawable.ic_notifications_black_24dp;
        //1，使用setLatestEventInfo
        //这里必需要用setLatestEventInfo(上下文,标题,内容,PendingIntent)不然会报错.
//          mNotification.setLatestEventInfo(mContext, "新消息", "主人，您孙子给你来短息了", mContentIntent);

        //2,使用远程视图
        mNotification.contentView = new RemoteViews(this.getPackageName(), R.layout.layout_notification);
        mNotification.contentView.setImageViewResource(R.id.status_icon, R.drawable.ic_notifications_black_24dp);
        mNotification.contentView.setTextViewText(R.id.status_text, mes);
        mNotification.contentView.setTextViewText(R.id.time_test, time);

        if (notificationColdTime) {
            mNotification.defaults = Notification.DEFAULT_LIGHTS;
        } else {

            mNotification.defaults = Notification.DEFAULT_ALL;
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

//        mNotification.defaults = Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE;

        //添加震动
//        if (!notificationColdTime) {
//            mNotification.vibrate = new long[]{500, 1000, 500, 1000};
//            //        使用默认的声音，闪屏，振动效果
//        }

//        //添加led
//        mNotification.ledARGB = Color.BLUE;
//        mNotification.ledOffMS = 0;
//        mNotification.ledOnMS = 1;
        mNotification.flags = mNotification.flags | Notification.FLAG_SHOW_LIGHTS | Notification.FLAG_AUTO_CANCEL;

        //手动设置contentView属于时，必须同时也设置contentIntent不然会报错
        mNotification.contentIntent = mContentIntent;

        //触发通知(消息ID,通知对象)
        ((NotificationManager) this.getSystemService(NOTIFICATION_SERVICE)).notify(id++, mNotification);
    }

//    private void addRouter(String sn) {
//        RouterManager.getInstance().addRouter(new Router(null, "路由器", sn));
//    }

    @Override
    public void onCreate() {
        super.onCreate();
        MultiDex.install(this);
        SugarContext.init(this);
        IPCManager.init();
        RouterManager.init();
        Intent intent = new Intent(getBaseContext(), RouterQueryTimelineService.class);
        startService(intent);

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
                    statusNotification(name + "报警", new Date(timeline.getTimestamp() * 1000).toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        RouterManager.getInstance().loadRouters();
    }

    @Override
    public void onTerminate() {
        alarmHandle.unsubscribe();
        RouterManager.destroy();
        IPCManager.destroy();
        SugarContext.terminate();
        super.onTerminate();
    }
}
