package mrtech.smarthome.ipc;

/**
 * the controller of camera ,
 * Created by zdqa1 on 2015/11/28.
 */
public interface IPCController {

    IPCamera getCurrent();

    void ptzLeft();

    void ptzRight();

    void ptzUp();

    void ptzDown();

}
