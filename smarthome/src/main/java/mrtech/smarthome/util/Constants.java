package mrtech.smarthome.util;

/**
 * 系统常数
 */
public final class Constants {
    /**
     * 云端服务器地址
     */
    public static final String server = "cloud.hzmr-tech.com";// 服务器 ip：120.24.54.40
    /**
     * 端口号
     */
    public static final int port = 8300;// 端口
    /**
     * 连接路由器所需的证书
     */
    public static final String PRIVATE_CODE = "MIIDqTCCApGgAwIBAgIJAOumRs0WlpxwMA0GCSqGSIb3DQEBCwUAMGsxCzAJBgNVBAYTAkNOMRIwEAYDVQQIDAlHdWFuZ2RvbmcxJzAlBgNVBAoMHk1hb3JvbmcgSW50ZWxsaWdlbnQgVGVjaG5vbG9neTEfMB0GA1UEAwwWTWFvcm9uZyBSb290IEF1dGhvcml0eTAeFw0xNTAxMjIwMDU0MDZaFw0zNTAxMTcwMDU0MDZaMGsxCzAJBgNVBAYTAkNOMRIwEAYDVQQIDAlHdWFuZ2RvbmcxJzAlBgNVBAoMHk1hb3JvbmcgSW50ZWxsaWdlbnQgVGVjaG5vbG9neTEfMB0GA1UEAwwWTWFvcm9uZyBSb290IEF1dGhvcml0eTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAMkEV6iZH9w32LuIGL4gvxf1BqZo4K2SlGrk8ne7yp7koGiDPUr23BH8YDkPuuJkFXx4ATII1OjoHRb6E3FK6DwiqscgrqMnqZwRu1iK0TJUpm637XirXCny/G8YXYy+sddkV2wugzIlSVd3iJxpH5EBb1FyfavZqq4WlGmwNH4tWOx0bEYRG+J/yoEQgW09nVkAY0yDTVs+InBLcuz0Ovu+1O+agueZnHA7Yf1ScS/7mZqOZGq0/Iw5s1rj0AezUtc/4dSf4IGmT5jVmfpKUIhRbLAF8T3IgyOV+FfrI57KS2s+Q8f4++ORx4MNpOmvkm97Rb9FD3IE+roJ5gH9D68CAwEAAaNQME4wHQYDVR0OBBYEFP+UZKsPa/skvkuROzgrfcLubqNcMB8GA1UdIwQYMBaAFP+UZKsPa/skvkuROzgrfcLubqNcMAwGA1UdEwQFMAMBAf8wDQYJKoZIhvcNAQELBQADggEBAExqdn6BUFIvNU+yzU72b9wpv6FsD/ubOt84IqD9G+WasYZW8wBvGtJruzkt5wM3pjv8556jVDa2T0Og1Y0lr3AceACTZM+dcEJ+AHPnzFo+CdLdtSUXwwUA3gj2Jl6aeF+HYQhYS3ic7ApM9or1IBWYULj5gkCxqBvpV2VklAnRNIiM4Zr0LLPZA0NAN+BvsSDz6olNyR8Fpke7asrCfHIzHZkbv4nBzhdVyjJQn7nvGA6SMI1VjXHkjs6bLfBUNmoTt9J4PIvxjrnXJdunnOHhD+ycKiFMFDRrDVPuYmMqlRquCE+7DQ7nQwY8EgvhYXyd+au8nMZF9vO1Ei3kWWM=";
    /**
     * 连接云端服务器所需的证书
     */
    public static final String OAUTH_PRIVATE_CODE = "MIIDTjCCAjYCCQDCxFNADCuQ1DANBgkqhkiG9w0BAQUFADBpMQswCQYDVQQGEwJDTjESMBAGA1UECAwJR3VhbmdEb25nMRAwDgYDVQQHDAdIdWlaaG91MRAwDgYDVQQKDAdNYW9Sb25nMREwDwYDVQQLDAhET0IgVEVTVDEPMA0GA1UEAwwGRG9uZ3FpMB4XDTE1MDcwMzAyMzczMloXDTI1MDYzMDAyMzczMlowaTELMAkGA1UEBhMCQ04xEjAQBgNVBAgMCUd1YW5nRG9uZzEQMA4GA1UEBwwHSHVpWmhvdTEQMA4GA1UECgwHTWFvUm9uZzERMA8GA1UECwwIRE9CIFRFU1QxDzANBgNVBAMMBkRvbmdxaTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBALGnT4fy54sL5ShFLc2MArePTRk3jWmztUcHMeH7dhroNKVVwuH/+uNMUW1QaynOerxb7jM8k9Tf4LvJNfRMDhvJ0Z3fO3M1YAaBxwrWOrXlaacedYGNWH0HHOJvcN79zGilHt4ySE+2OVmB2XQnudc1zV7WQOWeTwoJsqzNTqSHgPV/9RzcOLA+jQBC+C+Zj+YCmGOwLGekuREFSuMCWc2iVoTzBhsxi79QvKLGUXsf+DeKdJBaz/k1XvvcgA4srjiZmIYOAR88pB1ey2fM2XP9jlgDmJXbgsJiOnBr4FeHzfJur7gHLEPkQd95OIOIBdse7Qn1OyVeaG3cZY7igyUCAwEAATANBgkqhkiG9w0BAQUFAAOCAQEArdmN5F+huPVzdKHQ+xWGtlX0N+/3VNqoFg4RkkddM3IMt4eV5yhkC9lSkufx4DI44jEcs07GP40XPloUxF6ZiIlpNMOdMsNzDLKHbudx/T+sFnQfbxPOxF6qGCfX2ZADLKv4SPrTWZlbUC9thNJ74EYLiSIHTzdFd8S5fyqqAhXBAhgNxwlvSLU6dULaoLepb5qA+rmxekkDq4fEChoOiHKVPCv5KTum/1ab7ycaM0/OOCkOQgmTjTkW5Fx/eoJYl6tQKpplV4bRHK7t8XXqa44Sa9RZZXvm3WDTw0m1u0bMAAio11urk0spGZtM4amYy+CCFrUqlg0vUjdfP5nRzQ==";
    /**
     * API Key
     */
    public static final String apiKey = "route_app";
    /**
     * API Secrets
     */
    public static final String apiSecret = "74C84CDC-24A2-42BE-AF7C-A2D6C3855251";

//    public static int handle = 0;
    /**
     * 访问令牌
     */
    public static String accessToken = "";
    /**
     * 令牌类型
     */
    public static String tokenType = "";
    /**
     * 默认分组ID
     */
    public static final int DEFALUT_GROUP_ID = 9999;
    /**
     * 设备默认分组ID
     */
    public static final int ALL_DEVICE_GROUP_ID = 99999;

//	public static final String TOKEN_SERVER = "https://157.122.116.47:44333/";
//	public static final String HTTPS_SERVER = "https://157.122.116.47:44334/";
    /**
     * 令牌服务地址
     */
    public static final String TOKEN_SERVER = "https://connect.hzmr-tech.com/";
    /**
     * HTTPS服务地址
     */
    public static final String HTTPS_SERVER = "https://user.hzmr-tech.com/";
//
//    public static final int NOTIFY_NONE = 0;
//
//    public static final int NOTIFY_SOUND = 1;
//
//    public static final int NOTIFY_SHAKE = 2;
//
//    public static final int NOTIFY_SOUND_SHAKE = 3;
//
//    public static final long NORMAL_READ_INTERVAL = 100;
//
//    public static final long NORMAL_WRITE_INTERVAL = 200;
//
//    public static final long BATTERY_SAVING_INTERVAL = 45 * 1000;
//

//    public static final class SharedConst {
//
//        public static final String SHARE_CONST_MY_HOME_GROUP_PIC = "group_pic";
//
//        public static final String SHARE_CONST_MY_HOME_GROUP_DEFAULT_GROUP = "default_group";
//
//        public static final String SHARE_CONST_CAMERA = "camera";
//
//        public static final String SHARE_CONST_GUEST = "guest";
//
//        public static final String SHARE_CONST_APP = "app";
//
//        public static final String SHARE_CONST_USER = "user";
//
//        public static final String SHARE_CONST_CACHE_USER = "cache";
//    }

