package mrtech.router_demo;

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
public class RouterKeepaliveDService extends IntentService {
    public static String ACT_START="";
    public static String ACT_STOP="";

    private RouterManager routerManager;

    private static void trace(String msg) {
        Log.e(RouterKeepaliveDService.class.getName(), msg);
    }

    public RouterKeepaliveDService() {
        super("RouterKeepaliveDService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        trace("处理数据。"+intent.getAction());

    }

    @Override
    public void onCreate() {
        trace("创建服务。");
        super.onCreate();
        routerManager = RouterManager.getInstance();
    }
}
