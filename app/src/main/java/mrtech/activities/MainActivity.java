package mrtech.activities;

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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import mrtech.models.RouterListItemData;
import mrtech.services.RouterQueryTimelineService;
import mrtech.smarthome.*;
import mrtech.smarthome.BuildConfig;
import mrtech.smarthome.auth.AuthConfig;
import mrtech.smarthome.auth.UserManager;
import mrtech.smarthome.ipc.IPCManager;
import mrtech.smarthome.ipc.IPCamera;
import mrtech.smarthome.ipc.Models.IPCStateChanged;
import mrtech.smarthome.ipc.Models.IPCStatus;
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
    private Router mCurrentRouter;
    private final HashMap<String, Subscription> mSubscriptions = new HashMap<>();
    private RouterManager mRouterManager;
    private Subscription cameraStatusChanged;
    private Models.Device mIFDev;
    private Button mIRCtrlBtn;
    private Button mCameraBtn;
    private Button mRouterConfigBtn;
    private List<Models.Device> mInfraredDeviceList;
    private Button mLockListBtn;
    private List<Models.Device> mLockList;

    protected void onCreate(Bundle savedInstanceState) {
        mContext = this;
        mUserManager = UserManager.getInstance();
        mRouterManager = RouterManager.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initContent();
        initToolBar();
        initNavBar();
        setCurrentRouter(getCacheData(Router.class));
    }

    private void initContent() {
        initCameraControl();
        initInfraredControl();
        initRouterConfigControl();
        initLocks();

    }

    private void initLocks() {
        mLockListBtn = (Button) findViewById(R.id.lock_list);
        mLockListBtn.setEnabled(false);
        mLockListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, LockListActivity.class));
            }
        });
    }

    private void initRouterConfigControl() {
        mRouterConfigBtn = (Button) findViewById(R.id.router_config_btn);
        mRouterConfigBtn.setText("测试红外设备");
        mRouterConfigBtn.setEnabled(false);
        mRouterConfigBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIFDev != null) {
                    final Models.InfraredOpCode infraredOpCode = mIFDev.getExtension(Models.InfraredDevice.detail).getOpcodesList().get(0);
                    final Messages.Request request = RequestUtil
                            .sendIrCommand(infraredOpCode.getDeviceId(), Models.InfraredCommand.newBuilder()
                                    .setExtension(Models.ExtensionCommand.newBuilder().setOpcodeId(infraredOpCode.getId())).build());
                    mCurrentRouter.getRouterSession().getCommunicationManager().postRequestAsync(request, new Action2<Messages.Response, Throwable>() {
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
                    mCurrentRouter.getRouterSession().getCommunicationManager().postRequestAsync(request, new Action2<Messages.Response, Throwable>() {
                        @Override
                        public void call(final Messages.Response response, final Throwable throwable) {
                            new Handler(getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    if (throwable != null) {
                                        Toast.makeText(MainActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                    } else {

                                        final List<Models.Device> resultsList = response.getExtension(Messages.QueryDeviceResponse.response).getResultsList();
                                        Toast.makeText(MainActivity.this, "获取到红外设备:" + resultsList.size(), Toast.LENGTH_SHORT).show();
                                        for (Models.Device device : resultsList) {
                                            if (device.getExtension(Models.InfraredDevice.detail).getOpcodesCount() > 0)
                                                mIFDev = device;
                                        }
                                    }
                                }
                            });
                        }
                    });
                }
            }
        });
    }

    private void initCameraControl() {
        mCameraBtn = (Button) findViewById(R.id.camera_btn);
        mCameraBtn.setEnabled(false);
        mCameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, IPCListActivity.class));
            }
        });
    }

    private void initInfraredControl() {

        mIRCtrlBtn = (Button) findViewById(R.id.control_btn);
        mIRCtrlBtn.setEnabled(false);
        mIRCtrlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, InfraredControlActivity.class));
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
        final ArrayAdapter<Router> routerArrayAdapter = new ArrayAdapter<Router>(this, R.layout.item_router, mRouterManager.getRouterList()) {
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
                            .inflate(R.layout.item_router, parent, false);
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
                    if (mCurrentRouter == null && router.getRouterSession().isAuthenticated()) {
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
            case R.id.test_crash_report:
                Router router=null;
                router.getRouterSession();
                break;
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

    private void setCurrentRouter(final Router router) {
        if (router == mCurrentRouter) return;
        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                setRouterInactive(mCurrentRouter);
                mCurrentRouter = router;
                setRouterActive(mCurrentRouter);
            }
        });

    }

    private void setRouterInactive(final Router router) {
        mIFDev = null;
        if (router != null) {
            RouterListItemData src = router.getUserData(RouterListItemData.class);
            src.setActive(false);
        }
        BaseActivity.setCacheData(Router.class, null);
        if (cameraStatusChanged != null) {
            cameraStatusChanged.unsubscribe();
        }
    }

    /**
     * @param router
     */
    private void setRouterActive(final Router router) {
        BaseActivity.setCacheData(Router.class, router);
        final boolean hasRouter = router != null;
        setTitle(hasRouter ? router.getName() : getText(R.string.title_activity_main));
        mRouterConfigBtn.setEnabled(hasRouter);
        if (hasRouter) {
            RouterListItemData src = router.getUserData(RouterListItemData.class);
            src.setActive(true);
            setCameraBtnState();
            setInfraredControlBtnState();
            setDoorLockBtnState();
        } else {
            mCameraBtn.setEnabled(false);
            mIRCtrlBtn.setEnabled(false);
            mLockListBtn.setEnabled(false);
            mCameraBtn.setText(getText(R.string.camera));
            mIRCtrlBtn.setText(getText(R.string.infrared_control));
            mLockListBtn.setText(getText(R.string.door_lock));
        }
    }

    private void setDoorLockBtnState() {
        if (mCurrentRouter == null) return;
        Messages.Request request = RequestUtil.getDevices(Models.DeviceQuery
                .newBuilder()
                .setPage(0)
                .setPageSize(100)
                .setType(Models.DeviceType.DEVICE_TYPE_ZIGBEE)
                .build());
        mCurrentRouter.getRouterSession().getCommunicationManager().postRequestToQueue(request, new Action1<Messages.Response>() {
            @Override
            public void call(Messages.Response response) {
                final List<Models.Device> zigbeeDeviceList = response.getExtension(Messages.QueryDeviceResponse.response).getResultsList();
                mLockList = new ArrayList<>();
                for (Models.Device device : zigbeeDeviceList) {
                    final Models.ZigBeeDevice zigBeeDevice = device.getExtension(Models.ZigBeeDevice.detail);
                    if (zigBeeDevice.getDeviceId() == Models.DeviceId.DEVICE_ID_DOOR_LOCK) {
                        mLockList.add(device);
                    }
                }
                if (isActive())
                    setCacheData(LockListActivity.LOCK_LIST_KEY, mLockList);
                new Handler(getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        mLockListBtn.setEnabled(mLockList.size() > 0);
                        mLockListBtn.setText(getText(R.string.door_lock) + "-" + mLockList.size());
                    }
                });
            }
        });
    }

    private void setCameraBtnState() {
        final IPCManager ipcManager = mCurrentRouter.getRouterSession().getCameraManager().getIPCManager();
        if (cameraStatusChanged != null) cameraStatusChanged.unsubscribe();
        cameraStatusChanged = ipcManager.createEventManager(null).subscribeCameraStatus(new Action1<IPCStateChanged>() {
            @Override
            public void call(IPCStateChanged ipcStateChanged) {
                setCameraCount(ipcManager.getCameraList());
            }
        });
        setCameraCount(ipcManager.getCameraList());
    }

    private void setInfraredControlBtnState() {
        if (mCurrentRouter == null) return;
        Messages.Request request = RequestUtil.getDevices(Models.DeviceQuery
                .newBuilder()
                .setPage(0)
                .setPageSize(100)
                .setType(Models.DeviceType.DEVICE_TYPE_INFRARED)
                .build());
        mCurrentRouter.getRouterSession().getCommunicationManager().postRequestToQueue(request, new Action1<Messages.Response>() {
                    @Override
                    public void call(Messages.Response response) {
                        mInfraredDeviceList = response.getExtension(Messages.QueryDeviceResponse.response).getResultsList();
                        final boolean hasDevices = mInfraredDeviceList.size() > 0;
                        if (isActive())
                            setCacheData(InfraredControlActivity.IR_LIST_KEY, mInfraredDeviceList);
                        new Handler(getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                mIRCtrlBtn.setEnabled(hasDevices);
                                mIRCtrlBtn.setText(getText(R.string.infrared_control) + "-" + mInfraredDeviceList.size());
                            }
                        });
                    }
                }
        );
    }

    private void setCameraCount(final List<IPCamera> cameras) {
        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                int validCount = 0;
                for (IPCamera camera : cameras) {
                    if (camera.getIpcContext().getStatus() == IPCStatus.CONNECTED)
                        validCount++;
                }
                mCameraBtn.setText("" + getText(R.string.camera) + validCount + "/" + cameras.size());
                mCameraBtn.setEnabled(validCount > 0);
            }
        });
    }

}