    /**
     * 文件保存路径
     */
//    public final class FileConst {
//
//        public static final String FILE_IMAGE_PATH = "/smarthome/image/";
//
//        public static final String FILE_CRASH_PATH = "/smarthome/crash/";
//
//        public static final String FILE_TEST_DATA_PATH = "/smarthome/test/";
//
//        public static final String FILE_DOWNLOAD_PATH = "/smarthome/download/";
//
//        public static final String GROUP_INFO = "group_info.txt";
//
//        public static final String ALL_DEVICE_INFO = "all_device_info.txt";
//
//        public static final String ZONE_INFO = "zone_info.txt";
//    }

    /**
     * 智能控制设备编号
     */
    public static final class NormalDeviceId {
        /**
         * 智能插座86
         */
        public static final String DEVICE_86_SMART_PLUG = "MR-CZ001";
        /**
         * 智能开关86 4位
         */
        public static final String DEVICE_86_ON_OFF_4_bit = "MR-KG001";
        /**
         * 智能开关86 3位
         */
        public static final String DEVICE_86_ON_OFF_3_bit = "MR-KG000";
        /**
         * 智能排插
         */
        public static final String DEVICE_EXTENSION_CORD = "MR-PC001";
        /**
         * 摄像头
         */
        public static final String DEVICE_CAMERA = "WIFICAM";
    }

