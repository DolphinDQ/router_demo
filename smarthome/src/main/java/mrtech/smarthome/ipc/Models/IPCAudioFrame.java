package mrtech.smarthome.ipc.Models;

/**
 * IPC音频帧
 */
public interface IPCAudioFrame extends IPCEventData {
    /**
     * 获取音频帧数据
     * @return 音频帧数据
     */
    byte[] getPcm();

    /**
     * 获取音频帧大小
     * @return 音频帧大小
     */
    int getPcmSize();
}
