package mrtech.smarthome.ipc.Models;

/**
 * IPC视频帧回调数据。
 */
public interface IPCVideoFrame extends IPCEventData {
    /**
     * 获取视频帧数据。
     * @return 视频帧。
     */
    byte[] getFrameData();

    /**
     * 帧类型。
     * @return 。。
     */
    int getFrameType();

    /**
     * 帧大小。
     * @return 。。。
     */
    int getFrameSize();
}
