package mrtech.router_demo;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

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
//        routerManager.addRouter(new Router(null,"router", "M1KHTR-SE27HN-MTZCK5-PLWEVO-KMGDI0-EMI"));
        routerManager.addRouter(new Router(null, "router", "T8QCY8-S3HLCS-YSJK2G-RUR057-W1BR09-76T"));
        initView();
        routerArrayAdapter.addAll(routerManager.getRouterList());
        routerArrayAdapter.notifyDataSetChanged();
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
    }

    private void initView() {
        final ListView routerList = (ListView) findViewById(R.id.router_list);
        routerArrayAdapter = new ArrayAdapter<Router>(this, R.layout.layout_router_list_item){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(this.getContext())
                            .inflate(R.layout.layout_router_list_item, parent, false);
                }
                final Router router = getItem(position);
                ((TextView)convertView.findViewById(R.id.router_name)).setText(router.getName());
                ((TextView) convertView.findViewById(R.id.router_state)).setText(router.getRouterSession().getRouterStatus().toString());
                return  convertView;
            }

        };
        routerList.setAdapter(routerArrayAdapter);
    }
}
