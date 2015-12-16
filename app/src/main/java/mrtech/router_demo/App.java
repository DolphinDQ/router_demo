package mrtech.router_demo;

import android.app.Application;
import android.util.Log;

import mrtech.smarthome.ipc.IPCManager;
import mrtech.smarthome.router.RouterManager;
import mrtech.smarthome.rpc.Messages;
import mrtech.smarthome.rpc.Models;


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
        RouterManager.init();
        IPCManager.init();
    }

    @Override
    public void onTerminate() {
        IPCManager.destroy();
        RouterManager.destroy();
        super.onTerminate();
    }
}
