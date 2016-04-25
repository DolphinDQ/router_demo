package mrtech.smarthome.ipc.Models;

/**
 * IPC视频帧回调数据
 */
public interface IPCVideoFrame extends IPCEventData {
    /**
     * 获取视频帧数据
     * @return 视频帧
     */
    byte[] getFrameData();

    /**
     * 获取帧类型
     * @return 帧类型
     */
    int getFrameType();

    /**
     * 获取帧大小
     * @return 帧大小
     */
    int getFrameSize();
}
