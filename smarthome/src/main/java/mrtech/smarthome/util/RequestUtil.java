/**
 *
 */
package mrtech.smarthome.util;

import android.app.DownloadManager;
import android.util.Log;

import java.util.List;

import mrtech.smarthome.rpc.Messages;
import mrtech.smarthome.rpc.Messages.ActivateSceneRequest;
import mrtech.smarthome.rpc.Messages.ArmRequest;
import mrtech.smarthome.rpc.Messages.AuthenticateRequest;
import mrtech.smarthome.rpc.Messages.BypassRequest;
import mrtech.smarthome.rpc.Messages.ConfigureCameraRecordRequest;
import mrtech.smarthome.rpc.Messages.CreateGroupRequest;
import mrtech.smarthome.rpc.Messages.CreatePlanRequest;
import mrtech.smarthome.rpc.Messages.CreateSceneRequest;
import mrtech.smarthome.rpc.Messages.DeleteDevicesRequest;
import mrtech.smarthome.rpc.Messages.DeleteGroupRequest;
import mrtech.smarthome.rpc.Messages.DeleteInternetAclRulesRequest;
import mrtech.smarthome.rpc.Messages.DeletePlanRequest;
import mrtech.smarthome.rpc.Messages.DeleteSceneRequest;
import mrtech.smarthome.rpc.Messages.Event.EventType;
import mrtech.smarthome.rpc.Messages.GetCameraInfoRequest;
import mrtech.smarthome.rpc.Messages.GetCameraWifiApRequest;
import mrtech.smarthome.rpc.Messages.GetDeviceRequest;
import mrtech.smarthome.rpc.Messages.GetGuestWlanConfigRequest;
import mrtech.smarthome.rpc.Messages.GetWanConfigRequest;
import mrtech.smarthome.rpc.Messages.GetWanRateRequest;
import mrtech.smarthome.rpc.Messages.GetWlanAccessPointsRequest;
import mrtech.smarthome.rpc.Messages.GetWlanAccessRulesRequest;
import mrtech.smarthome.rpc.Messages.GetWlanAndGuestConfigRequest;
import mrtech.smarthome.rpc.Messages.GetWlanConfigRequest;
import mrtech.smarthome.rpc.Messages.PPPoEConnectRequest;
import mrtech.smarthome.rpc.Messages.QueryDeviceRequest;
import mrtech.smarthome.rpc.Messages.QueryGroupRequest;
import mrtech.smarthome.rpc.Messages.QueryPlanRequest;
import mrtech.smarthome.rpc.Messages.QuerySceneRequest;
import mrtech.smarthome.rpc.Messages.QuerySystemLogRequest;
import mrtech.smarthome.rpc.Messages.QueryTimelineRequest;
import mrtech.smarthome.rpc.Messages.QueryZoneRequest;
import mrtech.smarthome.rpc.Messages.Request;
import mrtech.smarthome.rpc.Messages.Request.RequestType;
import mrtech.smarthome.rpc.Messages.DeleteSambaAclRulesRequest;
import mrtech.smarthome.rpc.Messages.SaveCameraRequest;
import mrtech.smarthome.rpc.Messages.SetArmGroupRequest;
import mrtech.smarthome.rpc.Messages.SetCameraWlanRequest;
import mrtech.smarthome.rpc.Messages.SetDeviceAliasRequest;
import mrtech.smarthome.rpc.Messages.SetEventsRequest;
import mrtech.smarthome.rpc.Messages.SetGroupRequest;
import mrtech.smarthome.rpc.Messages.SetGuestWlanConfigRequest;
import mrtech.smarthome.rpc.Messages.SetInternetAclRulesRequest;
import mrtech.smarthome.rpc.Messages.SetMobilePhoneConfigRequest;
import mrtech.smarthome.rpc.Messages.SetPlanActionRequest;
import mrtech.smarthome.rpc.Messages.SetPlanEnabledRequest;
import mrtech.smarthome.rpc.Messages.SetQosExclusiveModeMacRequest;
import mrtech.smarthome.rpc.Messages.SetQosModeRequest;
import mrtech.smarthome.rpc.Messages.SetQosVpnConfigRequest;
import mrtech.smarthome.rpc.Messages.SetSambaAclRulesRequest;
import mrtech.smarthome.rpc.Messages.SetSignalStrengthLevelRequest;
import mrtech.smarthome.rpc.Messages.SetSystemConfigurationRequest;
import mrtech.smarthome.rpc.Messages.SetWanConfigRequest;
import mrtech.smarthome.rpc.Messages.SetWlanAccessRulesRequest;
import mrtech.smarthome.rpc.Messages.SetWlanAndGuestConfigRequest;
import mrtech.smarthome.rpc.Messages.SetWlanConfigRequest;
import mrtech.smarthome.rpc.Messages.TestBandwidthRequest;
import mrtech.smarthome.rpc.Messages.ToggleOnOffRequest;
import mrtech.smarthome.rpc.Messages.UpdateGroupRequest;
import mrtech.smarthome.rpc.Messages.UpdatePlanRequest;
import mrtech.smarthome.rpc.Messages.UpdateSceneRequest;
import mrtech.smarthome.rpc.Models;
import mrtech.smarthome.rpc.Models.Action;
import mrtech.smarthome.rpc.Models.ArmGroup;
import mrtech.smarthome.rpc.Models.ArmMode;
import mrtech.smarthome.rpc.Models.CameraRecordConfiguration;
import mrtech.smarthome.rpc.Models.CameraWlan;
import mrtech.smarthome.rpc.Models.Device;
import mrtech.smarthome.rpc.Models.DeviceId;
import mrtech.smarthome.rpc.Models.DeviceQuery;
import mrtech.smarthome.rpc.Models.GroupQuery;
import mrtech.smarthome.rpc.Models.GuestWlanConfig;
import mrtech.smarthome.rpc.Models.MobilePhoneConfig;
import mrtech.smarthome.rpc.Models.Plan;
import mrtech.smarthome.rpc.Models.PlanQuery;
import mrtech.smarthome.rpc.Models.QosMode;
import mrtech.smarthome.rpc.Models.Scene;
import mrtech.smarthome.rpc.Models.SceneQuery;
import mrtech.smarthome.rpc.Models.SignalStrengthLevel;
import mrtech.smarthome.rpc.Models.SystemConfiguration;
import mrtech.smarthome.rpc.Models.SystemLogQuery;
import mrtech.smarthome.rpc.Models.TargetType;
import mrtech.smarthome.rpc.Models.TimelineQuery;
import mrtech.smarthome.rpc.Models.WanConfig;
import mrtech.smarthome.rpc.Models.WanPort;
import mrtech.smarthome.rpc.Models.WlanConfig;
import mrtech.smarthome.rpc.Models.WlanPort;
import mrtech.smarthome.rpc.Models.ZoneQuery;

/**
 * @author CJ
 * @version 1.0
 * @date 2015/4/13 22:48:14
 * 打包操作请求后交给RouterCommunicationMnager处理
 */
public final class RequestUtil {
    static Character id = 0;
    final static Object object = new Object();
    static int createId() {
        synchronized (object) {
            return id++ << 16;
        }
    }

    /**
     * 获取请求类型的值
     * @param callback 请求消息的相应回调
     * @return 请求类型的值
     */
    public static int getRequestTypeValue(Messages.Response callback) {
        int requestId = callback.getRequestId();
        return requestId << 16 >> 16;
    }

