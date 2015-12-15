package mrtech.router_demo;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import mrtech.smarthome.ipc.IPCManager;
import mrtech.smarthome.router.Router;
import mrtech.smarthome.router.RouterManager;

public class LoadingActivity extends Activity {

    private TextView loadingLog;
    private String log="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_loading);
        initView();
        log=getText(R.string.loading).toString();
        new AsyncTask<Void,String,Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                publishProgress("加载路由组件....");
                RouterManager.init();
                publishProgress("加载摄像头组件...");
                IPCManager.getInstance().init();
                publishProgress("初始化完毕...");
                return null;
            }

            @Override
            protected void onProgressUpdate(String... values) {
                super.onProgressUpdate(values);
                addLog(values[0]);
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                addLog("进入程序...");
                Intent intent=new Intent(LoadingActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        }.execute();
    }

    private void initView() {
        loadingLog = (TextView) findViewById(R.id.txtLoadingLogs);
    }

    private void addLog(String mes){
        log+="\n"+mes;
        loadingLog.post(new Runnable() {
            @Override
            public void run() {
                loadingLog.setText(log);
            }
        });
    }
}
