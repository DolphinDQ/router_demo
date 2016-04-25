package mrtech.smarthome.ipc;

/**
 * IPC控制器，控制IPC播放参数与转动
 */
public interface IPCController {

    /**
     * 获取当前控制器所控制的IPC
     * @return IPC对象
     */
    IPCamera getCurrent();

    /**
     * IPC向左转
     */
    void ptzLeft();

    /**
     * IPC向右转
     */
    void ptzRight();

    /**
     * IPC向上转
     */
    void ptzUp();

    /**
     * IPC向下转
     */
    void ptzDown();



}
