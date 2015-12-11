package mrtech.smarthome.ipc;
import rx.Subscription;
import rx.functions.Action1;

/**
 * camera media player
 * Created by zdqa1 on 2015/11/26.
 */
public interface IPCPlayer {
    /**
     * play default camera。
     */
    void play();

    void play(String deviceId);

    void play(IPCamera cam);

    IPCamera getPlayingCamera();

    IPCamera[] getPlayList();

    void stop();

    boolean getTalkSwitch();

    void setTalkSwitch(boolean on);

    boolean getAudioSwitch();

    void setAudioSwitch(boolean on);

    Subscription subscribePlayingCameraChanged(Action1<IPCamera> callback);

    Subscription subscribeRenderAction(Action1<IPCModels.RenderContext> callback);

    void takePicture(Action1<IPCModels.PictureData> callback);

}
