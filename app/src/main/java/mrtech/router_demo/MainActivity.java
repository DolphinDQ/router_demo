package mrtech.router_demo;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import mrtech.models.RouterListItemData;
import mrtech.smarthome.auth.AuthConfig;
import mrtech.smarthome.auth.UserManager;
import mrtech.smarthome.ipc.IPCManager;
import mrtech.smarthome.ipc.IPCModels;
import mrtech.smarthome.ipc.IPCamera;
import mrtech.smarthome.router.Router;
import mrtech.smarthome.router.RouterManager;
import mrtech.smarthome.rpc.Messages;
import mrtech.smarthome.rpc.Models;
import mrtech.smarthome.util.RequestUtil;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Action2;

public class MainActivity extends BaseActivity {
    private boolean readyExit;
    private Context mContext;
    private UserManager mUserManager;
    private Router currentRouter;
    private final HashMap<String, Subscription> mSubscriptions = new HashMap<>();
    private RouterManager mRouterManager;
    private Subscription cameraStatusChanged;
    private Models.Device mIFDev;

    protected void onCreate(Bundle savedInstanceState) {
        mContext = this;
        mUserManager = UserManager.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setCurrentRouter(getDefaultData(Router.class));
        initToolBar();
        initNavBar();
        initContent();
    }

