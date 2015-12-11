package mrtech.router_demo;

import android.app.Application;
import android.util.Log;

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
}
