package mrtech.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import mrtech.fragments.CustomerControlFragment;
import mrtech.fragments.TVControlFragment;
import mrtech.smarthome.router.Models.CommunicationManager;
import mrtech.smarthome.router.Router;
import mrtech.smarthome.rpc.Models;

public class InfraredControlPanelActivity extends BaseActivity {
    public static final String INFRARED_DEVICE_KEY = "INFRARED_DEVICE";
    private Router mRouter;
    private Context mContext;
    private Models.Device mDevice;
    private Models.InfraredDevice mInfraredDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_infrared_control_panel);
        mContext = this;
        initContext();
        if (mInfraredDevice== null){
            finish();
            Toast.makeText(InfraredControlPanelActivity.this, "无效的红外设备。", Toast.LENGTH_SHORT).show();
        }
        initFragment();
    }

    private void initFragment() {
        final Fragment defaultPanel = selectDefaultControlPanel(mInfraredDevice.getType());
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (defaultPanel != null)
            transaction.add(R.id.default_control_panel_container, defaultPanel);
        if (mInfraredDevice.getOpcodesCount() > 0)
            transaction.add(R.id.custom_control_panel_container, CustomerControlFragment.newInstance(mRouter, mDevice));
        transaction.commit();
    }

    private void initContext() {
        mRouter = getCacheData(Router.class);
        mDevice = (Models.Device) getCacheData(INFRARED_DEVICE_KEY);
        if (mRouter == null || mDevice == null) {
            if (mRouter == null) {
                Toast.makeText(InfraredControlPanelActivity.this, R.string.router_not_found, Toast.LENGTH_SHORT).show();
            }
            if (mDevice == null) {
                Toast.makeText(InfraredControlPanelActivity.this, R.string.infrared_not_found, Toast.LENGTH_SHORT).show();
            }
            finish();
            return;
        }
        setTitle(mDevice.getAlias());
        mInfraredDevice = mDevice.getExtension(Models.InfraredDevice.detail);

    }

    private Fragment selectDefaultControlPanel(Models.InfraredDeviceType type) {
        switch (type) {
            case INFRARED_DEVICE_TYPE_TELEVISION:
                return TVControlFragment.newInstance(mRouter, mDevice);
            default:
                return null;
        }
    }
}
