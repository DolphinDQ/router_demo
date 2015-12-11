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

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import hsl.p2pipcam.nativecaller.DeviceSDK;
import hsl.p2pipcam.util.AudioPlayer;
import hsl.p2pipcam.util.CustomAudioRecorder;
import hsl.p2pipcam.util.CustomBuffer;
import hsl.p2pipcam.util.CustomBufferData;
import hsl.p2pipcam.util.CustomBufferHead;
import mrtech.smarthome.ipc.IPCModels.*;
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
        Log.e(HSLPlayer.class.getName(), msg);
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
    private PublishSubject<IPCamera> subjectPlayingCameraChanged=PublishSubject.create();
    private PublishSubject<PictureData> subjectPicture = PublishSubject.create();

    public HSLPlayer(GLSurfaceView glSurfaceView, IPCManager manager) {
        VideoRenderer videoRenderer = this.new VideoRenderer(glSurfaceView);
        glSurfaceView.setRenderer(videoRenderer);
        mRenderer = videoRenderer;
        mManager = manager;
        mAudioBuffer = new CustomBuffer();
        mAudioPlayer = new AudioPlayer(mAudioBuffer);
        mCustomAudioRecorder = new CustomAudioRecorder(this.new InnerRecordListener());
        mRenderer.setListener(new RenderListener() {
            @Override
            public void initComplete(int size, int width, int height) {

            }

            @Override
            public void takePicture(byte[] imageBuffer, int width, int height) {

            }
        });
        subscribeAudio();
    }

    private void subscribeAudio() {
        subscribeAudioHandler = mManager.createEventController().subscribeIPCAudioFrame(new Action1<IPCAudioFrame>() {
            @Override
            public void call(IPCAudioFrame ipcAudioFrame) {
                if (playingCamera != null && ipcAudioFrame.getCamera().equals(playingCamera) && getAudioSwitch()) {
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
    public void play(String deviceId) {
        play(mManager.getCamera(deviceId));
    }

    @Override
    public void play(IPCamera cam) {
        stop();
        if (cam != null) {
            playingCamera = cam;
            ((HSLCameraClient) cam.getIpcContext()).setIsPlaying(true);
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
        } else {
            trace("Can't play NULL camera ...");
        }
    }

    @Override
    public IPCamera getPlayingCamera() {
        return playingCamera;
    }

    @Override
    public IPCamera[] getPlayList() {
        IPCamera[] cameraList = mManager.getCameraList();
        ArrayList<IPCamera> cameras = new ArrayList<>();
        for (IPCamera cam : cameraList) {
            if (cam.getIpcContext().getStatus() == IPCStatus.CONNECTED) {
                cameras.add(cam);
            }
        }
        return cameras.toArray(new IPCamera[0]);
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
        return  subjectRenderContext.subscribe(callback);
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

    private class VideoRenderer implements GLSurfaceView.Renderer {
        int mHeight = 0;
        ByteBuffer mUByteBuffer = null;
        ByteBuffer mVByteBuffer = null;
        int mWidth = 0;
        ByteBuffer mYByteBuffer = null;
        FloatBuffer positionBuffer = null;
        final float[] positionBufferData;
        int positionSlot = 0;
        int programHandle = 0;
        int texRangeSlot = 0;
        int[] texture = new int[3];
        int[] textureSlot = new int[3];
        int vertexShader = 0;
        int yuvFragmentShader = 0;
        byte[] yuvData = null;
        final float[] textCoodBufferData;
        FloatBuffer textCoodBuffer = null;
        boolean bNeedSleep = true;
        private RenderListener listener;
        private boolean isTakePicture = false;

        public void setTakePicture(boolean isTakePicture) {
            this.isTakePicture = isTakePicture;
        }


        public void setListener(RenderListener listener) {
            this.listener = listener;
        }

        public VideoRenderer(GLSurfaceView paramGLSurfaceView) {
            float[] arrayOfFloat1 = new float[16];

            arrayOfFloat1[0] = 0.0F;
            arrayOfFloat1[1] = 0.0F;
            arrayOfFloat1[2] = 0.0F;
            arrayOfFloat1[3] = 1.0F;

            arrayOfFloat1[4] = 0.0F;
            arrayOfFloat1[5] = 1.0F;
            arrayOfFloat1[6] = 0.0F;
            arrayOfFloat1[7] = 1.0F;

            arrayOfFloat1[8] = 1.0F;
            arrayOfFloat1[9] = 0.0F;
            arrayOfFloat1[10] = 0.0F;
            arrayOfFloat1[11] = 1.0F;

            arrayOfFloat1[12] = 1.0F;
            arrayOfFloat1[13] = 1.0F;
            arrayOfFloat1[14] = 0.0F;
            arrayOfFloat1[15] = 1.0F;

            this.textCoodBufferData = arrayOfFloat1;

            float[] arrayOfFloat = new float[16];

            arrayOfFloat[0] = -1.0F;
            arrayOfFloat[1] = 1.0F;
            arrayOfFloat[2] = 0.0F;
            arrayOfFloat[3] = 1.0F;

            arrayOfFloat[4] = -1.0F;
            arrayOfFloat[5] = -1.0F;
            arrayOfFloat[6] = 0.0F;
            arrayOfFloat[7] = 1.0F;

            arrayOfFloat[8] = 1.0F;
            arrayOfFloat[9] = 1.0F;
            arrayOfFloat[10] = 0.0F;
            arrayOfFloat[11] = 1.0F;

            arrayOfFloat[12] = 1.0F;
            arrayOfFloat[13] = -1.0F;
            arrayOfFloat[14] = 0.0F;
            arrayOfFloat[15] = 1.0F;

            this.positionBufferData = arrayOfFloat;

            paramGLSurfaceView.setEGLContextClientVersion(2);
        }

        public int compileShader(String paramString, int paramInt) {
            int i = GLES20.glCreateShader(paramInt);
            if (i != 0) {
                int[] arrayOfInt = new int[1];
                GLES20.glShaderSource(i, paramString);
                GLES20.glCompileShader(i);
                GLES20.glGetShaderiv(i, 35713, arrayOfInt, 0);
                if (arrayOfInt[0] == 0) {
                    GLES20.glDeleteShader(i);
                    i = 0;
                }
            }
            return i;
        }

        public long createShaders() {
            String fragmentShaderCode = "uniform sampler2D Ytex;\n";
            fragmentShaderCode += "uniform sampler2D Utex;\n";
            fragmentShaderCode += "uniform sampler2D Vtex;\n";
            fragmentShaderCode += "precision mediump float;  \n";
            fragmentShaderCode += "varying vec4 VaryingTexCoord0; \n";
            fragmentShaderCode += "vec4 color;\n";
            fragmentShaderCode += "void main()\n";
            fragmentShaderCode += "{\n";
            fragmentShaderCode += "float yuv0 = (texture2D(Ytex,VaryingTexCoord0.xy)).r;\n";
            fragmentShaderCode += "float yuv1 = (texture2D(Utex,VaryingTexCoord0.xy)).r;\n";
            fragmentShaderCode += "float yuv2 = (texture2D(Vtex,VaryingTexCoord0.xy)).r;\n";
            fragmentShaderCode += "\n";
            fragmentShaderCode += "color.r = yuv0 + 1.4022 * yuv2 - 0.7011;\n";
            fragmentShaderCode += "color.r = (color.r < 0.0) ? 0.0 : ((color.r > 1.0) ? 1.0 : color.r);\n";
            fragmentShaderCode += "color.g = yuv0 - 0.3456 * yuv1 - 0.7145 * yuv2 + 0.53005;\n";
            fragmentShaderCode += "color.g = (color.g < 0.0) ? 0.0 : ((color.g > 1.0) ? 1.0 : color.g);\n";
            fragmentShaderCode += "color.b = yuv0 + 1.771 * yuv1 - 0.8855;\n";
            fragmentShaderCode += "color.b = (color.b < 0.0) ? 0.0 : ((color.b > 1.0) ? 1.0 : color.b);\n";
            fragmentShaderCode += "gl_FragColor = color;\n";
            fragmentShaderCode += "}\n";

            String vertexShaderCode = "uniform mat4 uMVPMatrix;   \n";
            vertexShaderCode += "attribute vec4 vPosition;  \n";
            vertexShaderCode += "attribute vec4 myTexCoord; \n";
            vertexShaderCode += "varying vec4 VaryingTexCoord0; \n";
            vertexShaderCode += "void main(){               \n";
            vertexShaderCode += "VaryingTexCoord0 = myTexCoord; \n";
            vertexShaderCode += "gl_Position = vPosition; \n";
            vertexShaderCode += "}  \n";

            int[] arrayOfInt = new int[1];
            int i = compileShader(vertexShaderCode, 35633);
            this.vertexShader = i;

            int j = compileShader(fragmentShaderCode, 35632);
            this.yuvFragmentShader = j;
            this.programHandle = GLES20.glCreateProgram();
            GLES20.glAttachShader(this.programHandle, this.vertexShader);
            GLES20.glAttachShader(this.programHandle, this.yuvFragmentShader);
            GLES20.glLinkProgram(this.programHandle);
            GLES20.glGetProgramiv(this.programHandle, 35714, arrayOfInt, 0);

            if (arrayOfInt[0] == 0) {
                destroyShaders();
            }

            this.texRangeSlot = GLES20.glGetAttribLocation(this.programHandle, "myTexCoord");

            this.textureSlot[0] = GLES20.glGetUniformLocation(this.programHandle, "Ytex");
            this.textureSlot[1] = GLES20.glGetUniformLocation(this.programHandle, "Utex");
            this.textureSlot[2] = GLES20.glGetUniformLocation(this.programHandle, "Vtex");

            this.positionSlot = GLES20.glGetAttribLocation(this.programHandle, "vPosition");
            return 0;
        }

        public long destroyShaders() {
            if (this.programHandle != 0) {
                GLES20.glDetachShader(this.programHandle, this.yuvFragmentShader);
                GLES20.glDetachShader(this.programHandle, this.vertexShader);
                GLES20.glDeleteProgram(this.programHandle);
                this.programHandle = 0;
            }
            if (this.yuvFragmentShader != 0) {
                GLES20.glDeleteShader(this.yuvFragmentShader);
                this.yuvFragmentShader = 0;
            }
            if (this.vertexShader != 0) {
                GLES20.glDeleteShader(this.vertexShader);
                this.vertexShader = 0;
            }
            return 0L;
        }

        public int draw(ByteBuffer paramByteBuffer1, ByteBuffer paramByteBuffer2, ByteBuffer paramByteBuffer3, int paramInt1, int paramInt2) {
            GLES20.glClear(16384);
            GLES20.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
            GLES20.glUseProgram(this.programHandle);
            paramByteBuffer1.position(0);
            GLES20.glActiveTexture(33984);
            loadTexture(this.texture[0], paramInt1, paramInt2, paramByteBuffer1);
            paramByteBuffer2.position(0);
            GLES20.glActiveTexture(33985);
            loadTexture(this.texture[1], paramInt1 >> 1, paramInt2 >> 1, paramByteBuffer2);
            paramByteBuffer3.position(0);
            GLES20.glActiveTexture(33986);
            loadTexture(this.texture[2], paramInt1 >> 1, paramInt2 >> 1, paramByteBuffer3);
            GLES20.glUniform1i(this.textureSlot[0], 0);
            GLES20.glUniform1i(this.textureSlot[1], 1);
            GLES20.glUniform1i(this.textureSlot[2], 2);

            this.positionBuffer.position(0);
            GLES20.glEnableVertexAttribArray(this.positionSlot);
            GLES20.glVertexAttribPointer(this.positionSlot, 4, GLES20.GL_FLOAT, false, 0, this.positionBuffer);

            this.textCoodBuffer.position(0);

            GLES20.glEnableVertexAttribArray(this.texRangeSlot);
            GLES20.glVertexAttribPointer(this.texRangeSlot, 4, GLES20.GL_FLOAT, false, 0, this.textCoodBuffer);

            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
            GLES20.glDisableVertexAttribArray(this.positionSlot);

            GLES20.glDisableVertexAttribArray(this.texRangeSlot);
            return 0;
        }

        public int loadTexture(int paramInt1, int paramInt2, int paramInt3, Buffer paramBuffer) {
            GLES20.glBindTexture(3553, paramInt1);
            GLES20.glTexParameteri(3553, 10241, 9729);
            GLES20.glTexParameteri(3553, 10240, 9729);
            GLES20.glTexParameteri(3553, 10242, 33071);
            GLES20.glTexParameteri(3553, 10243, 33071);
            GLES20.glTexImage2D(3553, 0, 6409, paramInt2, paramInt3, 0, 6409, GLES20.GL_UNSIGNED_BYTE, paramBuffer);
            return 0;
        }

        public int loadVBOs() {
            this.textCoodBuffer = ByteBuffer.allocateDirect(4 * this.textCoodBufferData.length).order(ByteOrder.nativeOrder()).asFloatBuffer();
            this.textCoodBuffer.put(this.textCoodBufferData).position(0);

            this.positionBuffer = ByteBuffer.allocateDirect(4 * this.positionBufferData.length).order(ByteOrder.nativeOrder()).asFloatBuffer();
            this.positionBuffer.put(this.positionBufferData).position(0);

            return 0;
        }


        public void onDrawFrame(GL10 paramGL10) {
            GLES20.glClear(16384);
            synchronized (this) {
                if ((this.mWidth == 0) || (this.mHeight == 0) || (this.mYByteBuffer == null) || (this.mUByteBuffer == null) || (this.mVByteBuffer == null))
                    return;
                if (bNeedSleep) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                bNeedSleep = true;
                draw(this.mYByteBuffer, this.mUByteBuffer, this.mVByteBuffer, this.mWidth, this.mHeight);

            }
        }


        public void onSurfaceChanged(GL10 paramGL10, int width, int height) {
            GLES20.glViewport(0, 0, width, height);
        }


        public void onSurfaceCreated(GL10 paramGL10, EGLConfig paramEGLConfig) {
            GLES20.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
            GLES20.glGenTextures(3, this.texture, 0);
            createShaders();
            loadVBOs();
        }

        public int unloadVBOs() {
            if (this.positionBuffer != null)
                this.positionBuffer = null;
            return 0;
        }

        public void writeSample(byte[] paramArrayOfByte, int width, int height) {
            synchronized (this) {
                if ((width == 0) || (height == 0)) {
                    return;
                }
                if (listener != null) {
                    listener.initComplete(paramArrayOfByte.length, width, height);
                }

                //拍照
                if (isTakePicture) {
                    isTakePicture = false;
                    if (listener != null)
                        listener.takePicture(paramArrayOfByte, width, height);
                }

                if ((width != this.mWidth) || (height != this.mHeight)) {
                    this.mWidth = width;
                    this.mHeight = height;
                    this.mYByteBuffer = ByteBuffer.allocate(this.mWidth * this.mHeight);
                    this.mUByteBuffer = ByteBuffer.allocate(this.mWidth * this.mHeight / 4);
                    this.mVByteBuffer = ByteBuffer.allocate(this.mWidth * this.mHeight / 4);
                }

                if (this.mYByteBuffer != null) {
                    this.mYByteBuffer.position(0);
                    this.mYByteBuffer.put(paramArrayOfByte, 0, this.mWidth * this.mHeight);
                    this.mYByteBuffer.position(0);
                }

                if (this.mUByteBuffer != null) {
                    this.mUByteBuffer.position(0);
                    this.mUByteBuffer.put(paramArrayOfByte, this.mWidth * this.mHeight, this.mWidth * this.mHeight / 4);
                    this.mUByteBuffer.position(0);
                }

                if (this.mVByteBuffer != null) {
                    this.mVByteBuffer.position(0);
                    this.mVByteBuffer.put(paramArrayOfByte, 5 * (this.mWidth * this.mHeight) / 4, this.mWidth * this.mHeight / 4);
                    this.mVByteBuffer.position(0);
                }

                bNeedSleep = false;
                //return 1;
            }
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
