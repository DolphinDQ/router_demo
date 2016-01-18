package mrtech.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

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

    /**
     * 设置默认值
     *
     * @param object 不能为空。
     */
    public static void setDefaultData(Object object) {
        if (object == null) throw new IllegalArgumentException("setDefaultData(Object) 参数不能为空！");
        defaultData.put(object.getClass(), object);
    }

    /**
     * 设置默认值
     *
     * @param cls  数据类型
     * @param data 可以为空
     * @param <T>  数据类型
     */
    public static <T> void setDefaultData(Class<T> cls, T data) {
        if (defaultData.containsKey(cls)) {
            defaultData.remove(cls);
        }
        if (data != null)
            defaultData.put(cls, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final Intent intent = getIntent();
        final RouterManager routerManager = RouterManager.getInstance();
        if (intent != null) {
            Router temp = routerManager.getRouter(intent.getAction());
            if (temp != null) setDefaultData(temp);
        }
        super.onCreate(savedInstanceState);
    }
}
