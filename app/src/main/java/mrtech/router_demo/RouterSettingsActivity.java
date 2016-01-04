package mrtech.router_demo;

import android.accounts.AuthenticatorException;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Debug;
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

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.okhttp.Request;

import java.lang.reflect.Type;
import java.net.Proxy;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import mrtech.smarthome.auth.Models.ApiCallback;
import mrtech.smarthome.auth.Models.PageResult;
import mrtech.smarthome.auth.Models.RouterCloudData;
import mrtech.smarthome.auth.Models.UserDetail;
import mrtech.smarthome.auth.UserManager;
import mrtech.smarthome.router.Router;
import mrtech.smarthome.router.RouterManager;
import mrtech.smarthome.util.Constants;
import rx.Subscription;
import rx.functions.Action1;

public class RouterSettingsActivity extends AppCompatActivity {

    private boolean readyExit;
    private ArrayAdapter<Router> routerArrayAdapter;
    private RouterManager routerManager;
    private Subscription stateChangedHandle;
    private UserManager userManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("mrtechLOG", "onCreate" + hashCode());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_router_settings);
        routerManager = RouterManager.getInstance();
        userManager = UserManager.getInstance();
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

    private boolean isLogon;

    private <T> Type getType(T cls){
        return new TypeToken<T>(){}.getType();
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
        findViewById(R.id.post_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String json="{\"Data\":{\"Result\":[{\"ConfigFileURL\":\"\",\"Name\":\"router\",\"ConnectionKey\":\"ZBXR81-WTX9IU-YIIKC7-BHDJSQ-VY7BXE-KTF\",\"RouterUserId\":23,\"LastConfigurationBackup\":\"2015-12-18T10:02:06.887\",\"BackupFailed\":false,\"AutoBackup\":false,\"ID\":15},{\"ConfigFileURL\":\"\",\"Name\":\"router\",\"ConnectionKey\":\"2T24IA-979RT4-HITATI-S25IYH-DMS6ZO-7SB\",\"RouterUserId\":1014,\"LastConfigurationBackup\":null,\"BackupFailed\":false,\"AutoBackup\":false,\"ID\":1008},{\"ConfigFileURL\":\"\",\"Name\":\"router\",\"ConnectionKey\":\"7IR495-H7TA10-A1XPND-BZWJKY-GOMBEK-8BW\",\"RouterUserId\":2,\"LastConfigurationBackup\":null,\"BackupFailed\":false,\"AutoBackup\":false,\"ID\":1014},{\"ConfigFileURL\":\"\",\"Name\":\"router\",\"ConnectionKey\":\"ZBXR81-WTX9IU-YIIKC7-BHDJSQ-VY7BXE-KTF\",\"RouterUserId\":1018,\"LastConfigurationBackup\":\"2015-12-18T09:01:54.63\",\"BackupFailed\":false,\"AutoBackup\":false,\"ID\":1055},{\"ConfigFileURL\":\"\",\"Name\":\"router\",\"ConnectionKey\":\"T8GNPK-IG86SR-S5QYXQ-YJF3OJ-G73UYH-NQP\",\"RouterUserId\":1023,\"LastConfigurationBackup\":\"2015-11-23T16:53:45.487\",\"BackupFailed\":false,\"AutoBackup\":false,\"ID\":1101},{\"ConfigFileURL\":\"\",\"Name\":\"router\",\"ConnectionKey\":\"S5K8B7-JIYYQR-Z2KKME-XEENI0-99NX42-MLE\",\"RouterUserId\":1028,\"LastConfigurationBackup\":null,\"BackupFailed\":false,\"AutoBackup\":false,\"ID\":1135},{\"ConfigFileURL\":\"\",\"Name\":\"router\",\"ConnectionKey\":\"Z22B6I-U1EPG9-X503FD-WASYUP-XOSEGC-JKZ\",\"RouterUserId\":1030,\"LastConfigurationBackup\":null,\"BackupFailed\":false,\"AutoBackup\":false,\"ID\":1206},{\"ConfigFileURL\":\"\",\"Name\":\"router\",\"ConnectionKey\":\"CSCC4A-FVFJT7-FB7TWF-EH0PBL-FV050S-6ZN\",\"RouterUserId\":1030,\"LastConfigurationBackup\":null,\"BackupFailed\":false,\"AutoBackup\":false,\"ID\":1208},{\"ConfigFileURL\":\"\",\"Name\":\"router\",\"ConnectionKey\":\"CSCC4A-FVFJT7-FB7TWF-EH0PBL-FV050S-6ZN\",\"RouterUserId\":1032,\"LastConfigurationBackup\":null,\"BackupFailed\":false,\"AutoBackup\":false,\"ID\":1209},{\"ConfigFileURL\":\"\",\"Name\":\"家\",\"ConnectionKey\":\"S5K8B7-JIYYQR-Z2KKME-XEENI0-99NX42-MLE\",\"RouterUserId\":1033,\"LastConfigurationBackup\":null,\"BackupFailed\":false,\"AutoBackup\":false,\"ID\":1221},{\"ConfigFileURL\":\"\",\"Name\":\"router\",\"ConnectionKey\":\"JSKE8Y-X5FLNW-M0IO1S-I4MURT-O7VO79-H7B\",\"RouterUserId\":5,\"LastConfigurationBackup\":null,\"BackupFailed\":false,\"AutoBackup\":false,\"ID\":1233},{\"ConfigFileURL\":\"\",\"Name\":\"会议室\",\"ConnectionKey\":\"6YP84F-50XKQ2-DX2V4T-BZ8L6C-UAX9NY-9D2\",\"RouterUserId\":1033,\"LastConfigurationBackup\":null,\"BackupFailed\":false,\"AutoBackup\":false,\"ID\":1239},{\"ConfigFileURL\":\"\",\"Name\":\"会议室\",\"ConnectionKey\":\"6YP84F-50XKQ2-DX2V4T-BZ8L6C-UAX9NY-9D2\",\"RouterUserId\":1035,\"LastConfigurationBackup\":null,\"BackupFailed\":false,\"AutoBackup\":false,\"ID\":1240},{\"ConfigFileURL\":\"\",\"Name\":\"家\",\"ConnectionKey\":\"S5K8B7-JIYYQR-Z2KKME-XEENI0-99NX42-MLE\",\"RouterUserId\":1035,\"LastConfigurationBackup\":null,\"BackupFailed\":false,\"AutoBackup\":false,\"ID\":1244},{\"ConfigFileURL\":\"\",\"Name\":\"会议室\",\"ConnectionKey\":\"6YP84F-50XKQ2-DX2V4T-BZ8L6C-UAX9NY-9D2\",\"RouterUserId\":1036,\"LastConfigurationBackup\":null,\"BackupFailed\":false,\"AutoBackup\":false,\"ID\":1247},{\"ConfigFileURL\":\"\",\"Name\":\"家\",\"ConnectionKey\":\"S5K8B7-JIYYQR-Z2KKME-XEENI0-99NX42-MLE\",\"RouterUserId\":1036,\"LastConfigurationBackup\":null,\"BackupFailed\":false,\"AutoBackup\":false,\"ID\":1249},{\"ConfigFileURL\":\"\",\"Name\":\"会议室\",\"ConnectionKey\":\"6YP84F-50XKQ2-DX2V4T-BZ8L6C-UAX9NY-9D2\",\"RouterUserId\":1037,\"LastConfigurationBackup\":null,\"BackupFailed\":false,\"AutoBackup\":false,\"ID\":2270},{\"ConfigFileURL\":\"\",\"Name\":\"毛\",\"ConnectionKey\":\"M1KHTR-SE27HN-MTZCK5-PLWEVO-KMGDI0-EMI\",\"RouterUserId\":1028,\"LastConfigurationBackup\":null,\"BackupFailed\":false,\"AutoBackup\":false,\"ID\":2272},{\"ConfigFileURL\":\"\",\"Name\":\"router\",\"ConnectionKey\":\"ULI2AK-OS0OCU-VGU455-XOPKDZ-Y6F8U6-KRP\",\"RouterUserId\":1,\"LastConfigurationBackup\":null,\"BackupFailed\":false,\"AutoBackup\":false,\"ID\":2297},{\"ConfigFileURL\":\"\",\"Name\":\"router\",\"ConnectionKey\":\"XFCGEO-JSND61-YSJK2B-WKLI7V-X1ZZOY-JZD\",\"RouterUserId\":2039,\"LastConfigurationBackup\":null,\"BackupFailed\":false,\"AutoBackup\":false,\"ID\":2318},{\"ConfigFileURL\":\"\",\"Name\":\"美淘鞋店\",\"ConnectionKey\":\"SOZGA6-ZCPYSB-IOT83P-P2MLOL-LFY81Z-57F\",\"RouterUserId\":16,\"LastConfigurationBackup\":null,\"BackupFailed\":false,\"AutoBackup\":false,\"ID\":2355},{\"ConfigFileURL\":\"\",\"Name\":\"工程部\",\"ConnectionKey\":\"Z22B6I-U1EPG9-X503FD-WASYUP-XOSEGC-JKZ\",\"RouterUserId\":1033,\"LastConfigurationBackup\":null,\"BackupFailed\":false,\"AutoBackup\":false,\"ID\":2360},{\"ConfigFileURL\":\"\",\"Name\":\"工程部\",\"ConnectionKey\":\"Z22B6I-U1EPG9-X503FD-WASYUP-XOSEGC-JKZ\",\"RouterUserId\":1036,\"LastConfigurationBackup\":null,\"BackupFailed\":false,\"AutoBackup\":false,\"ID\":2365},{\"ConfigFileURL\":\"\",\"Name\":\"MySmartHome\",\"ConnectionKey\":\"SOZGA6-ZCPYSB-IOT83P-P2MLOL-LFY81Z-57F\",\"RouterUserId\":1015,\"LastConfigurationBackup\":null,\"BackupFailed\":false,\"AutoBackup\":false,\"ID\":2366},{\"ConfigFileURL\":\"\",\"Name\":\"工程部\",\"ConnectionKey\":\"Z22B6I-U1EPG9-X503FD-WASYUP-XOSEGC-JKZ\",\"RouterUserId\":16,\"LastConfigurationBackup\":null,\"BackupFailed\":false,\"AutoBackup\":false,\"ID\":2368},{\"ConfigFileURL\":\"\",\"Name\":\"router\",\"ConnectionKey\":\"Z22B6I-U1EPG9-X503FD-WASYUP-XOSEGC-JKZ\",\"RouterUserId\":1035,\"LastConfigurationBackup\":null,\"BackupFailed\":false,\"AutoBackup\":false,\"ID\":2379},{\"ConfigFileURL\":\"\",\"Name\":\"MySmartHome\",\"ConnectionKey\":\"JSKE8Y-X5FLNW-M0IO1S-I4MURT-O7VO79-H7B\",\"RouterUserId\":1015,\"LastConfigurationBackup\":null,\"BackupFailed\":false,\"AutoBackup\":false,\"ID\":2382},{\"ConfigFileURL\":\"\",\"Name\":\"会议室\",\"ConnectionKey\":\"6YP84F-50XKQ2-DX2V4T-BZ8L6C-UAX9NY-9D2\",\"RouterUserId\":12,\"LastConfigurationBackup\":null,\"BackupFailed\":false,\"AutoBackup\":false,\"ID\":2383},{\"ConfigFileURL\":\"\",\"Name\":\"家\",\"ConnectionKey\":\"S5K8B7-JIYYQR-Z2KKME-XEENI0-99NX42-MLE\",\"RouterUserId\":12,\"LastConfigurationBackup\":null,\"BackupFailed\":false,\"AutoBackup\":false,\"ID\":2384},{\"ConfigFileURL\":\"\",\"Name\":\"工程部\",\"ConnectionKey\":\"Z22B6I-U1EPG9-X503FD-WASYUP-XOSEGC-JKZ\",\"RouterUserId\":12,\"LastConfigurationBackup\":null,\"BackupFailed\":false,\"AutoBackup\":false,\"ID\":2386},{\"ConfigFileURL\":\"\",\"Name\":\"会议室\",\"ConnectionKey\":\"6YP84F-50XKQ2-DX2V4T-BZ8L6C-UAX9NY-9D2\",\"RouterUserId\":16,\"LastConfigurationBackup\":null,\"BackupFailed\":false,\"AutoBackup\":false,\"ID\":2387},{\"ConfigFileURL\":\"\",\"Name\":\"router\",\"ConnectionKey\":\"6YP84F-50XKQ2-DX2V4T-BZ8L6C-UAX9NY-9D2\",\"RouterUserId\":2035,\"LastConfigurationBackup\":null,\"BackupFailed\":false,\"AutoBackup\":false,\"ID\":2413},{\"ConfigFileURL\":\"\",\"Name\":\"router\",\"ConnectionKey\":\"Z22B6I-U1EPG9-X503FD-WASYUP-XOSEGC-JKZ\",\"RouterUserId\":5,\"LastConfigurationBackup\":null,\"BackupFailed\":false,\"AutoBackup\":false,\"ID\":2430},{\"ConfigFileURL\":\"\",\"Name\":\"router\",\"ConnectionKey\":\"6YP84F-50XKQ2-DX2V4T-BZ8L6C-UAX9NY-9D2\",\"RouterUserId\":2047,\"LastConfigurationBackup\":null,\"BackupFailed\":false,\"AutoBackup\":false,\"ID\":2435},{\"ConfigFileURL\":\"\",\"Name\":\"MySmartHome\",\"ConnectionKey\":\"T9S9WO-J07806-J8V6U4-LGQGQR-LY4Y8A-IDT\",\"RouterUserId\":12,\"LastConfigurationBackup\":null,\"BackupFailed\":false,\"AutoBackup\":false,\"ID\":2438},{\"ConfigFileURL\":\"\",\"Name\":\"工程部\",\"ConnectionKey\":\"Z22B6I-U1EPG9-X503FD-WASYUP-XOSEGC-JKZ\",\"RouterUserId\":2035,\"LastConfigurationBackup\":null,\"BackupFailed\":false,\"AutoBackup\":false,\"ID\":2444},{\"ConfigFileURL\":\"\",\"Name\":\"会议室\",\"ConnectionKey\":\"XER50Z-VH5EFU-Q5OSHW-JRPF88-0DJXHE-D8Q\",\"RouterUserId\":1031,\"LastConfigurationBackup\":null,\"BackupFailed\":false,\"AutoBackup\":false,\"ID\":2455},{\"ConfigFileURL\":\"\",\"Name\":\"工程部\",\"ConnectionKey\":\"GY68NY-6EN8RZ-E73UVP-DD7YUT-GZ4414-7S7\",\"RouterUserId\":1031,\"LastConfigurationBackup\":null,\"BackupFailed\":false,\"AutoBackup\":false,\"ID\":2458},{\"ConfigFileURL\":\"\",\"Name\":\"router\",\"ConnectionKey\":\"JSKE8Y-X5FLNW-M0IO1S-I4MURT-O7VO79-H7B\",\"RouterUserId\":2053,\"LastConfigurationBackup\":null,\"BackupFailed\":false,\"AutoBackup\":false,\"ID\":2463},{\"ConfigFileURL\":\"\",\"Name\":\"router\",\"ConnectionKey\":\"JSKE8Y-X5FLNW-M0IO1S-I4MURT-O7VO79-H7B\",\"RouterUserId\":4,\"LastConfigurationBackup\":null,\"BackupFailed\":false,\"AutoBackup\":false,\"ID\":2467},{\"ConfigFileURL\":\"\",\"Name\":\"router\",\"ConnectionKey\":\"JSKE8Y-X5FLNW-M0IO1S-I4MURT-O7VO79-H7B\",\"RouterUserId\":2054,\"LastConfigurationBackup\":null,\"BackupFailed\":false,\"AutoBackup\":false,\"ID\":2468},{\"ConfigFileURL\":\"\",\"Name\":\"router\",\"ConnectionKey\":\"JSKE8Y-X5FLNW-M0IO1S-I4MURT-O7VO79-H7B\",\"RouterUserId\":2055,\"LastConfigurationBackup\":null,\"BackupFailed\":false,\"AutoBackup\":false,\"ID\":2469},{\"ConfigFileURL\":\"\",\"Name\":\"我22\",\"ConnectionKey\":\"SOZGA6-ZCPYSB-IOT83P-P2MLOL-LFY81Z-57F\",\"RouterUserId\":2056,\"LastConfigurationBackup\":null,\"BackupFailed\":false,\"AutoBackup\":false,\"ID\":2473},{\"ConfigFileURL\":\"\",\"Name\":\"工程部\",\"ConnectionKey\":\"Z22B6I-U1EPG9-X503FD-WASYUP-XOSEGC-JKZ\",\"RouterUserId\":2056,\"LastConfigurationBackup\":null,\"BackupFailed\":false,\"AutoBackup\":false,\"ID\":2474},{\"ConfigFileURL\":\"\",\"Name\":\"我！！\",\"ConnectionKey\":\"JSKE8Y-X5FLNW-M0IO1S-I4MURT-O7VO79-H7B\",\"RouterUserId\":2056,\"LastConfigurationBackup\":null,\"BackupFailed\":false,\"AutoBackup\":false,\"ID\":2478},{\"ConfigFileURL\":\"\",\"Name\":\"MySmartHome\",\"ConnectionKey\":\"JJV8YC-IP4I85-XOFL2M-VGHJ7J-VY77NA-J71\",\"RouterUserId\":1031,\"LastConfigurationBackup\":null,\"BackupFailed\":false,\"AutoBackup\":false,\"ID\":2479},{\"ConfigFileURL\":\"\",\"Name\":\"美淘\",\"ConnectionKey\":\"SOZGA6-ZCPYSB-IOT83P-P2MLOL-LFY81Z-57F\",\"RouterUserId\":1035,\"LastConfigurationBackup\":null,\"BackupFailed\":false,\"AutoBackup\":false,\"ID\":2482},{\"ConfigFileURL\":\"\",\"Name\":\"美淘鞋店\",\"ConnectionKey\":\"SOZGA6-ZCPYSB-IOT83P-P2MLOL-LFY81Z-57F\",\"RouterUserId\":1033,\"LastConfigurationBackup\":null,\"BackupFailed\":false,\"AutoBackup\":false,\"ID\":2483},{\"ConfigFileURL\":\"\",\"Name\":\"路由器\",\"ConnectionKey\":\"Z22B6I-U1EPG9-X503FD-WASYUP-XOSEGC-JKZ\",\"RouterUserId\":1028,\"LastConfigurationBackup\":null,\"BackupFailed\":false,\"AutoBackup\":false,\"ID\":2484}],\"Total\":49},\"IsError\":false,\"Message\":null,\"RequestUri\":\"/api/RouterConfiguration/QueryPaging\"}";
////                JsonSerializer<Date> ser = new JsonSerializer<Date>() {
////                    @Override
////                    public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
////                        return src == null ? null : new JsonPrimitive(src.getTime());
////                    }
////                };
////
////                JsonDeserializer<Date> deser = new JsonDeserializer<Date>() {
////                    @Override
////                    public Date deserialize(JsonElement json, Type typeOfT,
////                                            JsonDeserializationContext context) throws JsonParseException {
////                        return json == null ? null : new Date(json.getAsLong());
////                    }
////                };
//
//                Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
//                final ApiCallback<PageResult<RouterCloudData>> pageResultApiCallback = gson.fromJson(json,getType( new ApiCallback<PageResult<RouterCloudData>>()));
//                Log.d("tt", pageResultApiCallback.getData().getResult()[0].getConnectionKey());

//                final PageResult pageResult = gson.fromJson(pageResultApiCallback.getData().toString(), new PageResult().getClass());
//                Log.d("tt", pageResult.getResult().toString());
//
//                final ArrayList arrayList = gson.fromJson(pageResult.getResult().toString(), new ArrayList<RouterCloudData>().getClass());
//                Log.d("tt", arrayList.size()+"");
//
//                return;
                if (!userManager.isLogin()) {
                    userManager.logon("229860255@qq.com", "123456", new Action1<Throwable>() {
                        @Override
                        public void call(final Throwable throwable) {
                            new Handler(getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(RouterSettingsActivity.this, (isLogon = throwable == null) ? "登陆成功！" : throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                } else {
                    userManager.logoff(new Action1<Throwable>() {
                        @Override
                        public void call(final Throwable throwable) {
                            new Handler(getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(RouterSettingsActivity.this, (isLogon = throwable != null) ? throwable.getMessage() : "登出成功！", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                }

//                new AsyncTask<Void, Void, Response>() {
//                    @Override
//                    protected Response doInBackground(Void... params) {
//                        final Request build;
//                        if (!isLogon) {
//                            String body = "grant_type=password&username=%s&password=%s&scope=api&client_id=%s&client_secret=%s";
//                            String user = "admin";
//                            String password = "admin";
//                            build = new Request.Builder()
//                                    .url(Constants.TOKEN_SERVER.concat("connect/token"))
//                                    .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), String.format(body, user, password, Constants.apiKey, Constants.apiSecret)))
//                                    .build();
//
//                        } else {
//                            String body = "token=%s&token_type_hint=access_token&scope=api&client_id=%s&client_secret=%s";
//                            build = new Request.Builder()
////                                    .addHeader("Authorization", token.token_type + " " + token.access_token)
//                                    .url(Constants.TOKEN_SERVER.concat("connect/revocation"))
//                                    .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), String.format(body, token.getAccessToken(), Constants.apiKey, Constants.apiSecret)))
//                                    .build();
//                        }
//                        try {
//                            return okHttpClient.newCall(build).execute();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                        return null;
//                    }
//
//                    ///connect/endsession?id_token_hint=
//                    @Override
//                    protected void onPostExecute(Response response) {
//                        String message;
//                        if (response != null) {
//                            try {
//                                String json = response.body().string();
//                                message = response.code() + "->" + json;
//
//                                if (!isLogon) {
//                                    token = new Gson().fromJson(json, AccessToken.class);
//                                    isLogon=true;
//                                } else {
//                                    isLogon=false;
//                                }
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                                message = e.getMessage();
//                            }
//                        } else {
//                            message = "请求出错!";
//                        }
//                        Toast.makeText(RouterSettingsActivity.this, message, Toast.LENGTH_SHORT).show();
//                    }
//                }.execute();
            }
        });
        findViewById(R.id.get_data_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    final Request.Builder builder = userManager.createApiRequestBuilder(Constants.ServerUrl.USER_GET_SELF);
                    userManager.executeApiRequest(new TypeToken<ApiCallback<Object>>(){}.getType(), builder.build(), new Action1<ApiCallback<Object>>() {
                        @Override
                        public void call(final ApiCallback<Object> apiCallback) {
                            new Handler(getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(RouterSettingsActivity.this, apiCallback.getMessage() + apiCallback.getData(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                } catch (AuthenticatorException e) {
                    Toast.makeText(RouterSettingsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
//                if (token == null) {
//                    Toast.makeText(RouterSettingsActivity.this, "未登录", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                new AsyncTask<Void, Void, Response>() {
//                    @Override
//                    protected Response doInBackground(Void... params) {
//                        try {
//                            return okHttpClient.newCall(new Request.Builder()
//                                    .addHeader("Authorization", token.getTokenType() + " " + token.getAccessToken())
//                                    .url(Constants.HTTPS_SERVER + "api/routeruser/GetSelfDetail").build()).execute();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                        return null;
//                    }
//
//                    @Override
//                    protected void onPostExecute(Response response) {
//                        if (response != null) {
//                            try {
//                                Toast.makeText(RouterSettingsActivity.this, response.body().string(), Toast.LENGTH_SHORT).show();
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                }.execute();
            }
        });

    }

    private void addRouter(String sn) {
        routerManager.addRouter(new Router(null, "路由器", sn));
    }

}
