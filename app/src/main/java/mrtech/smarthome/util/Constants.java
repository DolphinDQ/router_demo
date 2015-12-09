package mrtech.smarthome.util;



public class Constants {
	public static final String server = "cloud.hzmr-tech.com";// 服务器 ip：120.24.54.40
	public static final int port = 8300;// 端口
	//NGngzBgf@umkss83g7brx，CgBANPjs@umkss83g7brx
//	public static String UMID = "umkss83g7brx"; //umkss7ig4crx,bCPUxrCc umkss76npc9u,ILyvwbmY umkss83g7brx,AxoPgnqw umkss86gncqu,tWNagyCw,AcxMFNrH,ifcsOUIm,AhLsQcGG umkss76gncqu,QXhVEMhM
//	public static String API_KEY = "NGngzBgf";
//	public static final String user = "admin";// 设备名
//	public static final String password = "";// 设备密码
//	public static final byte NEW_DATA = 0x11;
//	public static final byte DELETE_FAILED = 0x12;
//	public static final byte DELETE_SUCCEED = 0x13;
//	public static final byte ADD_FAILED = 0x14;
//	public static final byte ADD_SUCCEED = 0x15;
	public static final String PRIVATE_CODE = "MIIDqTCCApGgAwIBAgIJAOumRs0WlpxwMA0GCSqGSIb3DQEBCwUAMGsxCzAJBgNVBAYTAkNOMRIwEAYDVQQIDAlHdWFuZ2RvbmcxJzAlBgNVBAoMHk1hb3JvbmcgSW50ZWxsaWdlbnQgVGVjaG5vbG9neTEfMB0GA1UEAwwWTWFvcm9uZyBSb290IEF1dGhvcml0eTAeFw0xNTAxMjIwMDU0MDZaFw0zNTAxMTcwMDU0MDZaMGsxCzAJBgNVBAYTAkNOMRIwEAYDVQQIDAlHdWFuZ2RvbmcxJzAlBgNVBAoMHk1hb3JvbmcgSW50ZWxsaWdlbnQgVGVjaG5vbG9neTEfMB0GA1UEAwwWTWFvcm9uZyBSb290IEF1dGhvcml0eTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAMkEV6iZH9w32LuIGL4gvxf1BqZo4K2SlGrk8ne7yp7koGiDPUr23BH8YDkPuuJkFXx4ATII1OjoHRb6E3FK6DwiqscgrqMnqZwRu1iK0TJUpm637XirXCny/G8YXYy+sddkV2wugzIlSVd3iJxpH5EBb1FyfavZqq4WlGmwNH4tWOx0bEYRG+J/yoEQgW09nVkAY0yDTVs+InBLcuz0Ovu+1O+agueZnHA7Yf1ScS/7mZqOZGq0/Iw5s1rj0AezUtc/4dSf4IGmT5jVmfpKUIhRbLAF8T3IgyOV+FfrI57KS2s+Q8f4++ORx4MNpOmvkm97Rb9FD3IE+roJ5gH9D68CAwEAAaNQME4wHQYDVR0OBBYEFP+UZKsPa/skvkuROzgrfcLubqNcMB8GA1UdIwQYMBaAFP+UZKsPa/skvkuROzgrfcLubqNcMAwGA1UdEwQFMAMBAf8wDQYJKoZIhvcNAQELBQADggEBAExqdn6BUFIvNU+yzU72b9wpv6FsD/ubOt84IqD9G+WasYZW8wBvGtJruzkt5wM3pjv8556jVDa2T0Og1Y0lr3AceACTZM+dcEJ+AHPnzFo+CdLdtSUXwwUA3gj2Jl6aeF+HYQhYS3ic7ApM9or1IBWYULj5gkCxqBvpV2VklAnRNIiM4Zr0LLPZA0NAN+BvsSDz6olNyR8Fpke7asrCfHIzHZkbv4nBzhdVyjJQn7nvGA6SMI1VjXHkjs6bLfBUNmoTt9J4PIvxjrnXJdunnOHhD+ycKiFMFDRrDVPuYmMqlRquCE+7DQ7nQwY8EgvhYXyd+au8nMZF9vO1Ei3kWWM=";
	public static final String OAUTH_PRIVATE_CODE = "MIIDTjCCAjYCCQDCxFNADCuQ1DANBgkqhkiG9w0BAQUFADBpMQswCQYDVQQGEwJDTjESMBAGA1UECAwJR3VhbmdEb25nMRAwDgYDVQQHDAdIdWlaaG91MRAwDgYDVQQKDAdNYW9Sb25nMREwDwYDVQQLDAhET0IgVEVTVDEPMA0GA1UEAwwGRG9uZ3FpMB4XDTE1MDcwMzAyMzczMloXDTI1MDYzMDAyMzczMlowaTELMAkGA1UEBhMCQ04xEjAQBgNVBAgMCUd1YW5nRG9uZzEQMA4GA1UEBwwHSHVpWmhvdTEQMA4GA1UECgwHTWFvUm9uZzERMA8GA1UECwwIRE9CIFRFU1QxDzANBgNVBAMMBkRvbmdxaTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBALGnT4fy54sL5ShFLc2MArePTRk3jWmztUcHMeH7dhroNKVVwuH/+uNMUW1QaynOerxb7jM8k9Tf4LvJNfRMDhvJ0Z3fO3M1YAaBxwrWOrXlaacedYGNWH0HHOJvcN79zGilHt4ySE+2OVmB2XQnudc1zV7WQOWeTwoJsqzNTqSHgPV/9RzcOLA+jQBC+C+Zj+YCmGOwLGekuREFSuMCWc2iVoTzBhsxi79QvKLGUXsf+DeKdJBaz/k1XvvcgA4srjiZmIYOAR88pB1ey2fM2XP9jlgDmJXbgsJiOnBr4FeHzfJur7gHLEPkQd95OIOIBdse7Qn1OyVeaG3cZY7igyUCAwEAATANBgkqhkiG9w0BAQUFAAOCAQEArdmN5F+huPVzdKHQ+xWGtlX0N+/3VNqoFg4RkkddM3IMt4eV5yhkC9lSkufx4DI44jEcs07GP40XPloUxF6ZiIlpNMOdMsNzDLKHbudx/T+sFnQfbxPOxF6qGCfX2ZADLKv4SPrTWZlbUC9thNJ74EYLiSIHTzdFd8S5fyqqAhXBAhgNxwlvSLU6dULaoLepb5qA+rmxekkDq4fEChoOiHKVPCv5KTum/1ab7ycaM0/OOCkOQgmTjTkW5Fx/eoJYl6tQKpplV4bRHK7t8XXqa44Sa9RZZXvm3WDTw0m1u0bMAAio11urk0spGZtM4amYy+CCFrUqlg0vUjdfP5nRzQ==";
//	public static PortServerClient portserverclient=new PortServerClient();
	public static final String apiKey = "route_app";
	public static final String apiSecret = "74C84CDC-24A2-42BE-AF7C-A2D6C3855251";
	public static int handle = 0;
	public static String accessToken = "";
	public static String tokenType = "";
	
