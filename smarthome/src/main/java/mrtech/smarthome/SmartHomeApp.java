package mrtech.smarthome;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import mrtech.smarthome.auth.UserManager;
import mrtech.smarthome.router.RouterManager;

/**
 * Created by sphynx on 2016/1/7.
 */
public class SmartHomeApp extends Application {
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
