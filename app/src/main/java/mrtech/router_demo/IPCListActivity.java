package mrtech.router_demo;

import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import mrtech.smarthome.ipc.IPCController;
import mrtech.smarthome.ipc.IPCManager;
import mrtech.smarthome.ipc.IPCModels;
import mrtech.smarthome.ipc.IPCPlayer;
import mrtech.smarthome.ipc.IPCamera;
import mrtech.smarthome.router.Models.CameraManager;
import mrtech.smarthome.router.Router;
import rx.Subscription;
import rx.functions.Action1;

public class IPCListActivity extends BaseActivity {

    private GLSurfaceView glSurfaceView;
    private IPCManager ipcManager;
    private int index;
    private IPCPlayer cameraPlayer;
    private TextView viewCamera;
    private CameraManager cameraManager;
    private Subscription subscriptionCameraStatusChanged;

    private static void trace(String msg) {
        Log.e(IPCListActivity.class.getName(), msg);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ipclist);
        cameraManager = getDefaultData(Router.class).getRouterSession().getCameraManager();
        ipcManager = cameraManager.getIPCManager();
        if (ipcManager == null) {
            Toast.makeText(IPCListActivity.this, "参数无效", Toast.LENGTH_SHORT).show();
            finish();
        }
         subscriptionCameraStatusChanged = ipcManager.createEventController().subscribeCameraStatus(new Action1<IPCModels.IPCStateChanged>() {
            @Override
            public void call(IPCModels.IPCStateChanged ipcStateChanged) {
                final IPCamera camera = ipcManager.getCamera(ipcStateChanged.getCameraId());
                if (camera != null) {
                    play(0);
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
        ListeningClick(R.id.refresh_btn, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraManager.reloadIPCAsync(true, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        new Handler(getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(IPCListActivity.this, "刷新完毕..", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
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
        startActivity(new Intent(this, SearchIPCActivity.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void deleteCamera() {
        final IPCamera playingCamera = cameraPlayer.getPlayingCamera();
        if (playingCamera != null) {
            cameraManager.deleteCamera(playingCamera, new Action1<Throwable>() {
                @Override
                public void call(Throwable throwable) {
                    final String message;
                    if (throwable != null) {
                        throwable.printStackTrace();
                        message = "删除摄像头失败." + throwable.getMessage();
                    } else {
                        message = "删除摄像头成功.";
                        play(0);
                    }
                    new Handler(getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(IPCListActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
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

    private void play(final int idx) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                IPCamera[] playingList = cameraPlayer.getPlayList();
                int max = playingList.length;
                if (max == 0) {
                    trace("play nothing!");
                    viewCamera.setText("没有摄像头");
                    return;
                }
                trace("play ...." + idx);
                int i = index + idx;
                if (i < 0) i = 0;
                if (i >= max) i = max - 1;
                index = i;
                viewCamera.setText("已连接:"+ max+"/"+ipcManager.getCameraList().length +" 正在播放:" + (i + 1) + "/" + max);
                if (playingList[i].equals(cameraPlayer.getPlayingCamera())) return;
                cameraPlayer.play(playingList[i]);
                ((Switch) findViewById(R.id.audio_switch)).setChecked(false);
            }
        });
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

    @Override
    protected void onDestroy() {
        subscriptionCameraStatusChanged.unsubscribe();
        super.onDestroy();
    }
}
