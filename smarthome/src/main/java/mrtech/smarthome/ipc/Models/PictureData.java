package mrtech.smarthome.ipc.Models;

/**
 * 视频截图
 */
public interface PictureData {
    /**
     * 获取视频截图缓存
     * @return 视频截图缓存
     */
    byte[] getImageBuffer();

    /**
     * 获取视频截图的宽度
     * @return 视频截图的宽度
     */
    int getWidth();

    /**
     * 获取视频截图的高度
     * @return 视频截图的高度
     */
    int getHeight();
}