	public static final int DEFALUT_GROUP_ID = 9999;
	public static final int ALL_DEVICE_GROUP_ID = 99999;
	
//	public static final String TOKEN_SERVER = "https://157.122.116.47:44333/";
//	public static final String HTTPS_SERVER = "https://157.122.116.47:44334/";
	
	public static final String TOKEN_SERVER = "https://connect.hzmr-tech.com/";
	public static final String HTTPS_SERVER = "https://user.hzmr-tech.com/";
	
//	public static List<RouterConnectBean> connectBeans = new ArrayList<RouterConnectBean>();
//	public static int connectBeanIndex = 1;
	
	public static final int NOTIFY_NONE = 0;
	public static final int NOTIFY_SOUND = 1;
	public static final int NOTIFY_SHAKE = 2;
	public static final int NOTIFY_SOUND_SHAKE = 3;
	
	public static final long NORMAL_READ_INTERVAL = 100;
	public static final long NORMAL_WRITE_INTERVAL = 200;
	public static final long BATTERY_SAVING_INTERVAL = 45 * 1000;
	
	
	public static final class SharedConst
	{
		public static final String SHARE_CONST_MY_HOME_GROUP_PIC = "group_pic";
		
		public static final String SHARE_CONST_MY_HOME_GROUP_DEFAULT_GROUP = "default_group";
		
		public static final String SHARE_CONST_CAMERA = "camera";
		
		public static final String SHARE_CONST_GUEST = "guest";
		
		public static final String SHARE_CONST_APP = "app";
		
		public static final String SHARE_CONST_USER = "user";
		
		public static final String SHARE_CONST_CACHE_USER = "cache";
	}
	
	public final class FileConst
	{
		public static final String FILE_IMAGE_PATH = "/smarthome/image/";
		
		public static final String FILE_CRASH_PATH = "/smarthome/crash/";
		
		public static final String FILE_TEST_DATA_PATH = "/smarthome/test/";
		
		public static final String FILE_DOWNLOAD_PATH = "/smarthome/download/";
		
