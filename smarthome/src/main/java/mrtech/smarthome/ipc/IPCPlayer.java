package mrtech.smarthome.ipc;

import mrtech.smarthome.ipc.Models.PictureData;
import mrtech.smarthome.ipc.Models.RenderContext;
import rx.Subscription;
import rx.functions.Action1;

/**
 * 摄像头播放器
 */
public interface IPCPlayer {
    /**
     * 获取渲染器
     * @return 视频渲染器
     */
    VideoRenderer getRenderer();

    /**
     * 播放默认摄像头，上次播放或者摄像头列表第一个已经连接的摄像头
     */
    void play();

    /**
     * 播放指定IPC
     * @param cam 指定IPC
     */
    void play(IPCamera cam);

    /**
     * 指定设备ID播放
     * @param deviceId 设备ID
     */
    void play(String deviceId);

    /**
     * 获取当前播放IPC
     * @return IPC对象
     */
    IPCamera getPlayingCamera();

    /**
     * 获取当前播放列表，已连接摄像头列表
     * @return 播放列表
     */
    IPCamera[] getPlayList();

    /**
     * 停止当期播放
     */
    void stop();

    /**
     * 获取当前对讲开关
     * @return true为打开
     */
    boolean getTalkSwitch();

    /**
     * 设置对讲开关
     * @param on true为打开
     */
    void setTalkSwitch(boolean on);

    /**
     * 获取监听开关
     * @return true为打开
     */
    boolean getAudioSwitch();

    /**
     * 设置监听开关
     * @param on true为打开
     */
    void setAudioSwitch(boolean on);

    /**
     * 订阅视频渲染事件
     * @param callback 视频渲染事件回调
     * @return 订阅句柄，句柄在事件不再使用时候，必须执行反订阅（即unsubscribe）
     */
    Subscription subscribePlayingCameraChanged(Action1<IPCamera> callback);

    /**
     * 订阅音频渲染事件
     * @param callback 音频渲染事件回调
     * @return 订阅句柄，句柄在事件不再使用时候，必须执行反订阅（即unsubscribe）
     */
    Subscription subscribeRenderAction(Action1<RenderContext> callback);

    /**
     * 当前视频截图
     * @param callback 截图数据回调
     */
    void takePicture(Action1<PictureData> callback);


    /**
     * 画面上下反转
     * @param reversed 设置上下反转
     */
    void upAndDown(boolean reversed);

    /**
     * 画面左右反转
     * @param reversed 设置左右反转
     */
    void leftAndRight(boolean reversed);

}
