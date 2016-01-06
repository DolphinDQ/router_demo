package mrtech.smarthome.router.Models;


import mrtech.smarthome.ipc.IPCManager;
import mrtech.smarthome.ipc.IPCamera;
import mrtech.smarthome.rpc.Models;
import rx.functions.Action1;

/**
 * IPC 配置管理器。
 */
public interface CameraManager {

    /**
     * 获取IPC管理器。
     *
     * @return
     */
    IPCManager getIPCManager();

    /**
     * 重新加载IPC列表。
     *
     * @param cache     true 为使用缓存中的信息。
     * @param exception 异常回调，如果回调值Throwable为null为刷新成功。其他均为刷新失败。
     */
    void reloadIPCAsync(boolean cache, final Action1<Throwable> exception);

    /**
     * 保存摄像头
     * @param exception
     */
    void saveCamera(Models.Device device,Action1<Throwable> exception );

    /**
     * 删除摄像头。
     * @param camera
     * @param result
     */
    void deleteCamera(IPCamera camera ,Action1<Throwable> result);

}
