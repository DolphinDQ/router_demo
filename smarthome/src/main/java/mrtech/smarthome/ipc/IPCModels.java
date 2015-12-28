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
         * camera connection handle(userId)
         *
         * @return
         */
        long getHandle();
        /**
         * camera status
         *
         * @return 100 success
         */
        IPCStatus getStatus();
        /**
         * is camera play
         *
         * @return
         */
        boolean isPlaying();
        /**
         * get reconnecting status
         *
         * @return
         */
        boolean isReconnecting();
        /**
         * destroy ip camera context
         * @return
         */
        void destroy();
        /**
         * init
         */
        void init();
        /**
         * subscribe the action of camera playing changed.
         * @param callback
         * @return
         */
        Subscription subscribePlayStatus(Action1<IPCContext> callback);
        /**
         * subscribe the status of camera reconnection.
         * @param callback
         * @return
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
        private  IPCStatus(String description){
            this.description =description;
        }

        @Override
        public String toString() {
            return description;
        }
    }

    public interface IPCEventData {
        long getCameraId();
    }

    public interface IPCStateChanged extends IPCEventData{
        IPCStatus getStatus();
    }

    public interface IPCVideoFrame extends IPCEventData {
        byte[] getFrameData();
        int getFrameType();
        int getFrameSize();
    }

    public interface IPCAudioFrame extends IPCEventData{
        byte[] getPcm();
        int getPcmSize();
    }

    public interface IPCGetParameter extends IPCEventData{
        int getParamType();
        Object getParamData();
    }

    public interface IPCSetParameter extends IPCEventData{
        int getParamType();
        Object getResult();
    }

    public interface IPCAlarm extends IPCEventData{
        int getAlarmType();
    }

    public interface PictureData  {
        byte[] getImageBuffer();
        int getWidth();
        int getHeight();
    }

    public interface RenderContext{
        int getWidth();
        int getHeight();
        int getSize();
    }
}


