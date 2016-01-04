package mrtech.router_demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.lang.reflect.Type;
import java.util.Hashtable;

import mrtech.smarthome.router.Router;
import mrtech.smarthome.router.RouterManager;

/**
 * Created by sphynx on 2015/12/28.
 */
public class BaseActivity extends AppCompatActivity {
    private static Hashtable<Type, Object> defaultData = new Hashtable<>();

    public static <T> T getDefaultData(Class<T> cls) {
        T result = null;
        if (defaultData.containsKey(cls)) {
            result = (T) defaultData.get(cls);
        }
        return result;
    }

    public static void setDefaultData(Object object) {
        if (defaultData.containsKey(object.getClass())) {
            defaultData.remove(object.getClass());
        }
        defaultData.put(object.getClass(), object);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final Intent intent = getIntent();
        final RouterManager routerManager = RouterManager.getInstance();
        if (intent != null) {
            Router temp = routerManager.getRouter(intent.getAction());
            if (temp != null) setDefaultData(temp);
            if (getDefaultData(Router.class) != null) {
                super.onCreate(savedInstanceState);
                return;
            }
        }
        Toast.makeText(BaseActivity.this, "需要指定操作的路由器。", Toast.LENGTH_SHORT).show();
        finish();
    }
}
