package mrtech.router_demo;

import android.app.Application;
import android.util.Log;

import mrtech.smarthome.ipc.IPCManager;
import mrtech.smarthome.router.Router;
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
        addRouter("S5K8B7-JIYYQR-Z2KKME-XEENI0-99NX42-MLE");
        addRouter("T8QCY8-S3HLCS-YSJK2G-RUR057-W1BR09-76T");
    }
    private void addRouter(String sn) {
        RouterManager.getInstance().addRouter(new Router(null, "路由器", sn));
    }
    @Override
    public void onTerminate() {
        IPCManager.destroy();
        RouterManager.destroy();
        super.onTerminate();
    }
}