    /**
     * 报警设备编号
     */
    public static final class AlarmDeviceId {
        /**
         * 人体感应
         */
        public static final String DEVICE_BODY_SENSOR = "MR-RT001";
        /**
         * 声光感应
         */
        public static final String DEVICE_SOUND_SENSOR = "MR-SG001";
        /**
         * 门磁感应
         */
        public static final String DEVICE_GATE_SENSOR = "MR-MC001";
        /**
         * 烟温感应
         */
        public static final String DEVICE_TEMP_SENSOR = "MR-YW001";
        /**
         * 震动感应
         */
        public static final String DEVICE_SHAKE_SENSOR = "MR-ZD001";
        /**
         * 紧急按钮
         */
        public static final String DEVICE_EMERGENSY = "MR-JJ001";
    }

    /**
     * 服务接口
     */
    public static final class ServerUrl {
        /**
         * 发送邮件验证
         */
        public static final String SEND_MAIL_VERIFY = "api/User/SendMailVerifyCode?mail=";
        /**
         * 发送手机短信验证
         */
        public static final String SEND_MOBILE_VERIFY = "api/User/SendSmsVerifyCode?phoneNo=";
        /**
         * 获取用户信息
         */
        public static final String USER_GET_SELF = "api/User/GetSelf";
        /**
         * 注册新用户
         */
        public static final String USER_REGISTER = "api/User/RegisterUser";
        /**
         * 修改用户登录密码
         */
        public static final String USER_SET_PASSWORD = "api/User/SetPassword";
        /**
         * 获取用户头像
         */
        public static final String FACE_GET_SELF = "api/Face/GetSelf";
        /**
         * 设置用户头像
         */
        public static final String FACE_SET_SELF = "api/Face/SetSelf";
        /**
         * 退出用户帐号
         */
        public static final String USER_LOGOUT = "api/User/Logout";
        /**
         * 获取APP最新版本号
         */
        public static final String USER_GET_APP_LAST_VERSION = "api/User/GetAppLastVersion";
        /**
         * 重置用户登录密码第一步。发送验证码到注册邮箱
         */
        public static final String USER_RESET_PASSWORD_STEP_1 = "api/User/ResetPassword_Step1";
        /**
         * 重置用户登录密码第二步。验证接收到的验证码
         */
        public static final String USER_RESET_PASSWORD_STEP_2 = "api/User/ResetPassword_Step2";
        /**
         * 重置用户登录密码第三步。设置新密码
         */
        public static final String USER_RESET_PASSWORD_STEP_3 = "api/User/ResetPassword_Step3?data=";
        /**
         * 上传闪退报告
         * api/User/UploadCrashReport?version={version}&type={type}&express={express}
         */
        public static final String USER_UPLOAD_CRASH_REPORT = "api/User/UploadCrashReport?";
        /**
         * 增加路由配置信息
         */
        public static final String ROUTER_CONFIGURATION_POST = "api/RouterConfiguration/Post";
        /**
         * 删除路由器配置信息
         */
        public static final String ROUTER_CONFIGURATION_DELETE = "api/RouterConfiguration/Delete/";
        /**
         * 修改路由器配置信息
         */
        public static final String ROUTER_CONFIGURATION_PUT = "api/RouterConfiguration/Put/";
        /**
         * 分查询路由配置信息（分页查询）
         */
        public static final String ROUTER_CONFIGURATION_QUERY_PAGING = "api/RouterConfiguration/QueryPaging";
        /**
         * 获取当前用户信息
         */
        public static final String ROUTER_USER_GET_SELF = "api/RouterUser/GetSelf";
        /**
         * 获取当前用户详细信息
         */
        public static final String ROUTER_USER_GET_SELF_DETAIL="api/RouterUser/GetSelfDetail";
        /**
         * 设置云端密码
         */
        public static final String ROUTER_USER_SET_SECRET = "api/RouterUser/SetSecret";
        /**
         * 匹配云端密码
         */
        public static final String ROUTER_USER_MATCH_SECRET = "api/RouterUser/MatchSecret?secret=";
        /**
         * 开启云端密码验证
         */
        public static final String ROUTER_USER_ENABLE_SECRET = "api/RouterUser/EnableSecret";
        /**
         * 关闭云端密码验证
         */
        public static final String ROUTER_USER_DISABLE_SECRET_BY_SECRET = "api/RouterUser/DisableSecret?secret=";
        /**
         * 发送重置云端密码验证码到注册邮箱
         */
        public static final String ROUTER_USER_SEND_DISABLE_SECRET_VERIFY_CODE = "api/RouterUser/SendDisableSecretVerifyCode";
        /**
         * 使用验证码重置云端密码
         */
        public static final String ROUTER_USER_DISABLE_SECRET_BY_VERIFY_CODE = "api/RouterUser/DisableSecretByVerifyCode?code=";
    }

//    public static final class ReceiverAction {
//
//        public static final String INTENT_ACTION_UPDATE_ROUTER_LIST = "intent.action.update.router.list";
//
//        public static final String INTENT_ACTION_RECEIVE_TIMELINE = "intent.action_receive_timeline";
//    }
}
