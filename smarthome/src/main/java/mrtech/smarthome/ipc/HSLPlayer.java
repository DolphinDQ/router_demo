package mrtech.smarthome.ipc;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import hsl.p2pipcam.nativecaller.DeviceSDK;
import hsl.p2pipcam.nativecaller.NativeCaller;
import hsl.p2pipcam.util.AudioPlayer;
import hsl.p2pipcam.util.CustomAudioRecorder;
import hsl.p2pipcam.util.CustomBuffer;
import hsl.p2pipcam.util.CustomBufferData;
import hsl.p2pipcam.util.CustomBufferHead;
import mrtech.smarthome.BuildConfig;
import mrtech.smarthome.ipc.Models.*;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

/**
 * Created by sphynx on 2015/12/8.
 */
class HSLPlayer implements IPCPlayer {
    private Subscription subscribeAudioHandler;

    private static void trace(String msg) {
        if (BuildConfig.DEBUG)
            Log.d(HSLPlayer.class.getName(), msg);
    }

    private final VideoRenderer mRenderer;
    private final IPCManager mManager;
    private final CustomBuffer mAudioBuffer;
    private final AudioPlayer mAudioPlayer;
    private final CustomAudioRecorder mCustomAudioRecorder;
    private IPCamera playingCamera;
    private boolean audioSwitch;
    private boolean talkSwitch;
    private boolean audioAlreadyOn;
    private PublishSubject<RenderContext> subjectRenderContext = PublishSubject.create();
    private PublishSubject<IPCamera> subjectPlayingCameraChanged = PublishSubject.create();
    private PublishSubject<PictureData> subjectPicture = PublishSubject.create();


    public HSLPlayer(VideoRenderer renderer, IPCManager manager) {

        mRenderer = renderer;
        mManager = manager;
        mAudioBuffer = new CustomBuffer();
        mAudioPlayer = new AudioPlayer(mAudioBuffer);
        mCustomAudioRecorder = new CustomAudioRecorder(this.new InnerRecordListener());
        mRenderer.setListener(new RenderListener() {
            @Override
            public void initComplete(final int size, final int width, final int height) {
                subjectRenderContext.onNext(new RenderContext() {
                    @Override
                    public int getWidth() {
                        return width;
                    }

                    @Override
                    public int getHeight() {
                        return height;
                    }

                    @Override
                    public int getSize() {
                        return size;
                    }
                });
            }

            @Override
            public void takePicture(final byte[] imageBuffer, final int width, final int height) {
                subjectPicture.onNext(new PictureData() {
                    @Override
                    public byte[] getImageBuffer() {
                        return imageBuffer;
                    }

                    @Override
                    public int getWidth() {
                        return width;
                    }

                    @Override
                    public int getHeight() {
                        return height;
                    }
                });
            }
        });
        subscribeAudio();
    }

    private void subscribeAudio() {
        subscribeAudioHandler = mManager
                .createEventManager(null)
                .subscribeIPCAudioFrame(new Action1<IPCAudioFrame>() {
                    @Override
                    public void call(IPCAudioFrame ipcAudioFrame) {
                        final IPCamera camera = mManager.getCamera(ipcAudioFrame.getCameraId());
                        if (playingCamera != null && camera != null && camera.equals(playingCamera) && getAudioSwitch()) {
                            CustomBufferHead head = new CustomBufferHead();
                            CustomBufferData data = new CustomBufferData();
                            head.length = ipcAudioFrame.getPcmSize();
                            head.startcode = 0xff00ff;
                            data.head = head;
                            data.data = ipcAudioFrame.getPcm();
                            if (mAudioPlayer.isAudioPlaying())
                                mAudioBuffer.addData(data);
                        }
                    }
                });
    }

    private void unsubscribeAudio() {
        if (subscribeAudioHandler != null && !subscribeAudioHandler.isUnsubscribed())
            subscribeAudioHandler.unsubscribe();
    }


