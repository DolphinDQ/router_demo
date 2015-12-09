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
        UNKNOWN,
        /**
         * 正在连接
         */
        CONNECTING,
        /**
         * 在线
         */
        CONNECTED,
        /**
         * 连接失败
         */
        CONNECT_ERROR,
        /**
         * 用户名密码错误
         */
        ERROR_USER_PWD,
        /**
         * 超过最大可连接用户数
         */
        ERROR_MAX_USER,
        /**
         * 视频丢失
         */
        ERROR_VIDEO_LOST,
        /**
         * 不可用的设备ID
         */
        ERROR_INVALID_ID,
        /**
         * 设备不在线。
         */
        DEVICE_OFFLINE,
        /**
         * 连接超时
         */
        CONNECT_TIMEOUT,
        /**
         * 断开连接
         */
        DISCONNECT,
        /**
         * 校验用户账号
         */
        CHECK_ACCOUNT,
    }

    public interface IPCEventData {
        IPCamera getCamera();
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