    /**
     * 获取验证请求
     * @param apiKey 验证密钥
     * @return 验证请求
     */
    public static Request getAuthRequest(String apiKey) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.AUTHENTICATE);
        requestBuilder.setRequestId(RequestType.AUTHENTICATE_VALUE + createId());

        AuthenticateRequest.Builder auBuilder = AuthenticateRequest.newBuilder();
        auBuilder.setApiKey(apiKey);
        auBuilder.setProtocol(Long.valueOf("2644132401560307670"));
        auBuilder.setVersion(1);
        requestBuilder.setExtension(AuthenticateRequest.request, auBuilder.build());

        return requestBuilder.build();
    }

//	public static Request getAuthRequest()
//	{
//		Request.Builder requestBuilder = Request.newBuilder();
//		requestBuilder.setType(RequestType.AUTHENTICATE);
//		requestBuilder.setRequestId(RequestType.AUTHENTICATE_VALUE+createId());
//		
//		AuthenticateRequest.Builder auBuilder = AuthenticateRequest.newBuilder();
//		auBuilder.setApiKey(Constants.API_KEY);
//		auBuilder.setProtocol(Long.valueOf("2644132401560307670"));
//		auBuilder.setVersion(1);
//		requestBuilder.setExtension(AuthenticateRequest.request, auBuilder.build());
//		
//		return requestBuilder.build();
//	}

    /**
     * 获取路由器保持连接的请求
     * @return 路由器保持连接的请求
     */
    public static Request getKeepAliveRequest() {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.KEEP_ALIVE);
        requestBuilder.setRequestId(RequestType.KEEP_ALIVE_VALUE + createId());
        return requestBuilder.build();
    }

    /**
     * 获取查询路由器系统日志的请求
     * @param query 系统日志查询条件
     * @return 查询系统日志的请求
     */
    public static Request getSystemLogRequest(SystemLogQuery query) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.QUERY_SYSTEM_LOG);
        requestBuilder.setRequestId(RequestType.QUERY_SYSTEM_LOG_VALUE + createId());

        QuerySystemLogRequest.Builder builder = QuerySystemLogRequest.newBuilder();
        builder.setSystemLogQuery(query);
        requestBuilder.setExtension(QuerySystemLogRequest.request, builder.build());

        return requestBuilder.build();
    }

    /**
     * 获取查询时间轴信息的请求
     * @param query 时间轴查询条件
     * @return 查询时间轴信息的请求
     */
    public static Request getTimeline(TimelineQuery query) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.QUERY_TIMELINE);
        requestBuilder.setRequestId(RequestType.QUERY_TIMELINE_VALUE + createId());

        QueryTimelineRequest.Builder timelineBuilder = QueryTimelineRequest.newBuilder();
        timelineBuilder.setQuery(query);

        requestBuilder.setExtension(QueryTimelineRequest.request, timelineBuilder.build());
        return requestBuilder.build();
    }

    /**
     * 获取查询设备列表的请求
     * @param query 设备列表的查询条件
     * @return 查询设备列表的请求
     */
    public static Request getDevices(DeviceQuery query) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.QUERY_DEVICE);
        requestBuilder.setRequestId(RequestType.QUERY_DEVICE_VALUE + createId());

        QueryDeviceRequest.Builder queryDeviceRequestBuilder = QueryDeviceRequest.newBuilder();
        queryDeviceRequestBuilder.setQuery(query);

        requestBuilder.setExtension(QueryDeviceRequest.request, queryDeviceRequestBuilder.build());
        return requestBuilder.build();
    }

    /**
     * 获取查询分组列表的请求
     * @param groupQuery 分组列表的查询条件
     * @return 查询分组列表的请求
     */
    public static Request getGroup(GroupQuery groupQuery) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.QUERY_GROUP);
        requestBuilder.setRequestId(RequestType.QUERY_GROUP_VALUE + createId());

        QueryGroupRequest.Builder queryGroupRequestBuilder = QueryGroupRequest.newBuilder();
        queryGroupRequestBuilder.setQuery(groupQuery);

        requestBuilder.setExtension(QueryGroupRequest.request, queryGroupRequestBuilder.build());

        return requestBuilder.build();
    }



    /**
     * 获取查询情景模式列表的请求
     * @param query 情景模式列表的查询条件
     * @return 查询情景模式列表的请求
     */
    public static Request getScene(SceneQuery query) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.QUERY_SCENE);
        requestBuilder.setRequestId(RequestType.QUERY_SCENE_VALUE + createId());

        QuerySceneRequest.Builder querySceneRequestBuilder = QuerySceneRequest.newBuilder();
        querySceneRequestBuilder.setQuery(query);

        requestBuilder.setExtension(QuerySceneRequest.request, querySceneRequestBuilder.build());

        return requestBuilder.build();
    }

    /**
     * 获取查询摄像头列表的请求
     * @return 查询摄像头列表的请求
     */
    public static Request searchCamera() {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.SEARCH_CAMERA);
        requestBuilder.setRequestId(RequestType.SEARCH_CAMERA_VALUE + createId());
        return requestBuilder.build();
    }

    /**
     * 获取一键匹配设备的请求
     * @return 一键匹配设备的请求
     */
    public static Request getOneKeyMatch() {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.TOGGLE_EZMODE);
        requestBuilder.setRequestId(RequestType.TOGGLE_EZMODE_VALUE + createId());
        return requestBuilder.build();
    }

    /**
     * 获取创建分组的请求
     * @param groupName 要创建的分组名称
     * @return 创建分组的请求
     */
    public static Request createGroup(String groupName) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.CREATE_GROUP);
        requestBuilder.setRequestId(RequestType.CREATE_GROUP_VALUE + createId());

        CreateGroupRequest.Builder createGroupRequestBuilder = CreateGroupRequest.newBuilder();
        createGroupRequestBuilder.setName(groupName);

        requestBuilder.setExtension(CreateGroupRequest.request, createGroupRequestBuilder.build());
        return requestBuilder.build();
    }

    /**
     * 获取修改分组名称的请求
     * @param groupId 要修改的分组ID
     * @param name 修改后的分组名称
     * @return 修改分组名称的请求
     */
    public static Request updateGroup(int groupId, String name) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.UPDATE_GROUP);
        requestBuilder.setRequestId(RequestType.UPDATE_GROUP_VALUE + createId());

        UpdateGroupRequest.Builder updateGroupRequestBuilder = UpdateGroupRequest.newBuilder();
        updateGroupRequestBuilder.setId(groupId);
        updateGroupRequestBuilder.setName(name);

        requestBuilder.setExtension(UpdateGroupRequest.request, updateGroupRequestBuilder.build());
        return requestBuilder.build();
    }

    /**
     * 获取删除分组的请求
     * @param group 要删除的分组ID
     * @return 删除分组的请求
     */
    public static Request deleteGroup(int group) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.DELETE_GROUP);
        requestBuilder.setRequestId(RequestType.DELETE_GROUP_VALUE + createId());

        DeleteGroupRequest.Builder deleteGroupRequestBuilder = DeleteGroupRequest.newBuilder();
        deleteGroupRequestBuilder.addGroupId(group);

        requestBuilder.setExtension(DeleteGroupRequest.request, deleteGroupRequestBuilder.build());

        return requestBuilder.build();
    }

    /**
     * 获取设置设备别名的请求
     * @param deviceId 要设置别名的设备ID
     * @param alias 设备别名
     * @return 设置设备别名的请求
     */
    public static Request setDeviceAlias(int deviceId, String alias) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.SET_DEVICE_ALIAS);
        requestBuilder.setRequestId(RequestType.SET_DEVICE_ALIAS_VALUE + createId());

        SetDeviceAliasRequest setDeviceAliasRequest = SetDeviceAliasRequest.newBuilder().setDeviceId(deviceId).setAlias(alias).build();
        requestBuilder.setExtension(SetDeviceAliasRequest.request, setDeviceAliasRequest);

        return requestBuilder.build();
    }

    /**
     * 获取删除设备的请求
     * @param deviceId 要删除的设备ID
     * @return 删除设备的请求
     */
    public static Request deleteDevice(int deviceId) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.DELETE_DEVICES);
        requestBuilder.setRequestId(RequestType.DELETE_DEVICES_VALUE + createId());

        requestBuilder.setExtension(DeleteDevicesRequest.request, DeleteDevicesRequest.newBuilder().addDeviceIdList(deviceId).build());
        return requestBuilder.build();
    }

    /**
     * 获取删除设备的请求
     * @param deviceIds 要删除的设备的ID列表
     * @return 批量删除设备的请求
     */
    public static Request deleteDevice(List<Integer> deviceIds) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.DELETE_DEVICES);
        requestBuilder.setRequestId(RequestType.DELETE_DEVICES_VALUE + createId());

        requestBuilder.setExtension(DeleteDevicesRequest.request, DeleteDevicesRequest.newBuilder().addAllDeviceIdList(deviceIds).build());
        return requestBuilder.build();
    }

    /**
     * 获取添加摄像头的请求
     * @param device 要添加的摄像头对象
     * @return 添加摄像头的请求
     */
    public static Request saveCamera(Device device) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.SAVE_CAMERA);
        requestBuilder.setRequestId(RequestType.SAVE_CAMERA_VALUE + createId());

        requestBuilder.setExtension(SaveCameraRequest.request, SaveCameraRequest.newBuilder().setDevice(device).build());

        return requestBuilder.build();
    }

    /**
     * 获取设备切换状态的请求
     * @param onOff 开或关的状态
     * @param targetType 执行切换操作的设备类型
     * @param targetId 执行切换操作的设备ID
     * @return 设备切换状态的请求
     */
    public static Request toggleOnOff(boolean onOff, TargetType targetType, int targetId) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.TOGGLE_ON_OFF);
        requestBuilder.setRequestId(RequestType.TOGGLE_ON_OFF_VALUE + createId());

        ToggleOnOffRequest.Builder toggleOnOffRequestBuilder = ToggleOnOffRequest.newBuilder();

        if (targetType != TargetType.TARGET_TYPE_NOT_SPECIFIED) {
            toggleOnOffRequestBuilder.setTargetType(targetType);
        }
        toggleOnOffRequestBuilder.setTargetId(targetId);
        toggleOnOffRequestBuilder.setState(onOff);

        requestBuilder.setExtension(ToggleOnOffRequest.request, toggleOnOffRequestBuilder.build());

        return requestBuilder.build();
    }

    /**
     * 获取设备切换状态的请求
     * @param toggleOnOffRequest 设备状态切换
     * @return 设备切换状态的请求
     */
    public static Request toggleOnOff(ToggleOnOffRequest toggleOnOffRequest) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.TOGGLE_ON_OFF);
        requestBuilder.setRequestId(RequestType.TOGGLE_ON_OFF_VALUE + createId());

        requestBuilder.setExtension(ToggleOnOffRequest.request, toggleOnOffRequest);

        return requestBuilder.build();
    }

    /**
     * 获取设备加入或移动到指定分组的请求
     * @param deviceId 要加入或移动到指定分组的设备ID
     * @param groupId 设备要加入或移动到的目标分组ID
     * @return 设备加入或移动到指定分组的请求
     */
    public static Request setGroup(int deviceId, int groupId) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.SET_GROUP);
        requestBuilder.setRequestId(RequestType.SET_GROUP_VALUE + createId());

        SetGroupRequest.Builder setGroupRequestBuilder = SetGroupRequest.newBuilder();
        setGroupRequestBuilder.addDeviceId(deviceId);
        if (groupId > -1)
            setGroupRequestBuilder.setGroupId(groupId);

        requestBuilder.setExtension(SetGroupRequest.request, setGroupRequestBuilder.build());

        return requestBuilder.build();
    }

    /**
     * 获取多个设备加入或移动到指定分组的请求
     * @param deviceIds 要加入或移动到指定分组的设备ID列表
     * @param groupId 设备要加入或移动到的目标分组ID
     * @return 多个设备加入或移动到指定分组的请求
     */
    public static Request setGroup(List<Integer> deviceIds, int groupId) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.SET_GROUP);
        requestBuilder.setRequestId(RequestType.SET_GROUP_VALUE + createId());

        SetGroupRequest.Builder setGroupRequestBuilder = SetGroupRequest.newBuilder();
        setGroupRequestBuilder.addAllDeviceId(deviceIds);
        if (groupId > -1)
            setGroupRequestBuilder.setGroupId(groupId);

        requestBuilder.setExtension(SetGroupRequest.request, setGroupRequestBuilder.build());

        return requestBuilder.build();
    }

