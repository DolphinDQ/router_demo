package mrtech.smarthome.ipc;

import rx.Subscription;
import rx.functions.Action1;

/**
 * Created by sphynx on 2015/12/8.
 */
public class IPCModels {

    /**
     * HSL camera status description
     * Created by zdqa1 on 2015/11/26.
     */
    public interface IPCContext {
        /**
         * IPC连接句柄。
         *
         * @return
         */
        long getHandle();

        /**
         * IPC当前状态。
         *
         * @return 100 success
         */
        IPCStatus getStatus();

        /**
         * IPC是否处于播放状态。
         *
         * @return true为是。
         */
        boolean isPlaying();

        /**
         * IPC是否处于重连状态。
         *
         * @return true为是。
         */
        boolean isReconnecting();

        /**
         * 销毁IPC连接会话。
         */
        void destroy();

        /**
         * 初始化IPC连接会话，即重新连接。
         */
        void init();

        /**
         * 订阅IPC播放状态变化事件。
         *
         * @param callback 播放状态变化回调。
         * @return 订阅句柄，句柄在事件不再使用时候，必须执行反订阅（即unsubscribe）。
         */
        Subscription subscribePlayStatus(Action1<IPCContext> callback);

        /**
         * 订阅IPC重连回调。
         *
         * @param callback IPC重连回调。
         * @return 订阅句柄，句柄在事件不再使用时候，必须执行反订阅（即unsubscribe）。
         */
        Subscription subscribeReconnectionStatus(Action1<IPCContext> callback);
    }

    /**
     * Created by sphynx on 2015/12/8.
     */
    public enum IPCStatus {
        /**
         * 初始化
         */
        UNKNOWN("未知状态"),
        /**
         * 正在连接
         */
        CONNECTING("正在连接"),
        /**
         * 在线
         */
        CONNECTED("设备在线"),
        /**
         * 连接失败
         */
        CONNECT_ERROR("连接失败"),
        /**
         * 用户名密码错误
         */
        ERROR_USER_PWD("验证失败"),
        /**
         * 超过最大可连接用户数
         */
        ERROR_MAX_USER("连接数满"),
        /**
         * 视频丢失
         */
        ERROR_VIDEO_LOST("视频丢失"),
        /**
         * 不可用的设备ID
         */
        ERROR_INVALID_ID("ID不可用"),
        /**
         * 设备不在线。
         */
        DEVICE_OFFLINE("设备离线"),
        /**
         * 连接超时
         */
        CONNECT_TIMEOUT("连接超时"),
        /**
         * 断开连接
         */
        DISCONNECT("断开连接"),
        /**
         * 校验用户账号
         */
        CHECK_ACCOUNT("正在验证");

        private final String description;

        private IPCStatus(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return description;
        }
    }

    /**
     * IPC事件数据。
     */
    public interface IPCEventData {
        /**
         * IPC连接句柄。
         *
         * @return 连接句柄。
         */
        long getCameraId();
    }

    /**
     * IPC状态改变回调数据。
     */
    public interface IPCStateChanged extends IPCEventData {

        /**
         * 获取IPC即将改变的状态。
         * @return IPC状态。
         */
        IPCStatus getStatus();
    }

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

    public interface IPCAudioFrame extends IPCEventData {
        byte[] getPcm();

        int getPcmSize();
    }

    public interface IPCGetParameter extends IPCEventData {
        int getParamType();

        Object getParamData();
    }

    public interface IPCSetParameter extends IPCEventData {
        int getParamType();

        Object getResult();
    }

    public interface IPCAlarm extends IPCEventData {
        int getAlarmType();
    }

    public interface PictureData {
        byte[] getImageBuffer();

        int getWidth();

        int getHeight();
    }

    public interface RenderContext {
        int getWidth();

        int getHeight();

        int getSize();
    }
}


