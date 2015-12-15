package mrtech.router_demo;

import android.app.Application;
import android.util.Log;

import mrtech.smarthome.ipc.IPCManager;
import mrtech.smarthome.router.RouterManager;

/**
 * Created by sphynx on 2015/12/11.
 */
public class App extends Application {
    private static App instance;

    public static App getInstance() {
        return instance;
    }

    public App() {
        if (instance != null)
            Log.e("Application", "!!!!!!!!!!!");
        instance = this;
    }

    @Override
    public void onTerminate() {
        IPCManager.destroy();
        RouterManager.destroy();
        super.onTerminate();
    }
}
