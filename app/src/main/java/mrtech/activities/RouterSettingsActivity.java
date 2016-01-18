package mrtech.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import mrtech.smarthome.auth.UserManager;
import mrtech.smarthome.router.Router;
import mrtech.smarthome.router.RouterManager;
import rx.Subscription;
import rx.functions.Action1;

public class RouterSettingsActivity extends AppCompatActivity {

    private ArrayAdapter<Router> routerArrayAdapter;
    private RouterManager routerManager;
    private Subscription stateChangedHandle;
    private UserManager userManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_router_settings);
        routerManager = RouterManager.getInstance();
        if (stateChangedHandle == null)
            stateChangedHandle = routerManager.getEventManager().subscribeRouterStatusChangedEvent(new Action1<Router>() {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.router_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add_router) {
            IntentIntegrator integrator = new IntentIntegrator(RouterSettingsActivity.this);
            integrator.setCaptureActivity(RouterScanActivity.class);
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
        stateChangedHandle = null;
        return super.isDestroyed();
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
                convertView.findViewById(R.id.camera_btn).setVisibility(View.GONE);
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
        findViewById(R.id.post_btn).setVisibility(View.GONE);
//        //.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!userManager.isLogin()) {
//                    userManager.logon("229860255@qq.com", "123456", new Action1<Throwable>() {
//                        @Override
//                        public void call(final Throwable throwable) {
//                            new Handler(getMainLooper()).post(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Toast.makeText(RouterSettingsActivity.this, ( throwable == null) ? "登陆成功！" : throwable.getMessage(), Toast.LENGTH_SHORT).show();
//                                }
//                            });
//                        }
//                    });
//                } else {
//                    userManager.logoff(new Action1<Throwable>() {
//                        @Override
//                        public void call(final Throwable throwable) {
//                            new Handler(getMainLooper()).post(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Toast.makeText(RouterSettingsActivity.this, ( throwable != null) ? throwable.getMessage() : "登出成功！", Toast.LENGTH_SHORT).show();
//                                }
//                            });
//                        }
//                    });
//                }
//
//            }
//        });
        findViewById(R.id.get_data_btn).setVisibility(View.GONE);
//                .setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                try {
//                    final Request.Builder builder = userManager.createApiRequestBuilder(Constants.ServerUrl.USER_GET_SELF);
//                    userManager.executeApiRequest(new TypeToken<ApiCallback<Object>>() {
//                    }, builder.build(), new Action1<ApiCallback<Object>>() {
//                        @Override
//                        public void call(final ApiCallback<Object> apiCallback) {
//                            new Handler(getMainLooper()).post(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Toast.makeText(RouterSettingsActivity.this, apiCallback.getMessage() + apiCallback.getData(), Toast.LENGTH_SHORT).show();
//                                }
//                            });
//                        }
//                    });
//                } catch (AuthenticatorException e) {
//                    Toast.makeText(RouterSettingsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
//                }
//            }
//        });

    }

    private void addRouter(String sn) {
        routerManager.addRouter(new Router("路由器", sn));
    }

}
