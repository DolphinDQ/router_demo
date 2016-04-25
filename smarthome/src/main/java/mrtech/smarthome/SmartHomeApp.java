package mrtech.smarthome;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import mrtech.smarthome.auth.UserManager;
import mrtech.smarthome.router.RouterManager;

/**
 * 默认App，如不继承当前Application 请自行添加配置。
 * Created by sphynx on 2016/1/7.
 */
public class SmartHomeApp extends Application {
   public static boolean DEBUG=false;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        //安装编译兼容模块。
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        RouterManager.init(this);
        UserManager.getInstance().init(this);
    }

    @Override
    public void onTerminate() {
        RouterManager.destroy();
        super.onTerminate();
    }
}