    private void initContent() {
        final View cameraBtn = findViewById(R.id.camera_btn);
        final Router router = BaseActivity.getDefaultData(Router.class);
        cameraBtn.setEnabled(router != null);
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, IPCListActivity.class));
            }
        });

        final Button routerConfigBtn = (Button) findViewById(R.id.router_config_btn);
        routerConfigBtn.setEnabled(router != null);
        routerConfigBtn.setText("测试红外设备");
        routerConfigBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( mIFDev != null) {
                    final Models.InfraredOpCode infraredOpCode = mIFDev.getExtension(Models.InfraredDevice.detail).getOpcodesList().get(0);
                    final Messages.Request request = RequestUtil
                            .sendIrCommand(infraredOpCode.getDeviceId(), Models.InfraredCommand.newBuilder()
                                    .setExtension(Models.ExtensionCommand.newBuilder().setOpcodeId(infraredOpCode.getId())).build());
                    currentRouter.getRouterSession().getCommunicationManager().postRequestAsync(request, new Action2<Messages.Response, Throwable>() {
                        @Override
                        public void call(Messages.Response response, final Throwable throwable) {
                            new Handler(getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    if (throwable != null) {
                                        Toast.makeText(MainActivity.this, "发送失败" + throwable.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(MainActivity.this, "发送成功", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    });

                } else {
                    Messages.Request request = RequestUtil.getDevices(Models.DeviceQuery
                            .newBuilder()
                            .setPage(0)
                            .setPageSize(100)
                            .setType(Models.DeviceType.DEVICE_TYPE_INFRARED)
                            .build());
                    currentRouter.getRouterSession().getCommunicationManager().postRequestAsync(request, new Action2<Messages.Response, Throwable>() {
                        @Override
                        public void call(final Messages.Response response, Throwable throwable) {
                            new Handler(getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    final List<Models.Device> resultsList = response.getExtension(Messages.QueryDeviceResponse.response).getResultsList();
                                    Toast.makeText(MainActivity.this, "获取到红外设备:" + resultsList.size(), Toast.LENGTH_SHORT).show();
                                    for (Models.Device device : resultsList) {
                                        if (device.getExtension(Models.InfraredDevice.detail).getOpcodesCount() > 0)
                                            mIFDev = device;
                                    }
                                }
                            });
                        }
                    });
                }
            }

        });
    }


    private void initToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
    }

    private void initNavBar() {

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
//        navigationView.setNavigationItemSelectedListener(this);

        final View navigationHeaderView = navigationView;
        navigationHeaderView.findViewById(R.id.user_face).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUserManager.isLogin()) {
                    Toast.makeText(MainActivity.this, "已登录", Toast.LENGTH_SHORT).show();
                } else {
                    startActivity(new Intent(mContext, LoginActivity.class));
                }
            }
        });
        if (!mSubscriptions.containsKey("subscribeConfigChanged"))
            mSubscriptions.put("subscribeConfigChanged", mUserManager.subscribeConfigChanged(new Action1<AuthConfig>() {
                @Override
                public void call(final AuthConfig authConfig) {
                    new Handler(getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            final TextView userAccount = (TextView) navigationHeaderView.findViewById(R.id.user_account);
                            final TextView loginTime = (TextView) navigationHeaderView.findViewById(R.id.login_time);
                            if (mUserManager.isLogin()) {
                                userAccount.setText(authConfig.getUser());
                                loginTime.setText(new Date(authConfig.getLoginTime()).toLocaleString());
                            } else {
                                userAccount.setText("<未登录>");
                                loginTime.setText("");
                            }
                        }
                    });
                }
            }));

        final ListView routerList = (ListView) navigationView.findViewById(R.id.router_list);
        mRouterManager = RouterManager.getInstance();
        final ArrayAdapter<Router> routerArrayAdapter = new ArrayAdapter<Router>(this, R.layout.layout_router_list_item, mRouterManager.getRouterList()) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                final Router router = getItem(position);
                RouterListItemData source = router.getUserData(RouterListItemData.class);
                if (source == null) {
                    source = new RouterListItemData();
                    router.setUserData(RouterListItemData.class, source);
                }
                if (convertView == null) {
                    convertView = LayoutInflater.from(this.getContext())
                            .inflate(R.layout.layout_router_list_item, parent, false);
                }
                String routerName = router.getName();

                ((TextView) convertView.findViewById(R.id.router_name)).setText(routerName + (source.isActive() ? "*" : ""));
                ((TextView) convertView.findViewById(R.id.router_state)).setText(router.getRouterSession().getRouterStatus().toString());
                convertView.findViewById(R.id.camera_btn).setVisibility(View.GONE);
                convertView.findViewById(R.id.delete_btn).setVisibility(View.GONE);
                return convertView;
            }
        };

        routerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                view.setSelected(true);
                Router router = routerArrayAdapter.getItem(position);
                setCurrentRouter(router);
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                routerArrayAdapter.notifyDataSetChanged();
            }
        });

        if (!mSubscriptions.containsKey("subscribeRouterStatusChangedEvent"))
            mSubscriptions.put("subscribeRouterStatusChangedEvent", mRouterManager.getEventManager().subscribeRouterStatusChangedEvent(new Action1<Router>() {
                @Override
                public void call(Router router) {
                    if (currentRouter == null && router.getRouterSession().isAuthenticated()) {
                        setCurrentRouter(router);
                    }
                    new Handler(getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            routerArrayAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }));
        routerList.setAdapter(routerArrayAdapter);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(this, RouterSettingsActivity.class));
                return true;
            case R.id.action_logoff:
                mUserManager.logoff(null);
                return true;
            case R.id.action_refresh_router:
                mRouterManager.removeAllRouters(false);
                setCurrentRouter(null);
                mRouterManager.loadRouters();
                return true;
            case R.id.action_exit:
                RouterQueryTimelineService.setTerminate(true);
                android.os.Process.killProcess(android.os.Process.myPid());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean isDestroyed() {
        for (Subscription subscription : mSubscriptions.values()) {
            subscription.unsubscribe();
        }
        mSubscriptions.clear();
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

    public void setCurrentRouter(final Router router) {
        if (router == currentRouter) return;
        routerInactive(currentRouter);
        currentRouter = router;
        routerActive(currentRouter);

    }

    private void routerInactive(final Router router) {
        mIFDev = null;
        if (router != null) {
            RouterListItemData src = router.getUserData(RouterListItemData.class);
            src.setActive(false);
        }
        BaseActivity.setDefaultData(Router.class, null);
        if (cameraStatusChanged != null) {
            cameraStatusChanged.unsubscribe();
        }
    }

    private void routerActive(final Router router) {
        BaseActivity.setDefaultData(Router.class, router);
        final boolean turnOn = router != null;
        if (turnOn) {
            RouterListItemData src = router.getUserData(RouterListItemData.class);
            src.setActive(true);
        }
        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                setTitle(turnOn ? router.getName() : getText(R.string.title_activity_main));
                findViewById(R.id.router_config_btn).setEnabled(turnOn);
                if (turnOn) {
                    final IPCManager ipcManager = router.getRouterSession().getCameraManager().getIPCManager();
                    cameraStatusChanged = ipcManager.createEventManager(null).subscribeCameraStatus(new Action1<IPCModels.IPCStateChanged>() {
                        @Override
                        public void call(IPCModels.IPCStateChanged ipcStateChanged) {
                            setCameraCount(ipcManager.getCameraList());
                        }
                    });
                    setCameraCount(ipcManager.getCameraList());
                } else {
                    findViewById(R.id.camera_btn).setEnabled(false);
                    ((Button) findViewById(R.id.camera_btn)).setText(getText(R.string.camera));
                }
            }
        });
    }

    private void setCameraCount(final List<IPCamera> cameras) {
        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                int validCount = 0;
                for (IPCamera camera : cameras) {
                    if (camera.getIpcContext().getStatus() == IPCModels.IPCStatus.CONNECTED)
                        validCount++;
                }
                final Button button = (Button) findViewById(R.id.camera_btn);
                button.setText("" + getText(R.string.camera) + validCount + "/" + cameras.size());
                button.setEnabled(validCount > 0);
            }
        });


    }


}
