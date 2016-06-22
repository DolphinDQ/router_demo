package mrtech.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.concurrent.TimeoutException;

import mrtech.smarthome.router.Models.CameraDataManager;
import mrtech.smarthome.router.Models.CommunicationManager;
import mrtech.smarthome.router.Models.RouterSession;
import mrtech.smarthome.router.Router;
import mrtech.smarthome.rpc.Messages;
import mrtech.smarthome.rpc.Models;
import mrtech.smarthome.util.RequestUtil;
import rx.functions.Action1;
import rx.functions.Action2;

public class IPCSearchActivity extends BaseActivity {

    private ArrayAdapter<Models.Device> cameraDeviceArrayAdapter;
    private CameraDataManager mCameraManager;
    private RouterSession mRouterSession;
    private CommunicationManager mCommunicationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ipc_search);
        mRouterSession = getCacheData(Router.class).getRouterSession();
        mCommunicationManager = mRouterSession.getCommunicationManager();
        mCameraManager = mRouterSession.getCameraManager();
        cameraDeviceArrayAdapter = new ArrayAdapter<Models.Device>(this, R.layout.item_camera) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(this.getContext())
                            .inflate(R.layout.item_camera, parent, false);
                }
                final Models.Device device = getItem(position);
                ((TextView) convertView.findViewById(R.id.device_name)).setText(device.getAlias());
                ((TextView) convertView.findViewById(R.id.device_state)).setText(device.getType().toString());
                convertView.findViewById(R.id.add_btn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //配置摄像头参数。
                        Models.CameraDevice cameraDevice= device.getExtension(Models.CameraDevice.detail).toBuilder().setUser("admin").setPassword("").build();
                        Models.Device dev= device.toBuilder().setType(Models.DeviceType.DEVICE_TYPE_CAMERA).setExtension(Models.CameraDevice.detail,cameraDevice).build();
                        mCameraManager.saveCamera(dev, new Action1<Throwable>() {
                            @Override
                            public void call(final Throwable throwable) {
                                if (throwable != null) {
                                    new Handler(getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(IPCSearchActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    mCameraManager.reloadIPCAsync(false, new Action1<Throwable>() {
                                        @Override
                                        public void call(final Throwable throwable) {
                                            new Handler(getMainLooper()).post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (throwable != null)
                                                        Toast.makeText(IPCSearchActivity.this, "刷新失败." + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                    finish();
                                                }
                                            });
                                        }
                                    });
                                }
                            }
                        });
                    }
                });
                return convertView;
            }
        };
        initView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.camera_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == R.id.action_search_camera) {
            Toast.makeText(IPCSearchActivity.this, "开始搜索摄像头...", Toast.LENGTH_SHORT).show();
            item.setEnabled(false);
            mCommunicationManager.postRequestAsync(RequestUtil.searchCamera(), new Action2<Messages.Response, Throwable>() {
                @Override
                public void call(Messages.Response response, Throwable throwable) {
                    if (response != null && response.getErrorCode() == Messages.Response.ErrorCode.SUCCESS) {
                        final List<Models.Device> devices = response.getExtension(Messages.SearchCameraResponse.response)
                                .getDevicesList();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                if (devices != null) {
                                    cameraDeviceArrayAdapter.clear();
                                    cameraDeviceArrayAdapter.addAll(devices);
                                    Toast.makeText(IPCSearchActivity.this, "搜索完毕。", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(IPCSearchActivity.this, "没找到摄像头。", Toast.LENGTH_SHORT).show();
                                }
                                item.setEnabled(true);

                            }
                        });
                    }
                }
            });
            return true;
        }

        return super.

                onOptionsItemSelected(item);

    }

    private void initView() {
        ((ListView) findViewById(R.id.camera_list)).setAdapter(cameraDeviceArrayAdapter);
    }
}