		public static final String GROUP_INFO = "group_info.txt";
		
		public static final String ALL_DEVICE_INFO = "all_device_info.txt";
		
		public static final String ZONE_INFO = "zone_info.txt";
	}
	
	public static final class NormalDeviceId
	{
		/** 智能插座86 */
		public static final String DEVICE_86_SMART_PLUG = "MR-CZ001";
		/** 智能开关86 4位 */
		public static final String DEVICE_86_ON_OFF_4_bit = "MR-KG001";
		/** 智能开关86 3位 */
		public static final String DEVICE_86_ON_OFF_3_bit = "MR-KG000";
		/** 智能排插 */
		public static final String DEVICE_EXTENSION_CORD = "MR-PC001";
		/** 摄像头 */
		public static final String DEVICE_CAMERA = "WIFICAM";
	}
	
	public static final class AlarmDeviceId
	{
		/** 人体感应 */
		public static final String DEVICE_BODY_SENSOR = "MR-RT001";
		/** 声光感应 */
		public static final String DEVICE_SOUND_SENSOR = "MR-SG001";
		/** 门磁感应 */
		public static final String DEVICE_GATE_SENSOR = "MR-MC001";
		/** 烟温感应 */
		public static final String DEVICE_TEMP_SENSOR = "MR-YW001";
		/** 震动感应 */
		public static final String DEVICE_SHAKE_SENSOR = "MR-ZD001";
		/** 紧急按钮 */
		public static final String DEVICE_EMERGENSY = "MR-JJ001";
	}
	
	public static final class ServerUrl
	{
		public static final String SEND_MAIL_VERIFY = "api/User/SendMailVerifyCode?mail=";
		
		public static final String SEND_MOBILE_VERIFY = "api/User/SendSmsVerifyCode?phoneNo=";
		
		public static final String ROUTER_SUSER = "api/RoutersUser/GetSelf";
		
		public static final String USER_GET_SELF = "api/User/GetSelf";
		
		public static final String USER_REGISTER = "api/User/RegisterUser";
		
		public static final String ROUTER_USER_SET_SECRET = "api/RouterUser/SetSecret";
		
		public static final String ROUTER_CONFIG_POST = "api/RouterConfiguration/Post";
		
		public static final String ROUTER_CONFIGURATION_QUERY_PAGING = "api/RouterConfiguration/QueryPaging";
		
		public static final String ROUTER_USER_GETSELF = "api/RouterUser/GetSelf";

		public static final String USER_SET_PASSWORD = "api/User/SetPassword";
		
		public static final String FACE_GET_SELF = "api/Face/GetSelf";
		
		public static final String FACE_SET_SELF = "api/Face/SetSelf";
		
		public static final String USER_LOGOUT = "api/User/Logout";
		
		public static final String ROUTER_CONFIGURATION_DELETE = "api/RouterConfiguration/Delete/";
		
		public static final String ROUTER_CONFIGURATION_PUT = "api/RouterConfiguration/Put/";
		
		public static final String USER_GET_APP_LAST_VERSION = "api/User/GetAppLastVersion";
		
		public static final String USER_RESET_PASSWORD_STEP_1 = "api/User/ResetPassword_Step1";
		
		public static final String USER_RESET_PASSWORD_STEP_2 = "api/User/ResetPassword_Step2";
		
		public static final String USER_RESET_PASSWORD_STEP_3 = "api/User/ResetPassword_Step3?data=";
		
		public static final String ROUTER_USER_MATCH_SECRET = "api/RouterUser/MatchSecret?secret=";
		
		public static final String ROUTER_USER_ENABLE_SECRET = "api/RouterUser/EnableSecret";
		
		public static final String ROUTER_USER_SEND_DISABLE_SECRET_VERIFY_CODE = "api/RouterUser/SendDisableSecretVerifyCode";
		
		public static final String ROUTER_USER_DISABLE_SECRET_BY_VERIFY_CODE = "api/RouterUser/DisableSecretByVerifyCode?code=";
		
		public static final String ROUTER_USER_DISABLE_SECRET_BY_SECRET = "api/RouterUser/DisableSecret?secret=";
		/** api/User/UploadCrashReport?version={version}&type={type}&express={express} */
		public static final String USER_UPLOAD_CRASH_REPORT = "api/User/UploadCrashReport?";
	}
	
	public static final class ReceiverAction
	{
		public static final String INTENT_ACTION_UPDATE_ROUTER_LIST = "intent.action.update.router.list";
		
		public static final String INTENT_ACTION_RECEIVE_TIMELINE = "intent.action_receive_timeline";
	}
}
