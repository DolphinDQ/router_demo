package mrtech.router_demo;

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

public class SearchIPCActivity extends BaseActivity {

    private ArrayAdapter<Models.Device> cameraDeviceArrayAdapter;
    private CameraDataManager mCameraManager;
    private RouterSession mRouterSession;
    private CommunicationManager mCommunicationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_ipc);
        mRouterSession = getDefaultData(Router.class).getRouterSession();
        mCommunicationManager = mRouterSession.getCommunicationManager();
        mCameraManager = mRouterSession.getCameraManager();
        cameraDeviceArrayAdapter = new ArrayAdapter<Models.Device>(this, R.layout.layout_camera_list_item) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(this.getContext())
                            .inflate(R.layout.layout_camera_list_item, parent, false);
                }
                final Models.Device device = getItem(position);
                ((TextView) convertView.findViewById(R.id.device_name)).setText(device.getAlias());
                ((TextView) convertView.findViewById(R.id.device_state)).setText(device.getType().toString());
                convertView.findViewById(R.id.add_btn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //配置摄像头参数。
                        device.getExtension(Models.CameraDevice.detail).toBuilder().setUser("admin").build();
                        device.toBuilder().setType(Models.DeviceType.DEVICE_TYPE_CAMERA).build();
                        mCameraManager.saveCamera(device, new Action1<Throwable>() {
                            @Override
                            public void call(final Throwable throwable) {
                                if (throwable != null) {
                                    new Handler(getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(SearchIPCActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
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
                                                        Toast.makeText(SearchIPCActivity.this, "刷新失败." + throwable.getMessage(), Toast.LENGTH_SHORT).show();
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
            new AsyncTask<Void, Void, List<Models.Device>>() {
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    Toast.makeText(SearchIPCActivity.this, "开始搜索摄像头...", Toast.LENGTH_SHORT).show();
                    item.setEnabled(false);
                }

                @Override
                protected List<Models.Device> doInBackground(Void... params) {
                    try {
                        final Messages.Response response = mCommunicationManager.postRequest(RequestUtil.searchCamera());
                        if (response != null && response.getErrorCode() == Messages.Response.ErrorCode.SUCCESS) {
                            return response.getExtension(Messages.SearchCameraResponse.response)
                                    .getDevicesList();
                        }
                    } catch (TimeoutException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(List<Models.Device> devices) {
                    if (devices != null) {
                        cameraDeviceArrayAdapter.clear();
                        cameraDeviceArrayAdapter.addAll(devices);
                        Toast.makeText(SearchIPCActivity.this, "搜索完毕。", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(SearchIPCActivity.this, "没找到摄像头。", Toast.LENGTH_SHORT).show();
                    }
                    item.setEnabled(true);
                }
            }.execute();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        ((ListView) findViewById(R.id.camera_list)).setAdapter(cameraDeviceArrayAdapter);
    }
}
