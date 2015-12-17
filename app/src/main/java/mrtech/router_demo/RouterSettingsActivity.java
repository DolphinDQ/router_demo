package mrtech.router_demo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Parcel;
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
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import mrtech.smarthome.router.Router;
import mrtech.smarthome.router.RouterManager;
import rx.Subscription;
import rx.functions.Action1;

public class RouterSettingsActivity extends AppCompatActivity {

    private ArrayAdapter<Router> routerArrayAdapter;
    private RouterManager routerManager;
    private Subscription stateChangedHandle;
    private boolean readyExit;
    private RouterSettingsActivity mContext;
    private NotificationManager mNotificationManager;
    private Notification mNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("create", "create" + hashCode());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_router_settings);
        routerManager = RouterManager.getInstance();
//        mContext = this;
//        mNotification = new Notification(R.drawable.ic_menu_manage,"弹窗弹窗弹窗弹窗弹窗.",System.currentTimeMillis());
//        mNotificationManager = (NotificationManager)this.getSystemService(NOTIFICATION_SERVICE);
//        statusNotification();
        stateChangedHandle = routerManager.subscribeRouterStatusChanged(new Action1<Router>() {
            @Override
            public void call(final Router router) {
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

    public void statusNotification() {

        Intent mIntent = new Intent(mContext, RouterSettingsActivity.class);
        //这里需要设置Intent.FLAG_ACTIVITY_NEW_TASK属性
        mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent mContentIntent = PendingIntent.getActivity(mContext, 0, mIntent, 0);
        //1，使用setLatestEventInfo
        //这里必需要用setLatestEventInfo(上下文,标题,内容,PendingIntent)不然会报错.
        //  mNotification.setLatestEventInfo(mContext, "新消息", "主人，您孙子给你来短息了", mContentIntent);


        //2,使用远程视图
        mNotification.contentView = new RemoteViews(this.getPackageName(), R.layout.layout_notification);
        mNotification.contentView.setImageViewResource(R.id.status_icon, R.drawable.ic_menu_manage);
        mNotification.contentView.setTextViewText(R.id.status_text, "This is test content");

        //使用默认的声音，闪屏，振动效果
        //  mNotification.defaults = Notification.DEFAULT_ALL;
        //  mNotification.defaults = Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE;

        //添加震动
        long[] vibreate = new long[]{1000, 1000, 1000, 1000};
        mNotification.vibrate = vibreate;

        //添加led
        mNotification.ledARGB = Color.BLUE;
        mNotification.ledOffMS = 0;
        mNotification.ledOnMS = 1;
        mNotification.flags = mNotification.flags | Notification.FLAG_SHOW_LIGHTS;

        //手动设置contentView属于时，必须同时也设置contentIntent不然会报错
        mNotification.contentIntent = mContentIntent;

        //触发通知(消息ID,通知对象)
        mNotificationManager.notify(1, mNotification);
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
    protected void onStop() {
        stateChangedHandle.unsubscribe();
        super.onStop();
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
                convertView.findViewById(R.id.delete_btn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        routerManager.removeRouter(router);
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
}
