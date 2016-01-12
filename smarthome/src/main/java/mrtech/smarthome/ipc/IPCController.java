package mrtech.smarthome.ipc;

/**
 * IPC控制器，控制IPC播放参数与PTZ。
 * Created by zdqa1 on 2015/11/28.
 */
public interface IPCController {

    /**
     * 获取当前控制器所控制的IPC。
     *
     * @return IPC对象。
     */
    IPCamera getCurrent();

    /**
     * 云台转左。
     */
    void ptzLeft();

    /**
     * 云台转右。
     */
    void ptzRight();

    /**
     * 云台向上。
     */
    void ptzUp();

    /**
     * 云台向下。
     */
    void ptzDown();



}
