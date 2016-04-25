package mrtech.smarthome.ipc.Models;

/**
 * 视频渲染
 */
public interface RenderContext {
    /**
     * 获取视频画面的宽度
     * @return 视频画面的宽度
     */
    int getWidth();

    /**
     * 获取视频画面的高度
     * @return 视频画面的高度
     */
    int getHeight();

    /**
     * 获取视频画面的画质大小
     * @return 视频画面的画质大小
     */
    int getSize();
}