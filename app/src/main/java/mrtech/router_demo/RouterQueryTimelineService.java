package mrtech.router_demo;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.widget.RemoteViews;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import mrtech.smarthome.router.RouterManager;
import mrtech.smarthome.rpc.Models;
import rx.Subscription;
import rx.functions.Action1;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class RouterQueryTimelineService extends IntentService {
    private static void trace(String msg) {
        Log.e(RouterQueryTimelineService.class.getName(), msg);
    }

    public RouterQueryTimelineService() {
        super("RouterQueryTimelineService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        trace("处理服务事物。");
        do {
            try {
                trace("还活着：" + RouterManager.getInstance().getRouterList(true).size() + "个路由器");
                Thread.sleep(30 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (true);
    }
}