//	public static Request createScene()
//	{
//		Request.Builder requestBuilder = Request.newBuilder();
//		requestBuilder.setType(RequestType.CREATE_SCENE);
//		requestBuilder.setRequestId(RequestType.CREATE_SCENE_VALUE+createId());
//		
//		CreateSceneRequest.newBuilder().setScene(Scene.newBuilder().setName("").addActions(value))
//		
//		return requestBuilder.build();
//	}

    /**
     * 获取删除情景模式的请求
     * @param sceneId 要删除的情景模式ID
     * @return 删除情景模式的请求
     */
    public static Request deleteScene(int sceneId) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.DELETE_SCENE);
        requestBuilder.setRequestId(RequestType.DELETE_SCENE_VALUE + createId());

        requestBuilder.setExtension(DeleteSceneRequest.request, DeleteSceneRequest.newBuilder().addScenes(sceneId).build());

        return requestBuilder.build();
    }

    /**
     * 获取查询防区的请求
     * @param zoneQuery 防区的查询条件
     * @return 查询防区的请求
     */
    public static Request queryZone(ZoneQuery zoneQuery) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.QUERY_ZONE);
        requestBuilder.setRequestId(RequestType.QUERY_ZONE_VALUE + createId());

        requestBuilder.setExtension(QueryZoneRequest.request, QueryZoneRequest.newBuilder().setQuery(zoneQuery).build());

        return requestBuilder.build();
    }

    /**
     * 订阅路由器事件的请求，每次登录都要重新订阅
     * 重新设置订阅事件后会覆盖上一次的设置
     * 取消所有事件订阅可将EventType为Null
     * @param eventTypes 要订阅的事件组
     * @return 路由器事件的请求
     */
    public static Request setEvent(EventType... eventTypes) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.SET_EVENTS);
        requestBuilder.setRequestId(RequestType.SET_EVENTS_VALUE + createId());
        SetEventsRequest.Builder builder = SetEventsRequest.newBuilder();
        if (eventTypes != null) {
            for (EventType eventType : eventTypes) {
                Log.d("RequestUtil", "set event:" + eventType);
                builder.addEvents(eventType);
            }
        }
        return requestBuilder.setExtension(SetEventsRequest.request, builder.build()).build();
    }

    /**
     * 获取触发情景模式的请求
     * @param sceneId 要触发的情景模式ID
     * @return 触发情景模式的请求
     */
    public static Request activateScene(int sceneId) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.ACTIVATE_SCENE);
        requestBuilder.setRequestId(RequestType.ACTIVATE_SCENE_VALUE + createId());

        requestBuilder.setExtension(ActivateSceneRequest.request, ActivateSceneRequest.newBuilder().setSceneId(sceneId).build());
        return requestBuilder.build();
    }

    /**
     * 获取创建情景模式的请求
     * @param sceneName 要创建的情景模式的名称
     * @param actions 激活情景模式时要执行的操作
     * @return 创建情景模式的请求
     */
    public static Request createScene(String sceneName, List<Action> actions) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.CREATE_SCENE);
        requestBuilder.setRequestId(RequestType.CREATE_SCENE_VALUE + createId());
        Scene.Builder sceneBuilder = Scene.newBuilder().setName(sceneName);
        if (actions.size() > 0) {
            for (Action action : actions)
                sceneBuilder.addActions(action);
        }
        requestBuilder.setExtension(CreateSceneRequest.request, CreateSceneRequest.newBuilder().setScene(sceneBuilder.build()).build());

        return requestBuilder.build();
    }

    /**
     * 获取广域网配置信息的请求
     * @param wanPort 要获取配置信息的广域网端口
     * @return 获取广域网配置信息的请求
     */
    public static Request getWanConfig(WanPort wanPort) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.GET_WAN_CONFIG);
        requestBuilder.setRequestId(RequestType.GET_WAN_CONFIG_VALUE + createId());
        requestBuilder.setExtension(GetWanConfigRequest.request, GetWanConfigRequest.newBuilder().setPort(wanPort).build());
        return requestBuilder.build();
    }

    /**
     * 获取设置广域网配置信息的请求
     * @param wanPort 要设置配置信息的广域网端口
     * @param wanConfig 要设置的广域网配置信息
     * @return 设置广域网配置信息的请求
     */
    public static Request setWanConfig(WanPort wanPort, WanConfig wanConfig) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.SET_WAN_CONFIG);
        requestBuilder.setRequestId(RequestType.SET_WAN_CONFIG_VALUE + createId());
        requestBuilder.setExtension(SetWanConfigRequest.request, SetWanConfigRequest.newBuilder().setConfig(wanConfig).setPort(wanPort).build());
        return requestBuilder.build();
    }

    /**
     * 获取无线局域网配置信息的请求
     * @param wlanPort 要获取配置信息的无线局域网端口
     * @return 获取无线局域网配置信息的请求
     */
    public static Request getWlanConfig(WlanPort wlanPort) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.GET_WLAN_CONFIG);
        requestBuilder.setRequestId(RequestType.GET_WLAN_CONFIG_VALUE + createId());
        requestBuilder.setExtension(GetWlanConfigRequest.request, GetWlanConfigRequest.newBuilder().setPort(wlanPort).build());
        return requestBuilder.build();
    }

    /**
     * 获取无线局域网接入点列表的请求
     * @return 获取无线局域网接入点列表的请求
     */
    public static Request getWlanAccessPoint() {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.GET_WLAN_ACCESS_POINTS);
        requestBuilder.setRequestId(RequestType.GET_WLAN_ACCESS_POINTS_VALUE + createId());
        return requestBuilder.build();
    }

    /**
     * 获取设置无线局域网配置信息的请求
     * @param wlanPort 要设置配置信息的无线局域网端口
     * @param wlanConfig 要设置的无线局域网配置信息
     * @return 设置无线局域网配置信息的请求
     */
    public static Request setWlanConfig(WlanPort wlanPort, WlanConfig wlanConfig) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.SET_WLAN_CONFIG);
        requestBuilder.setRequestId(RequestType.SET_WLAN_CONFIG_VALUE + createId());
        requestBuilder.setExtension(SetWlanConfigRequest.request, SetWlanConfigRequest.newBuilder().setPort(wlanPort).setConfig(wlanConfig).build());
        return requestBuilder.build();
    }

    /**
     * 获取无线局域网连接点列表的请求
     * 包括2.4G网络、2.4G访客网络、5G网络、5G访客网络连接点
     * @param wlanPort 要获取连接点列表的无线局域网端口
     * @return 获取无线局域网连接点列表的请求
     */
    public static Request getWlanAccessPoint(WlanPort wlanPort) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.GET_WLAN_ACCESS_POINTS);
        requestBuilder.setRequestId(RequestType.GET_WLAN_ACCESS_POINTS_VALUE + createId());

        requestBuilder.setExtension(GetWlanAccessPointsRequest.request, GetWlanAccessPointsRequest.newBuilder().setPort(wlanPort).build());

        return requestBuilder.build();
    }

    /**
     * 获取启动布防模式的请求
     * @param armMode 布防模式
     * @return 启动布防模式的请求
     */
    public static Request arm(ArmMode armMode) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.ARM);
        requestBuilder.setRequestId(RequestType.ARM_VALUE + createId());

        requestBuilder.setExtension(ArmRequest.request, ArmRequest.newBuilder().setMode(armMode).build());
        return requestBuilder.build();
    }

    /**
     * 获取设置布防模式的请求
     * @param zoneIds 防区ID列表
     * @param armGroup 防护模式
     * @return 设置布防模式的请求
     */
    public static Request setArmGroup(List<Integer> zoneIds, ArmGroup armGroup) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.SET_ARM_GROUP);
        requestBuilder.setRequestId(RequestType.SET_ARM_GROUP_VALUE + createId());

        requestBuilder.setExtension(SetArmGroupRequest.request, SetArmGroupRequest.newBuilder().setArmGroup(armGroup).addAllZoneId(zoneIds).build());

        return requestBuilder.build();
    }




    /**
     * 获取设备列表的请求
     * @param devices 设备ID列表
     * @return 获取设备列表的请求
     */
    public static Request getDevices(List<Integer> devices) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.GET_DEVICE);
        requestBuilder.setRequestId(RequestType.GET_DEVICE_VALUE + createId());

        requestBuilder.setExtension(GetDeviceRequest.request, GetDeviceRequest.newBuilder().addAllId(devices).build());

        return requestBuilder.build();
    }

    /**
     * 获取更新情景模式的请求
     * @param scene 情景模式对象
     * @return 更新情景模式的请求
     */
    public static Request updateScene(Scene scene) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.UPDATE_SCENE);
        requestBuilder.setRequestId(RequestType.UPDATE_SCENE_VALUE + createId());

        requestBuilder.setExtension(UpdateSceneRequest.request, UpdateSceneRequest.newBuilder().setScene(scene).build());

        return requestBuilder.build();
    }

    /**
     * 获取广域网速率的请求
     * @param wanPort 广域网端口
     * @return 获取广域网速率的请求
     */
    public static Request getWanRate(WanPort wanPort) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.GET_WAN_RATE);
        requestBuilder.setRequestId(RequestType.GET_WAN_RATE_VALUE + createId());

        requestBuilder.setExtension(GetWanRateRequest.request, GetWanRateRequest.newBuilder().setPort(wanPort).build());

        return requestBuilder.build();
    }

    /**
     * 获取终端设备的请求
     * @return 获取终端设备的请求
     */
    public static Request getNetWorkDevice() {

        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.GET_NETWORK_DEVICE);
        requestBuilder.setRequestId(RequestType.GET_NETWORK_DEVICE_VALUE + createId());

        return requestBuilder.build();
    }

    /**
     * 获取访问控制列表的规则模式的请求
     * @return 获取访问控制列表的规则模式的请求
     */
    public static Request getAclRuleMode() {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.GET_ACL_RULE_MODE);
        requestBuilder.setRequestId(RequestType.GET_ACL_RULE_MODE_VALUE + createId());
        return requestBuilder.build();
    }


    /**
     * 获取互联网访问控制列表的规则的请求
     * @return 获取互联网访问控制列表的规则的请求
     */
    public static Request getInternetAclRules() {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.GET_INTERNET_ACL_RULES);
        requestBuilder.setRequestId(RequestType.GET_INTERNET_ACL_RULES_VALUE + createId());

        return requestBuilder.build();
    }

    /**
     * 获取设置互联网访问控制列表的规则的请求
     * @param macs 终端设备MAC地址列表
     * @return 设置互联网访问控制列表的规则的请求
     */
    public static Request setInternetAclRules(List<String> macs) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.SET_INTERNET_ACL_RULES);
        requestBuilder.setRequestId(RequestType.SET_INTERNET_ACL_RULES_VALUE + createId());

        requestBuilder.setExtension(SetInternetAclRulesRequest.request, SetInternetAclRulesRequest.newBuilder().addAllRules(macs).build());

        return requestBuilder.build();
    }

    /**
     * 获取删除互联网访问控制列表的规则的请求
     * @param mac 终端设备MAC地址
     * @return 删除互联网访问控制列表的规则的请求
     */
    public static Request deleteInternetAclRules(String mac) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.DELETE_INTERNET_ACL_RULES);
        requestBuilder.setRequestId(RequestType.DELETE_INTERNET_ACL_RULES_VALUE + createId());

        requestBuilder.setExtension(DeleteInternetAclRulesRequest.request, DeleteInternetAclRulesRequest.newBuilder().addRules(mac).build());

        return requestBuilder.build();
    }

    /**
     * 获取删除互联网访问控制列表的规则的请求
     * @param macs 终端设备MAC地址列表
     * @return 删除互联网访问控制列表的规则的请求
     */
    public static Request deleteInternetAclRules(List<String> macs) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.DELETE_INTERNET_ACL_RULES);
        requestBuilder.setRequestId(RequestType.DELETE_INTERNET_ACL_RULES_VALUE + createId());

        requestBuilder.setExtension(DeleteInternetAclRulesRequest.request, DeleteInternetAclRulesRequest.newBuilder().addAllRules(macs).build());

        return requestBuilder.build();
    }

    /**
     * 获取共享访问控制列表的规则的请求
     * @return 获取共享访问控制列表的规则的请求
     */
    public static Request getSambaAclRules() {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.GET_SAMBA_ACL_RULES);
        requestBuilder.setRequestId(RequestType.GET_SAMBA_ACL_RULES_VALUE + createId());

        return requestBuilder.build();
    }

    /**
     * 获取设置共享访问控制列表的规则的请求
     * @param macs 终端设备MAC地址列表
     * @return 设置共享访问控制列表的规则的请求
     */
    public static Request setSambaAclRules(List<String> macs) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.SET_SAMBA_ACL_RULES);
        requestBuilder.setRequestId(RequestType.SET_SAMBA_ACL_RULES_VALUE + createId());

        requestBuilder.setExtension(SetSambaAclRulesRequest.request, SetSambaAclRulesRequest.newBuilder().addAllRules(macs).build());

        return requestBuilder.build();
    }

    /**
     * 获取删除共享访问控制列表的规则的请求
     * @param macs 终端设备MAC地址列表
     * @return 删除共享访问控制列表的规则的请求
     */
    public static Request deleteSambaAclRules(List<String> macs) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.DELETE_SAMBA_ACL_RULES);
        requestBuilder.setRequestId(RequestType.DELETE_SAMBA_ACL_RULES_VALUE + createId());

        requestBuilder.setExtension(DeleteSambaAclRulesRequest.request, DeleteSambaAclRulesRequest.newBuilder().addAllRules(macs).build());

        return requestBuilder.build();
    }

    /**
     * 获取删除共享访问控制列表的规则的请求
     * @param macs 终端设备MAC地址
     * @return 删除共享访问控制列表的规则的请求
     */
    public static Request deleteSambaAclRules(String macs) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.DELETE_SAMBA_ACL_RULES);
        requestBuilder.setRequestId(RequestType.DELETE_SAMBA_ACL_RULES_VALUE + createId());

        requestBuilder.setExtension(DeleteSambaAclRulesRequest.request, DeleteSambaAclRulesRequest.newBuilder().addRules(macs).build());

        return requestBuilder.build();
    }

    /**
     * 获取以太网配置的请求
     * @return 以太网配置的请求
     */
    public static Request getEthernetConfig() {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.GET_ETHERNET_CONFIG);
        requestBuilder.setRequestId(RequestType.GET_ETHERNET_CONFIG_VALUE + createId());

        return requestBuilder.build();
    }

    /**
     * 获取摄像头信息的请求
     * @param deviceId 摄像头ID
     * @return 获取摄像头信息的请求
     */
    public static Request getCameraInfo(int deviceId) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.GET_CAMERA_INFO);
        requestBuilder.setRequestId(RequestType.GET_CAMERA_INFO_VALUE + createId());
        requestBuilder.setExtension(GetCameraInfoRequest.request, GetCameraInfoRequest.newBuilder().setDeviceId(deviceId).build());
        return requestBuilder.build();
    }

    /**
     * 获取设置摄像头录像配置的请求
     * @param deviceId 摄像头ID
     * @param cameraRecordConfiguration 摄像头录像配置
     * @return 设置摄像头录像配置的请求
     */
    public static Request configureCameraRecord(int deviceId, CameraRecordConfiguration cameraRecordConfiguration) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.CONFIGURE_CAMERA_RECORD);
        requestBuilder.setRequestId(RequestType.CONFIGURE_CAMERA_RECORD_VALUE + createId());

        requestBuilder.setExtension(ConfigureCameraRecordRequest.request, ConfigureCameraRecordRequest.newBuilder().setDeviceId(deviceId).setRecord(cameraRecordConfiguration).build());

        return requestBuilder.build();
    }

    /**
     * 获取访客网络配置的请求
     * @param wlanPort 访客网络的端口
     * @return 获取访客网络配置的请求
     */
    public static Request getGuestWlanConfig(WlanPort wlanPort) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.GET_GUEST_WLAN_CONFIG);
        requestBuilder.setRequestId(RequestType.GET_GUEST_WLAN_CONFIG_VALUE + createId());
        requestBuilder.setExtension(GetGuestWlanConfigRequest.request, GetGuestWlanConfigRequest.newBuilder().setPort(wlanPort).build());

        return requestBuilder.build();
    }

    /**
     * 获取设置访客网络配置的请求
     * @param wlanPort 访客网络的端口
     * @param guestWlanConfig 访客网络配置
     * @return 设置访客网络配置的请求
     */
    public static Request setGuestWlanConfig(WlanPort wlanPort, GuestWlanConfig guestWlanConfig) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.SET_GUEST_WLAN_CONFIG);
        requestBuilder.setRequestId(RequestType.SET_GUEST_WLAN_CONFIG_VALUE + createId());

        requestBuilder.setExtension(SetGuestWlanConfigRequest.request, SetGuestWlanConfigRequest.newBuilder().setPort(wlanPort).setConfig(guestWlanConfig).build());

        return requestBuilder.build();
    }

    /**
     * 获取硬盘信息的请求
     * @return 获取硬盘信息的请求
     */
    public static Request getHDDInfo() {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.GET_HDD_INFO);
        requestBuilder.setRequestId(RequestType.GET_HDD_INFO_VALUE + createId());

        return requestBuilder.build();
    }

    /**
     * 获取初始化硬盘的请求
     * @return 初始化硬盘的请求
     */
    public static Request initializeHDD() {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.INITIALIZE_HDD);
        requestBuilder.setRequestId(RequestType.INITIALIZE_HDD_VALUE + createId());

        return requestBuilder.build();
    }

    /**
     * 获取移除硬盘的请求
     * @return 移除硬盘的请求
     */
    public static Request removeHDD() {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.REMOVE_HDD);
        requestBuilder.setRequestId(RequestType.REMOVE_HDD_VALUE + createId());

        return requestBuilder.build();
    }

    /**
     * 获取重启路由器的请求
     * @return 获取重启路由器的请求
     */
    public static Request reboot() {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.REBOOT);
        requestBuilder.setRequestId(RequestType.REBOOT_VALUE + createId());

        return requestBuilder.build();
    }

    /**
     * 获取路由器信息的请求
     * @return 获取路由器信息的请求
     */
    public static Request getProductInfo() {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.GET_PRODUCT_INFO);
        requestBuilder.setRequestId(RequestType.GET_PRODUCT_INFO_VALUE + createId());

        return requestBuilder.build();
    }

    /**
     * 获取系统配置的请求
     * @return 获取系统配置的请求
     */
    public static Request getSysConfig() {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.GET_SYS_CONFIG);
        requestBuilder.setRequestId(RequestType.GET_SYS_CONFIG_VALUE + createId());

        return requestBuilder.build();
    }

    /**
     * 获取设置系统配置的请求
     * @param systemConfiguration 系统配置
     * @return 设置系统配置的请求
     */
    public static Request setSysConfig(SystemConfiguration systemConfiguration) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.SET_SYS_CONFIG);
        requestBuilder.setRequestId(RequestType.SET_SYS_CONFIG_VALUE + createId());

        requestBuilder.setExtension(SetSystemConfigurationRequest.request, SetSystemConfigurationRequest.newBuilder().setConfiguration(systemConfiguration).build());

        return requestBuilder.build();
    }

    /**
     * 获取查询计划任务的请求
     * @param planQuery 计划任务查询条件
     * @return 查询计划任务的请求
     */
    public static Request queryPlan(PlanQuery planQuery) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.QUERY_PLAN);
        requestBuilder.setRequestId(RequestType.QUERY_PLAN_VALUE + createId());

        requestBuilder.setExtension(QueryPlanRequest.request, QueryPlanRequest.newBuilder().setQuery(planQuery).build());

        return requestBuilder.build();
    }

    /**
     * 获取删除计划任务的请求
     * @param planId 计划任务ID
     * @return 删除计划任务的请求
     */
    public static Request deletePlan(int planId) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.DELETE_PLAN);
        requestBuilder.setRequestId(RequestType.DELETE_PLAN_VALUE + createId());

        requestBuilder.setExtension(DeletePlanRequest.request, DeletePlanRequest.newBuilder().addPlans(planId).build());

        return requestBuilder.build();
    }

    /**
     * 获取改变计划任务开关状态的请求
     * @param planId 计划任务ID
     * @param enable 开关状态
     * @return 改变计划任务开关状态的请求
     */
    public static Request setPlanEnable(int planId, boolean enable) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.SET_PLAN_ENABLED);
        requestBuilder.setRequestId(RequestType.SET_PLAN_ENABLED_VALUE + createId());

        requestBuilder.setExtension(SetPlanEnabledRequest.request, SetPlanEnabledRequest.newBuilder().setPlanId(planId).setEnabled(enable).build());

        return requestBuilder.build();
    }

    /**
     * 获取设置计划任务启动时要执行的操作的请求
     * @param planId 计划任务ID
     * @param actions 计划任务启动时要执行的操作
     * @return 设置计划任务启动时要执行的操作的请求
     */
    public static Request setPlanAction(int planId, List<Action> actions) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.SET_PLAN_ACTION);
        requestBuilder.setRequestId(RequestType.SET_PLAN_ACTION_VALUE + createId());

        requestBuilder.setExtension(SetPlanActionRequest.request, SetPlanActionRequest.newBuilder().setPlanId(planId).addAllActions(actions).build());

        return requestBuilder.build();
    }

    /**
     * 获取创建计划任务的请求
     * @param plan 计划任务对象
     * @return 创建计划任务的请求
     */
    public static Request createPlan(Plan plan) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.CREATE_PLAN);
        requestBuilder.setRequestId(RequestType.CREATE_PLAN_VALUE + createId());

        requestBuilder.setExtension(CreatePlanRequest.request, CreatePlanRequest.newBuilder().setPlan(plan).build());

        return requestBuilder.build();
    }

    /**
     * 获取删除计划任务的请求
     * @param planIds 要删除的计划任务ID列表
     * @return 删除计划任务的请求
     */
    public static Request deletePlans(List<Integer> planIds) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.DELETE_PLAN);
        requestBuilder.setRequestId(RequestType.DELETE_PLAN_VALUE + createId());

        requestBuilder.setExtension(DeletePlanRequest.request, DeletePlanRequest.newBuilder().addAllPlans(planIds).build());

        return requestBuilder.build();
    }

    /**
     * 获取修改计划任务的请求
     * @param plan 计划任务对象
     * @return 修改计划任务的请求
     */
    public static Request updatePlan(Plan plan) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.UPDATE_PLAN);
        requestBuilder.setRequestId(RequestType.UPDATE_PLAN_VALUE + createId());

        requestBuilder.setExtension(UpdatePlanRequest.request, UpdatePlanRequest.newBuilder().setPlan(plan).build());

        return requestBuilder.build();
    }

    /**
     * 获取改变PPPOE连接状态的请求
     * @param isConnect PPPOE连接状态
     * @return 改变PPPOE连接状态的请求
     */
    public static Request pppoeConnect(boolean isConnect) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.PPPOE_CONNECT);
        requestBuilder.setRequestId(RequestType.PPPOE_CONNECT_VALUE + createId());

        requestBuilder.setExtension(PPPoEConnectRequest.request, PPPoEConnectRequest.newBuilder().setConnect(isConnect).build());

        return requestBuilder.build();
    }

    /**
     * 获取添加ZigBee设备倒计时的请求
     * @return 获取添加ZigBee设备倒计时的请求
     */
    public static Request getEzmodeCountDown() {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.GET_EZMODE_COUNTDOWN);
        requestBuilder.setRequestId(RequestType.GET_EZMODE_COUNTDOWN_VALUE + createId());

        return requestBuilder.build();
    }

    /**
     * 获取Qos模式的请求
     * @return 获取Qos模式的请求
     */
    public static Request getQosMode() {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.GET_QOS_MODE);
        requestBuilder.setRequestId(RequestType.GET_QOS_MODE_VALUE + createId());

        return requestBuilder.build();
    }

    /**
     * 获取设置Qos模式的请求
     * @param qosMode Qos模式
     * @return 设置Qos模式的请求
     */
    public static Request setQosMode(QosMode qosMode) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.SET_QOS_MODE);
        requestBuilder.setRequestId(RequestType.SET_QOS_MODE_VALUE + createId());

        requestBuilder.setExtension(SetQosModeRequest.request, SetQosModeRequest.newBuilder().setMode(qosMode).build());

        return requestBuilder.build();
    }

    /**
     * 获取被设置为急速模式的设备的MAC地址的请求
     * @return 获取被设置为急速模式的设备的MAC地址的请求
     */
    public static Request getQosExclusiveModeMac() {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.GET_QOS_EXCLUSIVE_MODE_MAC);
        requestBuilder.setRequestId(RequestType.GET_QOS_EXCLUSIVE_MODE_MAC_VALUE + createId());

        return requestBuilder.build();
    }

    /**
     * 获取将指定设备设置为急速模式的请求
     * @param mac 要设置为急速模式的设备的MAC地址
     * @return 将指定设备设置为急速模式的请求
     */
    public static Request setQosExclusiveModeMac(String mac) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.SET_QOS_EXCLUSIVE_MODE_MAC);
        requestBuilder.setRequestId(RequestType.SET_QOS_EXCLUSIVE_MODE_MAC_VALUE + createId());
        requestBuilder.setExtension(SetQosExclusiveModeMacRequest.request, SetQosExclusiveModeMacRequest.newBuilder().setMac(mac).build());

        return requestBuilder.build();
    }

    /**
     * 获取是否开始测试网络带宽的请求
     * @param isStart 是否开始测试的状态
     * @return 是否开始测试网络带宽的请求
     */
    public static Request testBandwidth(boolean isStart) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.TEST_BANDWIDTH);
        requestBuilder.setRequestId(RequestType.TEST_BANDWIDTH_VALUE + createId());
        requestBuilder.setExtension(TestBandwidthRequest.request, TestBandwidthRequest.newBuilder().setStart(isStart).build());

        return requestBuilder.build();
    }

    /**
     * 获取网络带宽测试结果的请求
     * @return 获取网络带宽测试结果的请求
     */
    public static Request bandwidthTestResult() {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.BANDWIDTH_TEST_RESULT);
        requestBuilder.setRequestId(RequestType.BANDWIDTH_TEST_RESULT_VALUE + createId());

        return requestBuilder.build();
    }

    /**
     * 获取无线信号强度等级的请求
     * @return 获取无线信号强度等级的请求
     */
    public static Request getSignalStrengthLevel() {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.GET_SIGNAL_STRENGTH_LEVEL);
        requestBuilder.setRequestId(RequestType.GET_SIGNAL_STRENGTH_LEVEL_VALUE + createId());

        return requestBuilder.build();
    }

    /**
     * 获取设置无线信号强度等级的请求
     * @param signalStrengthLevel 无线信号强度等级
     * @return 设置无线信号强度等级的请求
     */
    public static Request setSignalStrengthLevel(SignalStrengthLevel signalStrengthLevel) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.SET_SIGNAL_STRENGTH_LEVEL);
        requestBuilder.setRequestId(RequestType.SET_SIGNAL_STRENGTH_LEVEL_VALUE + createId());
        requestBuilder.setExtension(SetSignalStrengthLevelRequest.request, SetSignalStrengthLevelRequest.newBuilder().setLevel(signalStrengthLevel).build());

        return requestBuilder.build();
    }

    /**
     * 获取QosVPN设置的请求
     * @return 获取QosVPN设置的请求
     */
    public static Request getQosVpnConfig() {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.GET_QOS_VPN_CONFIG);
        requestBuilder.setRequestId(RequestType.GET_QOS_VPN_CONFIG_VALUE + createId());

        return requestBuilder.build();
    }

    /**
     * 获取设置QosVPN的请求
     * @param qosVpnConfigRequest QosVPN设置
     * @return 设置QosVPN的请求
     */
    public static Request setQosVpnConfig(SetQosVpnConfigRequest qosVpnConfigRequest) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.SET_QOS_VPN_CONFIG);
        requestBuilder.setRequestId(RequestType.SET_QOS_VPN_CONFIG_VALUE + createId());

        requestBuilder.setExtension(SetQosVpnConfigRequest.request, qosVpnConfigRequest);

        return requestBuilder.build();
    }

    /**
     * 获取摄像头搜索到的路由器Wifi列表的请求
     * @param deviceId 摄像头ID
     * @return 获取摄像头搜索到的路由器Wifi列表的请求
     */
    public static Request getCameraWifiAp(int deviceId) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.GET_CAMERA_WIFI_AP);
        requestBuilder.setRequestId(RequestType.GET_CAMERA_WIFI_AP_VALUE + createId());

        requestBuilder.setExtension(GetCameraWifiApRequest.request, GetCameraWifiApRequest.newBuilder().setDeviceId(deviceId).build());

        return requestBuilder.build();
    }

    /**
     * 获取设置无线局域网和访客网络配置的请求
     * @param wlanPort 无线局域网的端口
     * @param wlanConfig 无线局域网配置
     * @param guestWlanConfig 访客网络配置
     * @return 设置无线局域网和访客网络配置的请求
     */
    public static Request setWlanAndGuestConfig(WlanPort wlanPort, WlanConfig wlanConfig, GuestWlanConfig guestWlanConfig) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.SET_WLAN_AND_GUEST_CONFIG);
        requestBuilder.setRequestId(RequestType.SET_WLAN_AND_GUEST_CONFIG_VALUE + createId());

        requestBuilder.setExtension(SetWlanAndGuestConfigRequest.request, SetWlanAndGuestConfigRequest.newBuilder().setPort(wlanPort).setWlanConfig(wlanConfig).setGuestConfig(guestWlanConfig).build());

        return requestBuilder.build();
    }

    /**
     * 获取无线局域网和访客网络配置的请求
     * @param wlanPort 无线局域网的端口
     * @return 获取无线局域网和访客网络配置的请求
     */
    public static Request getWlanAndGuestConfig(WlanPort wlanPort) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.GET_WLAN_AND_GUEST_CONFIG);
        requestBuilder.setRequestId(RequestType.GET_WLAN_AND_GUEST_CONFIG_VALUE + createId());

        requestBuilder.setExtension(GetWlanAndGuestConfigRequest.request, GetWlanAndGuestConfigRequest.newBuilder().setPort(wlanPort).build());

        return requestBuilder.build();
    }

    /**
     * 获取检查更新的请求，检查是否有更新
     * @return 获取检查更新的请求
     */
    public static Request checkUpdate() {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.CHECK_UPDATE);
        requestBuilder.setRequestId(RequestType.CHECK_UPDATE_VALUE + createId());

        return requestBuilder.build();
    }

    /**
     * 获取更新状态的请求，如更新版本号
     * @return 获取更新状态的请求
     */
    public static Request getOtaStatus() {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.GET_OTA_STATUS);
        requestBuilder.setRequestId(RequestType.GET_OTA_STATUS_VALUE + createId());

        return requestBuilder.build();
    }


    /**
     * 获取检查是否可以进行更新的请求
     * @return 检查是否可以进行更新的请求
     */
    public static Request ready2Update() {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.READY_TO_UPDATE);
        requestBuilder.setRequestId(RequestType.READY_TO_UPDATE_VALUE + createId());

        return requestBuilder.build();
    }

    /**
     * 获取安装更新的请求
     * @return 安装更新的请求
     */
    public static Request installUpdate() {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.INSTALL_UPDATE);
        requestBuilder.setRequestId(RequestType.INSTALL_UPDATE_VALUE + createId());

        return requestBuilder.build();
    }

    /**
     * 获取设置摄像头无线局域网连接的请求
     * @param deviceId 摄像头ID
     * @param cameraWlan 摄像头连接的无线局域网
     * @return 设置摄像头无线局域网连接的请求
     */
    public static Request configureCameraWlan(int deviceId, CameraWlan cameraWlan) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.CONFIGURE_CAMERA_WLAN);
        requestBuilder.setRequestId(RequestType.CONFIGURE_CAMERA_WLAN_VALUE + createId());

        requestBuilder.setExtension(SetCameraWlanRequest.request, SetCameraWlanRequest.newBuilder().setDeviceId(deviceId).setWlan(cameraWlan).build());

        return requestBuilder.build();
    }

    /**
     * 获取安全设置的请求，包括进入延迟报警时间、离开延迟报警时间、报警周期等
     * @return 获取安全设置的请求
     */
    public static Request getCieConfig() {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.GET_CIE_CONFIG);
        requestBuilder.setRequestId(RequestType.GET_CIE_CONFIG_VALUE + createId());

        return requestBuilder.build();
    }

    /**
     * 获取移动网络信息的请求
     * @return 获取移动网络信息的请求
     */
    public static Request getMobileInfo() {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.GET_MOBILE_INFO);
        requestBuilder.setRequestId(RequestType.GET_MOBILE_INFO_VALUE + createId());

        return requestBuilder.build();
    }


    /**
     * 获取设置联系电话配置的请求
     * @param mobilePhoneConfig  联系电话配置
     * @return 设置联系电话的请求
     */
    public static Request setMobilePhoneConfig(MobilePhoneConfig mobilePhoneConfig) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.SET_MOBILE_PHONE_CONFIG);
        requestBuilder.setRequestId(RequestType.SET_MOBILE_PHONE_CONFIG_VALUE + createId());
        requestBuilder.setExtension(SetMobilePhoneConfigRequest.request, SetMobilePhoneConfigRequest.newBuilder().setConfig(mobilePhoneConfig).build());

        return requestBuilder.build();
    }

    /**
     * 获取联系电话配置的请求
     * @return 获取联系电话配置的请求
     */
    public static Request getMobilePhoneConfig() {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.GET_MOBILE_PHONE_CONFIG);
        requestBuilder.setRequestId(RequestType.GET_MOBILE_PHONE_CONFIG_VALUE + createId());

        return requestBuilder.build();
    }

    /**
     * 获取电池电量信息的请求
     * @return 获取电池电量信息的请求
     */
    public static Request getBatteryInfo() {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.GET_BATTERY_INFO);
        requestBuilder.setRequestId(RequestType.GET_BATTERY_INFO_VALUE + createId());

        return requestBuilder.build();
    }

    /**
     * 获取设置设备旁路的请求
     * @param ids 要设置旁路的设备ID列表
     * @param isCancel 是否旁路的状态
     * @return 设置设备旁路的请求
     */
    public static Request bypass(List<Integer> ids, boolean isCancel) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.BYPASS);
        requestBuilder.setRequestId(RequestType.BYPASS_CAMERA_VALUE + createId());

        requestBuilder.setExtension(BypassRequest.request, BypassRequest.newBuilder().addAllZoneId(ids).setCancel(isCancel).build());

        return requestBuilder.build();
    }

    /**
     * 获取无线局域网接入规则的请求
     * @param port 无线局域网的端口
     * @return 获取无线局域网接入规则的请求
     */
    public static Request getWlanAccessRules(WlanPort port) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.GET_WLAN_ACCESS_RULES);
        requestBuilder.setRequestId(RequestType.GET_WLAN_ACCESS_RULES_VALUE + createId());

        requestBuilder.setExtension(GetWlanAccessRulesRequest.request, GetWlanAccessRulesRequest.newBuilder().setPort(port).build());

        return requestBuilder.build();
    }

    /**
     * 获取设置无线局域网接入规则的请求
     * @param port 无线局域网的接口
     * @param macs 终端设备的MAC地址列表
     * @return 设置无线局域网接入规则的请求
     */
    public static Request setWlanAccessRules(WlanPort port, List<String> macs) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.SET_WLAN_ACCESS_RULES);
        requestBuilder.setRequestId(RequestType.SET_WLAN_ACCESS_RULES_VALUE + createId());

        requestBuilder.setExtension(SetWlanAccessRulesRequest.request, SetWlanAccessRulesRequest.newBuilder().setPort(port).addAllRules(macs).build());

        return requestBuilder.build();
    }

    /**
     * 获取设置无线局域网接入规则的请求
     * @param port 无线局域网的接口
     * @param macs 终端设备的MAC地址
     * @return 设置无线局域网接入规则的请求
     */
    public static Request setWlanAccessRules(WlanPort port, String macs) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.SET_WLAN_ACCESS_RULES);
        requestBuilder.setRequestId(RequestType.SET_WLAN_ACCESS_RULES_VALUE + createId());

        requestBuilder.setExtension(SetWlanAccessRulesRequest.request, SetWlanAccessRulesRequest.newBuilder().setPort(port).addRules(macs).build());

        return requestBuilder.build();
    }

    /**
     * 获取发送红外命令的请求
     * @param id 红外设备ID
     * @param command 要执行的命令
     * @return 发送红外命令的请求
     */
    public static Request sendIrCommand(int id, Models.InfraredCommand command) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.SEND_IR_COMMAND);
        requestBuilder.setRequestId(RequestType.SEND_IR_COMMAND_VALUE + createId());

        requestBuilder.setExtension(Messages.SendIrCommandRequest.request, Messages.SendIrCommandRequest.newBuilder().setId(id).setCommand(command).build());

        return requestBuilder.build();
    }

    /**
     * 获取解锁门锁的请求
     * @param deviceId 门锁设备ID
     * @return 解锁门锁的请求
     */
    public static Request unlock(int deviceId) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.UNLOCK);
        requestBuilder.setRequestId(RequestType.UNLOCK_VALUE + createId());

        requestBuilder.setExtension(Messages.UnlockRequest.request, Messages.UnlockRequest.newBuilder().setSmartLockId(deviceId).build());

        return requestBuilder.build();
    }

    public static Request streamMultiplexingUnit(int streamId, Messages.StreamMultiplexingUnit.UnitType unitType) {
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.MULTIPLEX_STREAM);
        requestBuilder.setRequestId(RequestType.MULTIPLEX_STREAM_VALUE + createId());

        final Messages.StreamMultiplexingUnit unit = Messages.StreamMultiplexingUnit.newBuilder()
                .setType(unitType)
                .setStreamId(streamId)
                .build();
        requestBuilder.setExtension(Messages.StreamMultiplexingUnit.request, unit);

        return requestBuilder.build();
    }


    public  static Request collectDiagnosticInfo(){
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.COLLECT_DIAGNOSTIC_INFO);
        requestBuilder.setRequestId(RequestType.COLLECT_DIAGNOSTIC_INFO_VALUE + createId());
        return requestBuilder.build();
    }



    public static Request stopWarning(){
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(RequestType.STOP_WARNING);
        requestBuilder.setRequestId(RequestType.STOP_WARNING_VALUE + createId());
        return requestBuilder.build();
    }



}












