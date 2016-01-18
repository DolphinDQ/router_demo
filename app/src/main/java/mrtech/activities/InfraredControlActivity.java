package mrtech.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import mrtech.smarthome.router.Router;
import mrtech.smarthome.rpc.Models;

public class InfraredControlActivity extends BaseActivity {
    public static final String IR_LIST_KEY = "IR_LIST";
    private Router mRouter;
    private List<Models.Device> mIRList;
    private ListView mInfraredList;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_infrared_control);
        mContext = this;
        initContext();
        initInfraredList();
    }

    private void initInfraredList() {
        if (mIRList == null) return;
        mInfraredList = (ListView) findViewById(R.id.infrared_list);
        mInfraredList.setAdapter(new ArrayAdapter<Models.Device>(mContext, R.layout.layout_infrared_device_item, mIRList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null)
                    convertView = getLayoutInflater().inflate(R.layout.layout_infrared_device_item, parent, false);
                final Models.Device device = getItem(position);
                String type=device.getExtension(Models.InfraredDevice.detail).getType().toString();
                type=type.replace("INFRARED_DEVICE_TYPE_","");
                        ((TextView) convertView.findViewById(R.id.title)).setText(device.getAlias());
                ((TextView) convertView.findViewById(R.id.description)).setText(type);
                convertView.findViewById(R.id.infrared_control_btn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setCacheData(InfraredControlPanelActivity.INFRARED_DEVICE_KEY, device);
                        startActivity(new Intent(mContext, InfraredControlPanelActivity.class));
                    }
                });
                return convertView;
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void initContext() {
        mRouter = getCacheData(Router.class);
        mIRList = (List<Models.Device>) getCacheData(IR_LIST_KEY);
        if (mIRList == null || mIRList.size() == 0 || mRouter == null) {
            Toast.makeText(InfraredControlActivity.this,
                    mRouter == null ? R.string.router_not_found : R.string.infrared_not_found,
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }
}
