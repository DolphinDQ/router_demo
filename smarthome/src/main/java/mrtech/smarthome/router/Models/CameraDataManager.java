package mrtech.smarthome.router.Models;
import mrtech.smarthome.ipc.IPCManager;
import mrtech.smarthome.ipc.IPCamera;
import mrtech.smarthome.rpc.Models;
import rx.Subscription;
import rx.functions.Action1;

/**
 * IPC 配置管理器
 */
public interface CameraDataManager {

    /**
     * 获取IPC管理器
     * @return 对应Router新建的IPCManager对象
     */
    IPCManager getIPCManager();

    /**
     * 重新加载IPC列表。读取路由器/缓存摄像头数据。加载到IPCManager中。使用IPCManager.get
     * @param cache true 为使用缓存中的信息
     * @param exception 异常回调，如果回调值Throwable为null为刷新成功。其他均为刷新失败
     */
    void reloadIPCAsync(boolean cache, final Action1<Throwable> exception);

    /**
     * 保存摄像头，只能保存通过RequestUtil.searchCamera()请求返回的device对象
     * @param device 需要添加的设备。搜索到的设备需要配置相关参数，如：摄像头登录帐号、密码
     * @param callback 结果回调，Throwable为null则调用成功
     */
    void saveCamera(Models.Device device, Action1<Throwable> callback);

    /**
     * 删除摄像头
     * @param camera 指定要删除的IPCamera对象
     * @param callback 结果回调，Throwable为null则调用成功
     */
    void deleteCamera(IPCamera camera, Action1<Throwable> callback);

}
