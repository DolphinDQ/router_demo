package mrtech.activities;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import mrtech.smarthome.router.RouterManager;

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

    private static boolean terminate;

    public static void setTerminate(boolean terminate) {
        RouterQueryTimelineService.terminate = terminate;
    }

    public RouterQueryTimelineService() {
        super("RouterQueryTimelineService");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (terminate) {
            return super.onStartCommand(intent, flags, startId);
        } else {
            return START_STICKY;
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        do {
            try {
                trace("还活着：" + RouterManager.getInstance().getRouterList().size() + "个路由器");
                Thread.sleep(30 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (!terminate);
    }
}
