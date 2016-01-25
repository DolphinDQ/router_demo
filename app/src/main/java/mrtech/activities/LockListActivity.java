package mrtech.activities;

import android.app.DownloadManager;
import android.content.Context;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import mrtech.smarthome.router.Router;
import mrtech.smarthome.rpc.Messages;
import mrtech.smarthome.rpc.Models;
import mrtech.smarthome.util.RequestUtil;
import rx.functions.Action2;

public class LockListActivity extends BaseActivity {
    public static final String LOCK_LIST_KEY = "LOCK_LIST_KEY";
    private Router mRouter;
    private List<Models.Device> mDeviceList;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_list);
        mContext = this;
        initContext();
        initDevice();
    }

    private void initDevice() {
        if (mDeviceList == null) return;

        ListView list = (ListView) findViewById(R.id.device_list);
        list.setAdapter(new ArrayAdapter<Models.Device>(mContext, R.layout.item_device, mDeviceList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null)
                    convertView = getLayoutInflater().inflate(R.layout.item_device, parent, false);
                final Models.Device device = getItem(position);
                final Models.ZigBeeDevice zigBeeDevice = device.getExtension(Models.ZigBeeDevice.detail);
                ((TextView) convertView.findViewById(R.id.title)).setText(device.getAlias());
                ((TextView) convertView.findViewById(R.id.description)).setText(zigBeeDevice.getDeviceId().toString().replace("DEVICE_ID_", ""));
                convertView.findViewById(R.id.control_btn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Messages.Request request = RequestUtil.unlock(device.getId());
                        mRouter.getRouterSession().getCommunicationManager().postRequestAsync(request, new Action2<Messages.Response, Throwable>() {
                            @Override
                            public void call(Messages.Response response, final Throwable throwable) {
                                new Handler(getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (throwable==null){
                                            Toast.makeText(LockListActivity.this, "解锁成功", Toast.LENGTH_SHORT).show();
                                        }else {
                                            Toast.makeText(LockListActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        });
                    }
                });
                return convertView;
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void initContext() {
        mRouter = getCacheData(Router.class);
        mDeviceList = (List<Models.Device>) getCacheData(LOCK_LIST_KEY);
        if (mDeviceList == null || mDeviceList.size() == 0 || mRouter == null) {
            Toast.makeText(LockListActivity.this,
                    mRouter == null ? R.string.router_not_found : R.string.device_not_found,
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
