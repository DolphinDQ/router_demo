package mrtech.router_demo;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.IllegalFormatException;
import java.util.concurrent.TimeUnit;

import mrtech.smarthome.router.Router;
import mrtech.smarthome.router.RouterManager;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

public class RouterSettingsActivity extends AppCompatActivity {

    private boolean readyExit;
    private ArrayAdapter<Router> routerArrayAdapter;
    private RouterManager routerManager;
    private Subscription stateChangedHandle;
    private PublishSubject<String> subject = PublishSubject.create();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("mrtechLOG", "onCreate" + hashCode());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_router_settings);
        routerManager = RouterManager.getInstance();
        if (stateChangedHandle == null)
            stateChangedHandle = routerManager.getEventManager().subscribeRouterStatusChangedEvent(new Action1<Router>() {
                @Override
                public void call(final Router router) {
                    if (router.getRouterSession().isAuthenticated()) {
                        router.getRouterSession().getCameraManager().reloadIPCAsync(false, new Action1<Throwable>() {
                            @Override
                            public void call(final Throwable throwable) {
                                new Handler(getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (throwable != null) {
                                            throwable.printStackTrace();
                                            Toast.makeText(RouterSettingsActivity.this, router.getName() + "加载摄像头失败。" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(RouterSettingsActivity.this, router.getName() + "加载摄像头完毕。", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        });
                    }
                    new Handler(getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (routerArrayAdapter != null)
                                routerArrayAdapter.notifyDataSetChanged();
                        }
                    });
                }
            });
        initView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.router_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add_router) {
            IntentIntegrator integrator = new IntentIntegrator(RouterSettingsActivity.this);
            integrator.setCaptureActivity(RouterCaptureActivity.class);
            integrator.initiateScan();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        final IntentResult code = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (code != null) {
            addRouter(code.getContents());
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean isDestroyed() {
        stateChangedHandle.unsubscribe();
        return super.isDestroyed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!readyExit) {
                readyExit = true;
                Toast.makeText(this, "再按一下退出", Toast.LENGTH_SHORT).show();
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        try {
                            Thread.sleep(1500);
                        } catch (InterruptedException e) {
                        }
                        readyExit = false;
                        return null;
                    }
                }.execute();
                return true;
            } else {
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void initView() {
        final ListView routerList = (ListView) findViewById(R.id.router_list);
//        findViewById(R.id.post_btn).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                post();
//            }
//        });
        routerArrayAdapter = new ArrayAdapter<Router>(this, R.layout.layout_router_list_item, routerManager.getRouterList()) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(this.getContext())
                            .inflate(R.layout.layout_router_list_item, parent, false);
                }
                final Router router = getItem(position);
                ((TextView) convertView.findViewById(R.id.router_name)).setText(router.getName());
                ((TextView) convertView.findViewById(R.id.router_state)).setText(router.getRouterSession().getRouterStatus().toString());
//                convertView.findViewById(R.id.edit_btn).setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//
//                    }
//                });
                convertView.findViewById(R.id.camera_btn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        new AsyncTask<Void, Void, Models.SystemConfiguration>() {
//                            @Override
//                            protected Models.SystemConfiguration doInBackground(Void... params) {
//                                return router.getRouterSession().getRouterConfiguration(true);
//                            }
//
//                            @Override
//                            protected void onPostExecute(Models.SystemConfiguration configuration) {
//                                if (configuration != null) {
//                                    Toast.makeText(RouterSettingsActivity.this, configuration.getDeviceName() + configuration.getTime(), Toast.LENGTH_SHORT).show();
//                                }
//                            }
//                        }.execute();
                        final Intent intent = new Intent(RouterSettingsActivity.this, IPCListActivity.class);
                        intent.setAction(router.getSN());
                        startActivity(intent);
                    }
                });
                convertView.findViewById(R.id.delete_btn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        routerManager.removeRouter(router, true);
                    }
                });
                return convertView;
            }
        };
        routerList.setAdapter(routerArrayAdapter);
    }

    private void addRouter(String sn) {
        routerManager.addRouter(new Router(null, "路由器", sn));
    }

    private void post() {
//        routerManager.getRouterList().get(0).getRouterSession().postRequestToQueue(RequestUtil.getSysConfig(), new Action1<Messages.Response>() {
//            @Override
//            public void call(final Messages.Response response) {
//                new Handler(getMainLooper()).post(new Runnable() {
//                    @Override
//                    public void run() {
//                        final String deviceName = response.getExtension(Messages.GetSystemConfigurationResponse.response).getConfiguration().getDeviceName();
//                        Toast.makeText(RouterSettingsActivity.this,deviceName, Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
//        });
//      routerManager.getRouterList().get(0).getRouterSession().alarmTest();
    }
}