    @Override
    public void play(IPCamera cam) {
        if (cam == null) return;
        final HSLCameraClient cameraClient = (HSLCameraClient) cam.getIpcContext();
        if (cam.getIpcContext().getStatus() == IPCStatus.DISCONNECT) {
            cameraClient.reconnect(0);
            cameraClient.getObservablePlayStatus().first(new Func1<IPCContext, Boolean>() {
                @Override
                public Boolean call(IPCContext context) {
                    return context.getStatus() == IPCStatus.CONNECTED;
                }
            }).subscribe(new Action1<IPCContext>() {
                @Override
                public void call(IPCContext context) {
                    if (cameraClient.isPlaying()) {
                        play(cameraClient.getIPCamera());
                    }
                }
            });
        } else {
            stop();
            playingCamera = cam;
            cameraClient.setIsPlaying(true);
            subjectPlayingCameraChanged.onNext(playingCamera);
            new AsyncTask<Long, Void, Void>() {
                @Override
                protected Void doInBackground(Long... params) {
                    long userid = params[0];
                    DeviceSDK.setRender(userid, mRenderer);
                    DeviceSDK.startPlayStream(userid, 10, 1);

                    try {
                        JSONObject obj = new JSONObject();
                        obj.put("param", 13);
                        obj.put("value", 1024);
                        DeviceSDK.setDeviceParam(userid, 0x2026, obj.toString());

                        JSONObject obj1 = new JSONObject();
                        obj1.put("param", 6);
                        obj1.put("value", 15);
                        DeviceSDK.setDeviceParam(userid, 0x2026, obj1.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }.execute(cam.getIpcContext().getHandle());
        }
    }

    @Override
    public void play(String deviceId) {
        play(mManager.getCamera(deviceId));
    }

    @Override
    public IPCamera getPlayingCamera() {
        return playingCamera;
    }

    @Override
    public IPCamera[] getPlayList() {
        List<IPCamera> cameraList = mManager.getCameraList();
        ArrayList<IPCamera> cameras = new ArrayList<IPCamera>();
        for (IPCamera cam : cameraList) {
            if (cam.getIpcContext().getStatus() == IPCStatus.CONNECTED) {
                cameras.add(cam);
            }
        }
        return cameras.toArray(new IPCamera[cameras.size()]);
    }

    @Override
    public void stop() {
        if (playingCamera != null) {
            setAudioSwitch(false);
            setTalkSwitch(false);
            DeviceSDK.stopPlayStream(playingCamera.getIpcContext().getHandle());
            ((HSLCameraClient) playingCamera.getIpcContext()).setIsPlaying(false);
            playingCamera = null;
        }
    }

    @Override
    public void setAudioSwitch(boolean on) {
        IPCamera cam = getPlayingCamera();
        if (cam == null) return;
        synchronized (HSLPlayer.this) {
            long user = cam.getIpcContext().getHandle();
            if (audioSwitch == on) return;
            trace("set audio :" + on);
            if (audioSwitch = on) {
                subscribeAudio();
                mAudioBuffer.ClearAll();
                mAudioPlayer.AudioPlayStart();
                DeviceSDK.startPlayAudio(user, 1);
            } else {
                unsubscribeAudio();
                DeviceSDK.stopPlayAudio(user);
                mAudioBuffer.ClearAll();
                mAudioPlayer.AudioPlayStop();
            }
        }
    }

    @Override
    public Subscription subscribePlayingCameraChanged(Action1<IPCamera> callback) {
        return subjectPlayingCameraChanged.subscribe(callback);
    }

    @Override
    public Subscription subscribeRenderAction(Action1<RenderContext> callback) {
        return subjectRenderContext.subscribe(callback);
    }

    @Override
    public void takePicture(final Action1<PictureData> callback) {
        subjectPicture.first().subscribe(new Action1<PictureData>() {
            @Override
            public void call(PictureData pictureData) {
                callback.call(pictureData);
            }
        });
        mRenderer.setTakePicture(true);
    }

    @Override
    protected void finalize() throws Throwable {
        stop();
        super.finalize();
    }

    @Override
    public VideoRenderer getRenderer() {
        return mRenderer;
    }

    @Override
    public void play() {
        IPCamera playing = getPlayingCamera();
        if (playing != null) {
            play(playing);
        } else {
            for (IPCamera hslCamera : getPlayList()) {
                if (hslCamera.getIpcContext().getStatus() == IPCStatus.CONNECTED) {
                    play(hslCamera);
                    return;
                }
            }
        }
    }

    @Override
    public boolean getAudioSwitch() {
        return audioSwitch;
    }

    @Override
    public void setTalkSwitch(boolean on) {
        if (talkSwitch == on) return;
        IPCamera cam = getPlayingCamera();
        if (cam == null) return;
        long userid = cam.getIpcContext().getHandle();
        synchronized (HSLPlayer.this) {
            trace("set talk :" + on);
            if (talkSwitch = on) {
                if (audioAlreadyOn = getAudioSwitch()) {
                    setAudioSwitch(false);
                }
                DeviceSDK.startTalk(userid);
                mCustomAudioRecorder.StartRecord();
            } else {
                DeviceSDK.stopTalk(userid);
                mCustomAudioRecorder.StopRecord();
                setAudioSwitch(audioAlreadyOn);
                audioAlreadyOn = false;
            }
        }
    }

    @Override
    public boolean getTalkSwitch() {
        return talkSwitch;
    }


    @Override
    public void upAndDown(boolean reversed) {
        if (playingCamera == null) return;
        JSONObject obj = new JSONObject();
        try {
            obj.put("param", 5);
            obj.put("value", reversed ? 3 : 1);
            NativeCaller.SetParam(playingCamera.getIpcContext().getHandle(), 0x2026, obj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void leftAndRight(boolean reversed) {
        if (playingCamera == null) return;
        JSONObject obj = new JSONObject();
        try {
            obj.put("param", 5);
            obj.put("value", reversed ? 2 : 0);
            NativeCaller.SetParam(playingCamera.getIpcContext().getHandle(), 0x2026, obj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

//
//    private class InnerAudioListener implements AudioListener {
//        @Override
//        public void callBackAudioData(long userID, byte[] pcm, int size) {
//            IPCamera cam = getPlayingCamera();
//            if (cam == null) return;
//            if (userID == cam.getIpcContext().getHandle()) {
//                CustomBufferHead head = new CustomBufferHead();
//                CustomBufferData data = new CustomBufferData();
//                head.length = size;
//                head.startcode = 0xff00ff;
//                data.head = head;
//                data.data = pcm;
//                if (mAudioPlayer.isAudioPlaying())
//                    mAudioBuffer.addData(data);
//            }
//        }
//
//    }

    private class InnerRecordListener implements CustomAudioRecorder.AudioRecordResult {
        @Override
        public void AudioRecordData(byte[] data, int len) {
            IPCamera cam = getPlayingCamera();
            if (cam == null || len <= 0) return;
            DeviceSDK.SendTalkData(cam.getIpcContext().getHandle(), data, len);
        }
    }


    /**
     * Created by sphynx on 2015/12/1.
     */
    public interface RenderListener {
        void initComplete(int size, int width, int height);

        void takePicture(byte[] imageBuffer, int width, int height);
    }

//    private interface AudioListener {
//        void callBackAudioData(long userID, byte[] pcm, int size);
//    }
}
