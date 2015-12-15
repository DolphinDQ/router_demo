package mrtech.router_demo;

import android.content.Intent;
import android.hardware.Camera;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

import mrtech.smarthome.router.Router;
import mrtech.smarthome.router.RouterManager;
import rx.functions.Action1;

public class RouterSettingsActivity extends AppCompatActivity {

    private ArrayAdapter<Router> routerArrayAdapter;
    private RouterManager routerManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_router_settings);
        routerManager = RouterManager.getInstance();
        routerManager.subscribeRouterStatusChanged(new Action1<Router>() {
            @Override
            public void call(final Router router) {
                new Handler(getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        routerArrayAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
        initView();
//        addRouter("T8QCY8-S3HLCS-YSJK2G-RUR057-W1BR09-76T");
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
                convertView.findViewById(R.id.edit_btn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
                convertView.findViewById(R.id.delete_btn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        routerManager.removeRouter(router);
                        routerArrayAdapter.notifyDataSetChanged();
                    }
                });
                return convertView;
            }
        };
        routerList.setAdapter(routerArrayAdapter);
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

    private void addRouter(String sn) {
        routerManager.addRouter(new Router(null, "路由器", sn));
        routerArrayAdapter.notifyDataSetChanged();
    }
}
