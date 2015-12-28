package mrtech.router_demo;

import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import mrtech.smarthome.ipc.IPCController;
import mrtech.smarthome.ipc.IPCManager;
import mrtech.smarthome.ipc.IPCModels;
import mrtech.smarthome.ipc.IPCPlayer;
import mrtech.smarthome.ipc.IPCamera;
import mrtech.smarthome.router.Models;
import mrtech.smarthome.router.Router;
import mrtech.smarthome.router.RouterManager;
import rx.functions.Action1;

public class IPCListActivity extends AppCompatActivity {

    private GLSurfaceView glSurfaceView;
    private IPCManager ipcManager;
    private int index;
    private IPCamera[] cameraList;
    private IPCPlayer cameraPlayer;
    private TextView viewCamera;
    private Router router;
    private Models.CameraManager cameraManager;

    private static void trace(String msg) {
        Log.e(IPCListActivity.class.getName(), msg);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ipclist);
        final Intent intent = getIntent();
        final RouterManager routerManager = RouterManager.getInstance();
        if (intent != null) {
            router = routerManager.getRouter(intent.getAction());
            if (router != null) {
                cameraManager = router.getRouterSession().getCameraManager();
                ipcManager = cameraManager.getIPCManager();
            }
        }
        if (ipcManager == null) {
            Toast.makeText(IPCListActivity.this, "参数无效", Toast.LENGTH_SHORT).show();
            finish();
        }
        cameraList = ipcManager.getCameraList();
        ipcManager.createEventController().subscribeCameraStatus(new Action1<IPCModels.IPCStateChanged>() {
            @Override
            public void call(IPCModels.IPCStateChanged ipcStateChanged) {
                final IPCamera camera = ipcManager.getCamera(ipcStateChanged.getCameraId());
                if (camera!=null){
                    cameraPlayer.play();
                }
            }
        });
        initView();
        play(0);
    }

    private void initView() {
        ListeningClick(R.id.prov_btn, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play(-1);
            }
        });
        ListeningClick(R.id.next_btn, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play(1);
            }
        });
        ListeningClick(R.id.replay_btn, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play(0);
            }
        });
        ListeningClick(R.id.delete_btn, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteCamera();
            }
        });
        ListeningClick(R.id.add_btn, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCamera();
            }
        });
        ListeningClick(R.id.close_btn, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraPlayer.stop();
            }
        });
        viewCamera = (TextView) findViewById(R.id.cam_num_view);
        glSurfaceView = (GLSurfaceView) findViewById(R.id.view_gls);
        final GestureDetector gestureDetector = new GestureDetector(this, new PtzGestureListener());
        glSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return false;
            }
        });
        cameraPlayer = ipcManager.createCameraPlayer(glSurfaceView);
        ListeningClick(R.id.audio_switch, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraPlayer.setAudioSwitch(((Switch) v).isChecked());
            }
        });
        findViewById(R.id.start_talk_btn).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int act = event.getAction();
                if (act == MotionEvent.ACTION_DOWN) {
                    cameraPlayer.setTalkSwitch(true);
                }
                if (act == MotionEvent.ACTION_UP) {
                    cameraPlayer.setTalkSwitch(false);
                }
                return false;
            }
        });
    }

    private void addCamera() {
//        ipcManager.addCamera(new IPCamera(null, "HSL-118486-DLFHB", "admin", ""));
//        cameraList = ipcManager.getCameraList();
//        play(0);
        IntentIntegrator integrator = new IntentIntegrator(IPCListActivity.this);
        integrator.setCaptureActivity(RouterCaptureActivity.class);
        integrator.initiateScan();
    }

    private void addCamera(String deviceId) {
        if (deviceId ==null||deviceId.equals("")) return;
        cameraManager.saveCamera(mrtech.smarthome.rpc.Models.Device
                .newBuilder()
                .setId(1)
                .setAlias(deviceId)
                .setType(mrtech.smarthome.rpc.Models.DeviceType.DEVICE_TYPE_CAMERA)
                .setExtension(mrtech.smarthome.rpc.Models.CameraDevice.detail, mrtech.smarthome.rpc.Models.CameraDevice
                        .newBuilder()
                        .setId(1)
                        .setDeviceid(deviceId)
                        .setIpAddress("127.0.0.1")
                        .setPort(0)
                        .setDeviceName("cam")
                        .setUser("admin")
                        .setPassword("")
                        .build())
                .build(), new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {

                if (throwable != null) {
                    trace("添加摄像头出错！！" + throwable);
                } else {
                    new Handler(getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(IPCListActivity.this, "添加摄像头成功,正在刷新列表...", Toast.LENGTH_SHORT).show();
                        }
                    });
                    cameraManager.reloadIPCAsync(false, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            if (throwable != null) {
                                trace("刷新摄像头列表出错！" + throwable);
                            } else {
                                Toast.makeText(IPCListActivity.this, "摄像头列表更新完毕！", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        final IntentResult code = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (code != null) {
            addCamera(code.getContents());
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void deleteCamera() {
        ipcManager.removeCamera("HSL-118486-DLFHB");
        cameraList = ipcManager.getCameraList();
        play(0);
    }

    private class PtzGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            IPCController controller = ipcManager.createController(cameraPlayer.getPlayingCamera());
            float vx = Math.abs(velocityX);
            float vy = Math.abs(velocityY);
            if (vx > vy) {
                float forward = e2.getRawX() - e1.getRawX();
                if (Math.abs(forward) < 100) return false;
                // x
                if (forward > 0) {
                    controller.ptzLeft();
                    //right
                } else {
                    //left
                    controller.ptzRight();
                }
            } else {
                float forward = e2.getRawY() - e1.getRawY();
                if (Math.abs(forward) < 100) return false;
                // y
                if (forward > 0) {
                    controller.ptzDown();
                    //down
                } else {
                    //up
                    controller.ptzUp();
                }
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }

    private void ListeningClick(int id, View.OnClickListener listener) {
        findViewById(id).setOnClickListener(listener);
    }

    private void play(int idx) {
        IPCamera[] playingList = cameraPlayer.getPlayList();
        int max = playingList.length;
        if (max == 0) {
            trace("play nothing!");
            viewCamera.setText("nothing!");
            return;
        }
        trace("play ...." + idx);
        int i = index + idx;
        if (i < 0) i = 0;
        if (i >= max) i = max - 1;
        index = i;
        viewCamera.setText("playing :" + (i + 1) + "/" + max + "/" + cameraList.length);
        if (playingList[i].equals(cameraPlayer.getPlayingCamera())) return;
        cameraPlayer.play(playingList[i]);
        ((Switch) findViewById(R.id.audio_switch)).setChecked(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        play(0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraPlayer.stop();
    }
}
