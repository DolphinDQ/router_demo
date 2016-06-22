package mrtech.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.format.Time;
import android.util.Log;
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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import mrtech.models.RouterListItemData;
import mrtech.services.RouterQueryTimelineService;
import mrtech.smarthome.auth.AuthConfig;
import mrtech.smarthome.auth.UserManager;
import mrtech.smarthome.ipc.IPCManager;
import mrtech.smarthome.ipc.IPCamera;
import mrtech.smarthome.ipc.Models.IPCStateChanged;
import mrtech.smarthome.ipc.Models.IPCStatus;
import mrtech.smarthome.router.Models.CommunicationManager;
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
    private Subscription mDownloadTask;
    private TextView mDownLoadLog;

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
        mDownLoadLog = (TextView) findViewById(R.id.download_txt);
        findViewById(R.id.user_report_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentRouter == null || !mCurrentRouter.getRouterSession().isAuthenticated()) {
                    Toast.makeText(MainActivity.this, "当前未连接路由器，请选择需要反馈路由器...", Toast.LENGTH_SHORT).show();
                } else {
//                    final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
//
//                    final EditText et = new EditText(mContext);
//                    builder.setTitle("请输入问题内容")
//                            .setView(et)
//                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    collectReport(et.getText().toString());
//                                }
//                            }).setNeutralButton("取消", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//
//                        }
//                    });
//                    builder.create().show();
                    collectReport("");
                }
            }
        });
    }

    private void show(final String mes) {
        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                mDownLoadLog.setText(mes);
//                Toast.makeText(MainActivity.this, mes, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 发送反馈报告。
     */
    private void collectReport(final String mes) {
        if (mCurrentRouter != null) {
            final ArrayList<Byte> data = new ArrayList<>();
            final CommunicationManager communicationManager = mCurrentRouter.getRouterSession().getCommunicationManager();
            show("正在收集数据...请稍后...");
            communicationManager.postRequestAsync(RequestUtil.collectDiagnosticInfo(), new Action2<Messages.Response, Throwable>() {
                @Override
                public void call(Messages.Response response, Throwable throwable) {
                    if (throwable != null) {
                        show("收集数据出错..." + throwable.getMessage());
                    } else {
                        if (response == null) {
                            show("收集数据失败...请更新您的路由器固件版本...");
                            return;
                        }
                        final Messages.CollectDiagnosticInfoResponse collectDiagnosticInfoResponse = response.getExtension(Messages.CollectDiagnosticInfoResponse.response);
                        final int streamId = collectDiagnosticInfoResponse.getStreamId();
                        show("正在下载数据...");
                        mDownloadTask = communicationManager.subscribeStream(streamId, new Action2<Messages.StreamMultiplexingUnit, Throwable>() {
                            @Override
                            public void call(Messages.StreamMultiplexingUnit streamMultiplexingUnit, Throwable throwable) {
                                if (throwable != null) {
                                    show("下载异常:" + throwable);
                                } else {
                                    switch (streamMultiplexingUnit.getType()) {
                                        case DATA:
                                            //TODO 获取数据
                                            final byte[] bytes = streamMultiplexingUnit.getData().toByteArray();
                                            show("下载数据:" + bytes.length);
                                            for (byte b : bytes) {
                                                data.add(b);
                                            }
                                            return;
                                        case ABORT:
                                            show("下载失败...");
                                            data.clear();
                                            break;
                                        case CLOSE:
                                            show("下载完毕...");
                                            processReport(mes, data);
                                            //TODO 下载完毕 数据超过10M  不发邮件 发送失败Toast
                                            break;
                                        default:
                                            return;
                                    }
                                }
                                if (mDownloadTask != null) mDownloadTask.unsubscribe();
                            }
                        });
                    }
                }
            }, 1000 * 60);

        }
    }

    private void processReport(String mes, ArrayList<Byte> data) {
        if (data == null || data.size() == 0) {
            show("数据采集失败，请重新尝试下载...");
        } else {
            show("获取到数据:" + data.size() / (1024) + "KB");
            final Date date = new Date(System.currentTimeMillis());
            final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_hh_MM_ss");
            String file = "/sdcard/MR088_Info_" + simpleDateFormat.format(date) + ".bak";
            FileOutputStream fout = null;
            try {
                fout = new FileOutputStream(file, false);
                byte[] bytes = new byte[data.size()];
                for (int i = 0; i < data.size(); i++) {
                    bytes[i] = data.get(i);
                }
                fout.write(bytes);
                fout.close();
                show("保存成功:" + file);
            } catch (Exception e) {
                e.printStackTrace();
                show("保存失败");
            }
        }
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
                                    .setExtension(Models.ExtensionCommand.newBuilder().setOpcodeId(infraredOpCode.getId())).build(),false);
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
//        mCameraBtn.setEnabled(false);
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
                try {
                    Router router = null;
                    router.getRouterSession();
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
            if (src == null) return;
            src.setActive(true);
            setCameraBtnState();
            setInfraredControlBtnState();
            setDoorLockBtnState();
        } else {
//            mCameraBtn.setEnabled(false);
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
                        if (response == null) return;
                        final Messages.QueryDeviceResponse extension = response.getExtension(Messages.QueryDeviceResponse.response);
                        if (extension == null) return;
                        mInfraredDeviceList = extension.getResultsList();
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
//                mCameraBtn.setEnabled(validCount > 0);
            }
        });
    }

}
