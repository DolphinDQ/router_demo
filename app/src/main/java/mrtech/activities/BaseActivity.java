package mrtech.activities;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.Hashtable;
import java.util.List;

import mrtech.smarthome.router.Router;
import mrtech.smarthome.router.RouterManager;

/**
 * Created by sphynx on 2015/12/28.
 */
public class BaseActivity extends AppCompatActivity {
    private static Hashtable<String, Object> cacheData = new Hashtable<>();

    public static boolean isTopActivity(String className, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfo = manager.getRunningTasks(Integer.MAX_VALUE);
        String cmpNameTemp = null;
        if (null != runningTaskInfo) {
            cmpNameTemp = (runningTaskInfo.get(0).topActivity).getClassName();
        }
        if (null == cmpNameTemp) {
            return false;
        }
        return cmpNameTemp.equals(className);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getCacheData(Class<T> cls) {
        return (T) getCacheData(cls.getName());
    }

    public static Object getCacheData(String key) {
        Object result = null;
        if (cacheData.containsKey(key)) {
            result = cacheData.get(key);
        }
        return result;
    }

    public static void setCacheData(String key, Object data) {
        if (cacheData.containsKey(key)) {
            cacheData.remove(key);
        }
        if (data != null)
            cacheData.put(key, data);
    }

    /**
     * 设置默认值
     *
     * @param cls  数据类型
     * @param data 可以为空
     * @param <T>  数据类型
     */
    public static <T> void setCacheData(Class<T> cls, T data) {
        setCacheData(cls.getName(), data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final Intent intent = getIntent();
        final RouterManager routerManager = RouterManager.getInstance();
        if (intent != null) {
            Router temp = routerManager.getRouter(intent.getAction());
            if (temp != null) setCacheData(Router.class, temp);
        }
        super.onCreate(savedInstanceState);
    }

    protected boolean isActive() {
        return isTopActivity(getClass().getName(), this);
    }
}
