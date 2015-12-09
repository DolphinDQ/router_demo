/**
 * 
 */
package mrtech.smarthome.util;

import java.util.List;

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
 * @date 2015年4月13日 下午10:48:14
 * @version 1.0
 */
public class RequestUtil
{
	public static Request getAuthRequest(String apiKey)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.AUTHENTICATE);
		requestBuilder.setRequestId(RequestType.AUTHENTICATE_VALUE);
		
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
//		requestBuilder.setRequestId(RequestType.AUTHENTICATE_VALUE);
//		
//		AuthenticateRequest.Builder auBuilder = AuthenticateRequest.newBuilder();
//		auBuilder.setApiKey(Constants.API_KEY);
//		auBuilder.setProtocol(Long.valueOf("2644132401560307670"));
//		auBuilder.setVersion(1);
//		requestBuilder.setExtension(AuthenticateRequest.request, auBuilder.build());
//		
//		return requestBuilder.build();
//	}
	
	public static Request getKeepAliveRequest()
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.KEEP_ALIVE);
		requestBuilder.setRequestId(RequestType.KEEP_ALIVE_VALUE);
		return requestBuilder.build();
	}
	
	public static Request getSystemLogRequest(SystemLogQuery query)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.QUERY_SYSTEM_LOG);
		requestBuilder.setRequestId(RequestType.QUERY_SYSTEM_LOG_VALUE);
		
		QuerySystemLogRequest.Builder builder = QuerySystemLogRequest.newBuilder();
		builder.setSystemLogQuery(query);
		requestBuilder.setExtension(QuerySystemLogRequest.request, builder.build());
		
		return requestBuilder.build();
	}
	
	public static Request getTimeline(TimelineQuery query)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.QUERY_TIMELINE);
		requestBuilder.setRequestId(RequestType.QUERY_TIMELINE_VALUE);
		
		QueryTimelineRequest.Builder timelineBuilder = QueryTimelineRequest.newBuilder();
		timelineBuilder.setQuery(query);
		
		requestBuilder.setExtension(QueryTimelineRequest.request, timelineBuilder.build());
		return requestBuilder.build();
	}
	
	public static Request getDevices(DeviceQuery query)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.QUERY_DEVICE);
		requestBuilder.setRequestId(RequestType.QUERY_DEVICE_VALUE);
		
		QueryDeviceRequest.Builder queryDeviceRequestBuilder = QueryDeviceRequest.newBuilder();
		queryDeviceRequestBuilder.setQuery(query);
		
		requestBuilder.setExtension(QueryDeviceRequest.request, queryDeviceRequestBuilder.build());
		return requestBuilder.build();
	}
	
	public static Request getGroup(GroupQuery groupQuery)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.QUERY_GROUP);
		requestBuilder.setRequestId(RequestType.QUERY_GROUP_VALUE);
		
		QueryGroupRequest.Builder queryGroupRequestBuilder = QueryGroupRequest.newBuilder();
		queryGroupRequestBuilder.setQuery(groupQuery);
		
		requestBuilder.setExtension(QueryGroupRequest.request, queryGroupRequestBuilder.build());
		
		return requestBuilder.build();
	}
	
	public static Request getScene(SceneQuery query)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.QUERY_SCENE);
		requestBuilder.setRequestId(RequestType.QUERY_SCENE_VALUE);
		
		QuerySceneRequest.Builder querySceneRequestBuilder = QuerySceneRequest.newBuilder();
		querySceneRequestBuilder.setQuery(query);
		
		requestBuilder.setExtension(QuerySceneRequest.request, querySceneRequestBuilder.build());
		
		return requestBuilder.build();
	}
	
	public static Request searchCamera()
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.SEARCH_CAMERA);
		requestBuilder.setRequestId(RequestType.SEARCH_CAMERA_VALUE);
		return requestBuilder.build();
	}
	
	public static Request getOneKeyMatch()
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.TOGGLE_EZMODE);
		requestBuilder.setRequestId(RequestType.TOGGLE_EZMODE_VALUE);
		return requestBuilder.build();
	}
	
	public static Request createGroup(String groupName)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.CREATE_GROUP);
		requestBuilder.setRequestId(RequestType.CREATE_GROUP_VALUE);
		
		CreateGroupRequest.Builder createGroupRequestBuilder = CreateGroupRequest.newBuilder();
		createGroupRequestBuilder.setName(groupName);
		
		requestBuilder.setExtension(CreateGroupRequest.request, createGroupRequestBuilder.build());
		return requestBuilder.build();
	}
	
	public static Request updateGroup(int groupId, String name)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.UPDATE_GROUP);
		requestBuilder.setRequestId(RequestType.UPDATE_GROUP_VALUE);
		
		UpdateGroupRequest.Builder updateGroupRequestBuilder = UpdateGroupRequest.newBuilder();
		updateGroupRequestBuilder.setId(groupId);
		updateGroupRequestBuilder.setName(name);
		
		requestBuilder.setExtension(UpdateGroupRequest.request, updateGroupRequestBuilder.build());
		return requestBuilder.build();
	}
	
	public static Request deleteGroup(int group)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.DELETE_GROUP);
		requestBuilder.setRequestId(RequestType.DELETE_GROUP_VALUE);
		
		DeleteGroupRequest.Builder deleteGroupRequestBuilder = DeleteGroupRequest.newBuilder();
		deleteGroupRequestBuilder.addGroupId(group);
		
		requestBuilder.setExtension(DeleteGroupRequest.request, deleteGroupRequestBuilder.build());
		
		return requestBuilder.build();
	}
	
	public static Request setDeviceAlias(int deviceId, String alias)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.SET_DEVICE_ALIAS);
		requestBuilder.setRequestId(RequestType.SET_DEVICE_ALIAS_VALUE);
		
		SetDeviceAliasRequest setDeviceAliasRequest = SetDeviceAliasRequest.newBuilder().setDeviceId(deviceId).setAlias(alias).build();
		requestBuilder.setExtension(SetDeviceAliasRequest.request, setDeviceAliasRequest);
		
		return requestBuilder.build();
	}
	
	public static Request deleteDevice(int deviceId)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.DELETE_DEVICES);
		requestBuilder.setRequestId(RequestType.DELETE_DEVICES_VALUE);
		
		requestBuilder.setExtension(DeleteDevicesRequest.request, DeleteDevicesRequest.newBuilder().addDeviceIdList(deviceId).build());
		return requestBuilder.build();
	}
	
	public static Request deleteDevice(List<Integer> deviceIds)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.DELETE_DEVICES);
		requestBuilder.setRequestId(RequestType.DELETE_DEVICES_VALUE);
		
		requestBuilder.setExtension(DeleteDevicesRequest.request, DeleteDevicesRequest.newBuilder().addAllDeviceIdList(deviceIds).build());
		return requestBuilder.build();
	}
	
	public static Request saveCamera(Device device)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.SAVE_CAMERA);
		requestBuilder.setRequestId(RequestType.SAVE_CAMERA_VALUE);
		
		requestBuilder.setExtension(SaveCameraRequest.request, SaveCameraRequest.newBuilder().setDevice(device).build());
		
		return requestBuilder.build();
	}
	
	public static Request toggleOnOff(boolean onOff, TargetType targetType, int targetId, DeviceId deviceId)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.TOGGLE_ON_OFF);
		requestBuilder.setRequestId(RequestType.TOGGLE_ON_OFF_VALUE);
		
		ToggleOnOffRequest.Builder toggleOnOffRequestBuilder = ToggleOnOffRequest.newBuilder();
		
		if(targetType != TargetType.TARGET_TYPE_NOT_SPECIFIED)
		{
			toggleOnOffRequestBuilder.setTargetType(targetType);
		}
//		toggleOnOffRequestBuilder.setType(deviceId);
		toggleOnOffRequestBuilder.setTargetId(targetId);
		toggleOnOffRequestBuilder.setState(onOff);
		
		requestBuilder.setExtension(ToggleOnOffRequest.request, toggleOnOffRequestBuilder.build());
		
		return requestBuilder.build();
	}
	
	public static Request toggleOnOff(ToggleOnOffRequest toggleOnOffRequest)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.TOGGLE_ON_OFF);
		requestBuilder.setRequestId(RequestType.TOGGLE_ON_OFF_VALUE);
		
		requestBuilder.setExtension(ToggleOnOffRequest.request, toggleOnOffRequest);
		
		return requestBuilder.build();
	}
	
	public static Request setGroup(int deviceId, int groupId)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.SET_GROUP);
		requestBuilder.setRequestId(RequestType.SET_GROUP_VALUE);
		
		SetGroupRequest.Builder setGroupRequestBuilder = SetGroupRequest.newBuilder();
		setGroupRequestBuilder.addDeviceId(deviceId);
		if(groupId > -1)
			setGroupRequestBuilder.setGroupId(groupId);
		
		requestBuilder.setExtension(SetGroupRequest.request, setGroupRequestBuilder.build());
		
		return requestBuilder.build();
	}
	
	public static Request setGroup(List<Integer> deviceIds, int groupId)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.SET_GROUP);
		requestBuilder.setRequestId(RequestType.SET_GROUP_VALUE);
		
		SetGroupRequest.Builder setGroupRequestBuilder = SetGroupRequest.newBuilder();
		setGroupRequestBuilder.addAllDeviceId(deviceIds);
		if(groupId > -1)
			setGroupRequestBuilder.setGroupId(groupId);
		
		requestBuilder.setExtension(SetGroupRequest.request, setGroupRequestBuilder.build());
		
		return requestBuilder.build();
	}
	
//	public static Request createScene()
//	{
//		Request.Builder requestBuilder = Request.newBuilder();
//		requestBuilder.setType(RequestType.CREATE_SCENE);
//		requestBuilder.setRequestId(RequestType.CREATE_SCENE_VALUE);
//		
//		CreateSceneRequest.newBuilder().setScene(Scene.newBuilder().setName("").addActions(value))
//		
//		return requestBuilder.build();
//	}
	
	public static Request deleteScene(int sceneId)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.DELETE_SCENE);
		requestBuilder.setRequestId(RequestType.DELETE_SCENE_VALUE);
		
		requestBuilder.setExtension(DeleteSceneRequest.request, DeleteSceneRequest.newBuilder().addScenes(sceneId).build());
		
		return requestBuilder.build();
	}
	
	public static Request queryZone(ZoneQuery zoneQuery)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.QUERY_ZONE);
		requestBuilder.setRequestId(RequestType.QUERY_ZONE_VALUE);
		
		requestBuilder.setExtension(QueryZoneRequest.request, QueryZoneRequest.newBuilder().setQuery(zoneQuery).build());
		
		return requestBuilder.build();
	}
	
	public static Request setEvent()
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.SET_EVENTS);
		requestBuilder.setRequestId(RequestType.SET_EVENTS_VALUE);
		requestBuilder.setExtension(SetEventsRequest.request, SetEventsRequest.newBuilder()
				.addEvents(EventType.DISCONNECT)
				.addEvents(EventType.SYS_CONFIG_CHANGED)
				.addEvents(EventType.EZMODE_STATUS_CHANGED)
				.addEvents(EventType.PERMIT_JOIN_STATUS_CHANGED)
				.addEvents(EventType.NEW_TIMELINE)
				.addEvents(EventType.ON_OFF_STATE_CHANGED)
				.addEvents(EventType.SCENE_CHANGED)
				.addEvents(EventType.PPPOE_STATE_CHANGED)
				.build());
		
		return requestBuilder.build();
	}
	
	public static Request activateScene(int sceneId)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.ACTIVATE_SCENE);
		requestBuilder.setRequestId(RequestType.ACTIVATE_SCENE_VALUE);
		
		requestBuilder.setExtension(ActivateSceneRequest.request, ActivateSceneRequest.newBuilder().setSceneId(sceneId).build());
		return requestBuilder.build();
	}
	
	public static Request createScene(String sceneName, List<Action> actions)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.CREATE_SCENE);
		requestBuilder.setRequestId(RequestType.CREATE_SCENE_VALUE);
		Scene.Builder sceneBuilder = Scene.newBuilder().setName(sceneName);
		if(actions.size() > 0)
		{
			for(Action action : actions)
				sceneBuilder.addActions(action);
		}
		requestBuilder.setExtension(CreateSceneRequest.request, CreateSceneRequest.newBuilder().setScene(sceneBuilder.build()).build());
		
		return requestBuilder.build();
	}
	
	public static Request getWanConfig(WanPort wanPort)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.GET_WAN_CONFIG);
		requestBuilder.setRequestId(RequestType.GET_WAN_CONFIG_VALUE);
		requestBuilder.setExtension(GetWanConfigRequest.request, GetWanConfigRequest.newBuilder().setPort(wanPort).build());
		return requestBuilder.build();
	}
	
	public static Request setWanConfig(WanPort wanPort, WanConfig wanConfig)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.SET_WAN_CONFIG);
		requestBuilder.setRequestId(RequestType.SET_WAN_CONFIG_VALUE);
		requestBuilder.setExtension(SetWanConfigRequest.request, SetWanConfigRequest.newBuilder().setConfig(wanConfig).setPort(wanPort).build());
		return requestBuilder.build();
	}
	
	public static Request getWlanConfig(WlanPort wlanPort)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.GET_WLAN_CONFIG);
		requestBuilder.setRequestId(RequestType.GET_WLAN_CONFIG_VALUE);
		requestBuilder.setExtension(GetWlanConfigRequest.request, GetWlanConfigRequest.newBuilder().setPort(wlanPort).build());
		return requestBuilder.build();
	}
	
	public static Request getWlanAccessPoint()
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.GET_WLAN_ACCESS_POINTS);
		requestBuilder.setRequestId(RequestType.GET_WLAN_ACCESS_POINTS_VALUE);
		return requestBuilder.build();
	}
	
	public static Request setWlanConfig(WlanPort wlanPort, WlanConfig wlanConfig)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.SET_WLAN_CONFIG);
		requestBuilder.setRequestId(RequestType.SET_WLAN_CONFIG_VALUE);
		requestBuilder.setExtension(SetWlanConfigRequest.request, SetWlanConfigRequest.newBuilder().setPort(wlanPort).setConfig(wlanConfig).build());
		return requestBuilder.build();
	}
	
	public static Request getWlanAccessPoint(WlanPort wlanPort)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.GET_WLAN_ACCESS_POINTS);
		requestBuilder.setRequestId(RequestType.GET_WLAN_ACCESS_POINTS_VALUE);
		
		requestBuilder.setExtension(GetWlanAccessPointsRequest.request, GetWlanAccessPointsRequest.newBuilder().setPort(wlanPort).build());
		
		return requestBuilder.build();
	}
	
	public static Request arm(ArmMode armMode)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.ARM);
		requestBuilder.setRequestId(RequestType.ARM_VALUE);
		
		requestBuilder.setExtension(ArmRequest.request, ArmRequest.newBuilder().setMode(armMode).build());
		return requestBuilder.build();
	}
	
	public static Request setArmGroup(List<Integer> zoneIds, ArmGroup armGroup)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.SET_ARM_GROUP);
		requestBuilder.setRequestId(RequestType.SET_ARM_GROUP_VALUE);
		
		requestBuilder.setExtension(SetArmGroupRequest.request, SetArmGroupRequest.newBuilder().setArmGroup(armGroup).addAllZoneId(zoneIds).build());
		
		return requestBuilder.build();
	}
	
	public static Request getDevices(List<Integer> devices)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.GET_DEVICE);
		requestBuilder.setRequestId(RequestType.GET_DEVICE_VALUE);
		
		requestBuilder.setExtension(GetDeviceRequest.request, GetDeviceRequest.newBuilder().addAllId(devices).build());
		
		return requestBuilder.build();
	}
	
	public static Request updateScene(Scene scene)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.UPDATE_SCENE);
		requestBuilder.setRequestId(RequestType.UPDATE_SCENE_VALUE);
		
		requestBuilder.setExtension(UpdateSceneRequest.request, UpdateSceneRequest.newBuilder().setScene(scene).build());
		
		return requestBuilder.build();
	}
	
	public static Request getWanRate(WanPort wanPort)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.GET_WAN_RATE);
		requestBuilder.setRequestId(RequestType.GET_WAN_RATE_VALUE);
		
		requestBuilder.setExtension(GetWanRateRequest.request, GetWanRateRequest.newBuilder().setPort(wanPort).build());
		
		return requestBuilder.build();
	}
	
	public static Request getNetWorkDevice()
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.GET_NETWORK_DEVICE);
		requestBuilder.setRequestId(RequestType.GET_NETWORK_DEVICE_VALUE);
		
		return requestBuilder.build();
	}
	
	public static Request getAclRuleMode()
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.GET_ACL_RULE_MODE);
		requestBuilder.setRequestId(RequestType.GET_ACL_RULE_MODE_VALUE);
		
		return requestBuilder.build();
	}
	
	public static Request getInternetAclRules()
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.GET_INTERNET_ACL_RULES);
		requestBuilder.setRequestId(RequestType.GET_INTERNET_ACL_RULES_VALUE);
		
		return requestBuilder.build();
	}
	
	public static Request getSambaAclRules()
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.GET_SAMBA_ACL_RULES);
		requestBuilder.setRequestId(RequestType.GET_SAMBA_ACL_RULES_VALUE);
		
		return requestBuilder.build();
	}
	
	public static Request getEthernetConfig()
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.GET_ETHERNET_CONFIG);
		requestBuilder.setRequestId(RequestType.GET_ETHERNET_CONFIG_VALUE);
		
		return requestBuilder.build();
	}
	
	public static Request getCameraInfo(int deviceId)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.GET_CAMERA_INFO);
		requestBuilder.setRequestId(RequestType.GET_CAMERA_INFO_VALUE);
		
		requestBuilder.setExtension(GetCameraInfoRequest.request, GetCameraInfoRequest.newBuilder().setDeviceId(deviceId).build());
		return requestBuilder.build();
	}
	
	public static Request configureCameraRecord(int deviceId, CameraRecordConfiguration cameraRecordConfiguration)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.CONFIGURE_CAMERA_RECORD);
		requestBuilder.setRequestId(RequestType.CONFIGURE_CAMERA_RECORD_VALUE);
		
		requestBuilder.setExtension(ConfigureCameraRecordRequest.request, ConfigureCameraRecordRequest.newBuilder().setDeviceId(deviceId).setRecord(cameraRecordConfiguration).build());
		
		return requestBuilder.build();
	}
	
	public static Request getGuestWlanConfig(WlanPort wlanPort)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.GET_GUEST_WLAN_CONFIG);
		requestBuilder.setRequestId(RequestType.GET_GUEST_WLAN_CONFIG_VALUE);
		requestBuilder.setExtension(GetGuestWlanConfigRequest.request, GetGuestWlanConfigRequest.newBuilder().setPort(wlanPort).build());
		
		return requestBuilder.build();
	}
	
	public static Request setGuestWlanConfig(WlanPort wlanPort, GuestWlanConfig guestWlanConfig)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.SET_GUEST_WLAN_CONFIG);
		requestBuilder.setRequestId(RequestType.SET_GUEST_WLAN_CONFIG_VALUE);
		
		requestBuilder.setExtension(SetGuestWlanConfigRequest.request, SetGuestWlanConfigRequest.newBuilder().setPort(wlanPort).setConfig(guestWlanConfig).build());
		
		return requestBuilder.build();
	}
	
	public static Request getHDDInfo()
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.GET_HDD_INFO);
		requestBuilder.setRequestId(RequestType.GET_HDD_INFO_VALUE);
		
		return requestBuilder.build();
	}
	
	public static Request initializeHDD()
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.INITIALIZE_HDD);
		requestBuilder.setRequestId(RequestType.INITIALIZE_HDD_VALUE);
		
		return requestBuilder.build();
	}
	
	public static Request removeHDD()
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.REMOVE_HDD);
		requestBuilder.setRequestId(RequestType.REMOVE_HDD_VALUE);
		
		return requestBuilder.build();
	}
	
	public static Request reboot()
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.REBOOT);
		requestBuilder.setRequestId(RequestType.REBOOT_VALUE);
		
		return requestBuilder.build();
	}
	
	public static Request getProductInfo()
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.GET_PRODUCT_INFO);
		requestBuilder.setRequestId(RequestType.GET_PRODUCT_INFO_VALUE);
		
		return requestBuilder.build();
	}
	
	public static Request getSysConfig()
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.GET_SYS_CONFIG);
		requestBuilder.setRequestId(RequestType.GET_SYS_CONFIG_VALUE);
		
		return requestBuilder.build();
	}
	
	public static Request queryPlan(PlanQuery planQuery)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.QUERY_PLAN);
		requestBuilder.setRequestId(RequestType.QUERY_PLAN_VALUE);
		
		requestBuilder.setExtension(QueryPlanRequest.request, QueryPlanRequest.newBuilder().setQuery(planQuery).build());
		
		return requestBuilder.build();
	}
	
	public static Request deletePlan(int planId)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.DELETE_PLAN);
		requestBuilder.setRequestId(RequestType.DELETE_PLAN_VALUE);
		
		requestBuilder.setExtension(DeletePlanRequest.request, DeletePlanRequest.newBuilder().addPlans(planId).build());
		
		return requestBuilder.build();
	}
	
	public static Request setPlanEnable(int planId, boolean enable)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.SET_PLAN_ENABLED);
		requestBuilder.setRequestId(RequestType.SET_PLAN_ENABLED_VALUE);
		
		requestBuilder.setExtension(SetPlanEnabledRequest.request, SetPlanEnabledRequest.newBuilder().setPlanId(planId).setEnabled(enable).build());
		
		return requestBuilder.build();
	}
	
	public static Request setPlanAction(int planId, List<Action> actions)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.SET_PLAN_ACTION);
		requestBuilder.setRequestId(RequestType.SET_PLAN_ACTION_VALUE);
		
		requestBuilder.setExtension(SetPlanActionRequest.request, SetPlanActionRequest.newBuilder().setPlanId(planId).addAllActions(actions).build());
		
		return requestBuilder.build();
	}
	
	public static Request createPlan(Plan plan)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.CREATE_PLAN);
		requestBuilder.setRequestId(RequestType.CREATE_PLAN_VALUE);
		
		requestBuilder.setExtension(CreatePlanRequest.request, CreatePlanRequest.newBuilder().setPlan(plan).build());
		
		return requestBuilder.build();
	}
	
	public static Request deletePlans(List<Integer> planIds)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.DELETE_PLAN);
		requestBuilder.setRequestId(RequestType.DELETE_PLAN_VALUE);
		
		requestBuilder.setExtension(DeletePlanRequest.request, DeletePlanRequest.newBuilder().addAllPlans(planIds).build());
		
		return requestBuilder.build();
	}
	
	public static Request updatePlan(Plan plan)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.UPDATE_PLAN);
		requestBuilder.setRequestId(RequestType.UPDATE_PLAN_VALUE);
		
		requestBuilder.setExtension(UpdatePlanRequest.request, UpdatePlanRequest.newBuilder().setPlan(plan).build());
		
		return requestBuilder.build();
	}
	
	public static Request pppoeConnect(boolean isConnect)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.PPPOE_CONNECT);
		requestBuilder.setRequestId(RequestType.PPPOE_CONNECT_VALUE);
		
		requestBuilder.setExtension(PPPoEConnectRequest.request, PPPoEConnectRequest.newBuilder().setConnect(isConnect).build());
		
		return requestBuilder.build();
	}
	
	public static Request getEzmodeCountDown()
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.GET_EZMODE_COUNTDOWN);
		requestBuilder.setRequestId(RequestType.GET_EZMODE_COUNTDOWN_VALUE);
		
		return requestBuilder.build();
	}
	
	public static Request getQosMode()
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.GET_QOS_MODE);
		requestBuilder.setRequestId(RequestType.GET_QOS_MODE_VALUE);
		
		return requestBuilder.build();
	}
	
	public static Request getQosExclusiveModeMac()
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.GET_QOS_EXCLUSIVE_MODE_MAC);
		requestBuilder.setRequestId(RequestType.GET_QOS_EXCLUSIVE_MODE_MAC_VALUE);
		
		return requestBuilder.build();
	}
	
	public static Request setQosExclusiveModeMac(String mac)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.SET_QOS_EXCLUSIVE_MODE_MAC);
		requestBuilder.setRequestId(RequestType.SET_QOS_EXCLUSIVE_MODE_MAC_VALUE);
		requestBuilder.setExtension(SetQosExclusiveModeMacRequest.request, SetQosExclusiveModeMacRequest.newBuilder().setMac(mac).build());
		
		return requestBuilder.build();
	}
	
	public static Request testBandwidth(boolean isStart)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.TEST_BANDWIDTH);
		requestBuilder.setRequestId(RequestType.TEST_BANDWIDTH_VALUE);
		requestBuilder.setExtension(TestBandwidthRequest.request, TestBandwidthRequest.newBuilder().setStart(isStart).build());
		
		return requestBuilder.build();
	}
	
	public static Request bandwidthTestResult()
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.BANDWIDTH_TEST_RESULT);
		requestBuilder.setRequestId(RequestType.BANDWIDTH_TEST_RESULT_VALUE);
		
		return requestBuilder.build();
	}
	
	public static Request getSignalStrengthLevel()
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.GET_SIGNAL_STRENGTH_LEVEL);
		requestBuilder.setRequestId(RequestType.GET_SIGNAL_STRENGTH_LEVEL_VALUE);
		
		return requestBuilder.build();
	}
	
	public static Request setSignalStrengthLevel(SignalStrengthLevel signalStrengthLevel)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.SET_SIGNAL_STRENGTH_LEVEL);
		requestBuilder.setRequestId(RequestType.SET_SIGNAL_STRENGTH_LEVEL_VALUE);
		requestBuilder.setExtension(SetSignalStrengthLevelRequest.request, SetSignalStrengthLevelRequest.newBuilder().setLevel(signalStrengthLevel).build());
		
		return requestBuilder.build();
	}
	
	public static Request getQosVpnConfig()
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.GET_QOS_VPN_CONFIG);
		requestBuilder.setRequestId(RequestType.GET_QOS_VPN_CONFIG_VALUE);
		
		return requestBuilder.build();
	}
	
	public static Request setQosVpnConfig(SetQosVpnConfigRequest qosVpnConfigRequest)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.SET_QOS_VPN_CONFIG);
		requestBuilder.setRequestId(RequestType.SET_QOS_VPN_CONFIG_VALUE);
		
		requestBuilder.setExtension(SetQosVpnConfigRequest.request, qosVpnConfigRequest);
		
		return requestBuilder.build();
	}
	
	public static Request setInternetAclRules(List<String> macs)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.SET_INTERNET_ACL_RULES);
		requestBuilder.setRequestId(RequestType.SET_INTERNET_ACL_RULES_VALUE);
		
		requestBuilder.setExtension(SetInternetAclRulesRequest.request, SetInternetAclRulesRequest.newBuilder().addAllRules(macs).build());
		
		return requestBuilder.build();
	}
	
	public static Request deleteInternetAclRules(List<String> macs)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.DELETE_INTERNET_ACL_RULES);
		requestBuilder.setRequestId(RequestType.DELETE_INTERNET_ACL_RULES_VALUE);
		
		requestBuilder.setExtension(DeleteInternetAclRulesRequest.request, DeleteInternetAclRulesRequest.newBuilder().addAllRules(macs).build());
		
		return requestBuilder.build();
	}
	
	public static Request deleteInternetAclRules(String mac)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.DELETE_INTERNET_ACL_RULES);
		requestBuilder.setRequestId(RequestType.DELETE_INTERNET_ACL_RULES_VALUE);
		
		requestBuilder.setExtension(DeleteInternetAclRulesRequest.request, DeleteInternetAclRulesRequest.newBuilder().addRules(mac).build());
		
		return requestBuilder.build();
	}
	
	public static Request getCameraWifiAp(int deviceId)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.GET_CAMERA_WIFI_AP);
		requestBuilder.setRequestId(RequestType.GET_CAMERA_WIFI_AP_VALUE);
		
		requestBuilder.setExtension(GetCameraWifiApRequest.request, GetCameraWifiApRequest.newBuilder().setDeviceId(deviceId).build());
		
		return requestBuilder.build();
	}
	
	public static Request setQosMode(QosMode qosMode)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.SET_QOS_MODE);
		requestBuilder.setRequestId(RequestType.SET_QOS_MODE_VALUE);
		
		requestBuilder.setExtension(SetQosModeRequest.request, SetQosModeRequest.newBuilder().setMode(qosMode).build());
		
		return requestBuilder.build();
	}
	
	public static Request setWlanAndGuestConfig(WlanPort wlanPort, WlanConfig wlanConfig, GuestWlanConfig guestWlanConfig)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.SET_WLAN_AND_GUEST_CONFIG);
		requestBuilder.setRequestId(RequestType.SET_WLAN_AND_GUEST_CONFIG_VALUE);
		
		requestBuilder.setExtension(SetWlanAndGuestConfigRequest.request, SetWlanAndGuestConfigRequest.newBuilder().setPort(wlanPort).setWlanConfig(wlanConfig).setGuestConfig(guestWlanConfig).build());
		
		return requestBuilder.build();
	}
	
	public static Request getWlanAndGuestConfig(WlanPort wlanPort)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.GET_WLAN_AND_GUEST_CONFIG);
		requestBuilder.setRequestId(RequestType.GET_WLAN_AND_GUEST_CONFIG_VALUE);
		
		requestBuilder.setExtension(GetWlanAndGuestConfigRequest.request, GetWlanAndGuestConfigRequest.newBuilder().setPort(wlanPort).build());
		
		return requestBuilder.build();
	}
	
	public static Request checkUpdate()
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.CHECK_UPDATE);
		requestBuilder.setRequestId(RequestType.CHECK_UPDATE_VALUE);
		
		return requestBuilder.build();
	}
	
	public static Request installUpdate()
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.INSTALL_UPDATE);
		requestBuilder.setRequestId(RequestType.INSTALL_UPDATE_VALUE);
		
		return requestBuilder.build();
	}
	
	public static Request ready2Update()
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.READY_TO_UPDATE);
		requestBuilder.setRequestId(RequestType.READY_TO_UPDATE_VALUE);
		
		return requestBuilder.build();
	}
	
	public static Request getOtaStatus()
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.GET_OTA_STATUS);
		requestBuilder.setRequestId(RequestType.GET_OTA_STATUS_VALUE);
		
		return requestBuilder.build();
	}
	
	public static Request configureCameraWlan(int deviceId, CameraWlan cameraWlan)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.CONFIGURE_CAMERA_WLAN);
		requestBuilder.setRequestId(RequestType.CONFIGURE_CAMERA_WLAN_VALUE);
		
		requestBuilder.setExtension(SetCameraWlanRequest.request, SetCameraWlanRequest.newBuilder().setDeviceId(deviceId).setWlan(cameraWlan).build());
		
		return requestBuilder.build();
	}
	
	public static Request setSysConfig(SystemConfiguration systemConfiguration)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.SET_SYS_CONFIG);
		requestBuilder.setRequestId(RequestType.SET_SYS_CONFIG_VALUE);
		
		requestBuilder.setExtension(SetSystemConfigurationRequest.request, SetSystemConfigurationRequest.newBuilder().setConfiguration(systemConfiguration).build());
		
		return requestBuilder.build();
	}
	
	public static Request getCieConfig()
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.GET_CIE_CONFIG);
		requestBuilder.setRequestId(RequestType.GET_CIE_CONFIG_VALUE);
		
		return requestBuilder.build();
	}
	
	public static Request getMobileInfo()
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.GET_MOBILE_INFO);
		requestBuilder.setRequestId(RequestType.GET_MOBILE_INFO_VALUE);
		
		return requestBuilder.build();
	}
	
	public static Request setMobilePhoneConfig(MobilePhoneConfig mobilePhoneConfig)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.SET_MOBILE_PHONE_CONFIG);
		requestBuilder.setRequestId(RequestType.SET_MOBILE_PHONE_CONFIG_VALUE);
		requestBuilder.setExtension(SetMobilePhoneConfigRequest.request, SetMobilePhoneConfigRequest.newBuilder().setConfig(mobilePhoneConfig).build());
		
		return requestBuilder.build();
	}
	
	public static Request getMobilePhoneConfig()
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.GET_MOBILE_PHONE_CONFIG);
		requestBuilder.setRequestId(RequestType.GET_MOBILE_PHONE_CONFIG_VALUE);
		
		return requestBuilder.build();
	}
	
	public static Request getBatteryInfo()
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.GET_BATTERY_INFO);
		requestBuilder.setRequestId(RequestType.GET_BATTERY_INFO_VALUE);
		
		return requestBuilder.build();
	}
	
	public static Request bypass(List<Integer> ids, boolean isCancel)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.BYPASS);
		requestBuilder.setRequestId(RequestType.BYPASS_CAMERA_VALUE);
		
		requestBuilder.setExtension(BypassRequest.request, BypassRequest.newBuilder().addAllZoneId(ids).setCancel(isCancel).build());
		
		return requestBuilder.build();
	}
	
	public static Request getWlanAccessRules(WlanPort port)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.GET_WLAN_ACCESS_RULES);
		requestBuilder.setRequestId(RequestType.GET_WLAN_ACCESS_RULES_VALUE);
		
		requestBuilder.setExtension(GetWlanAccessRulesRequest.request, GetWlanAccessRulesRequest.newBuilder().setPort(port).build());
		
		return requestBuilder.build();
	}
	
	public static Request setWlanAccessRules(WlanPort port, List<String> macs)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.SET_WLAN_ACCESS_RULES);
		requestBuilder.setRequestId(RequestType.SET_WLAN_ACCESS_RULES_VALUE);
		
		requestBuilder.setExtension(SetWlanAccessRulesRequest.request, SetWlanAccessRulesRequest.newBuilder().setPort(port).addAllRules(macs).build());
		
		return requestBuilder.build();
	}
	
	public static Request setWlanAccessRules(WlanPort port, String macs)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.SET_WLAN_ACCESS_RULES);
		requestBuilder.setRequestId(RequestType.SET_WLAN_ACCESS_RULES_VALUE);
		
		requestBuilder.setExtension(SetWlanAccessRulesRequest.request, SetWlanAccessRulesRequest.newBuilder().setPort(port).addRules(macs).build());
		
		return requestBuilder.build();
	}
	
	public static Request setSambaAclRules(List<String> macs)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.SET_SAMBA_ACL_RULES);
		requestBuilder.setRequestId(RequestType.SET_SAMBA_ACL_RULES_VALUE);
		
		requestBuilder.setExtension(SetSambaAclRulesRequest.request, SetSambaAclRulesRequest.newBuilder().addAllRules(macs).build());
		
		return requestBuilder.build();
	}
	
	public static Request deleteSambaAclRules(List<String> macs)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.DELETE_SAMBA_ACL_RULES);
		requestBuilder.setRequestId(RequestType.DELETE_SAMBA_ACL_RULES_VALUE);
		
		requestBuilder.setExtension(DeleteSambaAclRulesRequest.request, DeleteSambaAclRulesRequest.newBuilder().addAllRules(macs).build());
		
		return requestBuilder.build();
	}
	
	public static Request deleteSambaAclRules(String macs)
	{
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setType(RequestType.DELETE_SAMBA_ACL_RULES);
		requestBuilder.setRequestId(RequestType.DELETE_SAMBA_ACL_RULES_VALUE);
		
		requestBuilder.setExtension(DeleteSambaAclRulesRequest.request, DeleteSambaAclRulesRequest.newBuilder().addRules(macs).build());
		
		return requestBuilder.build();
	}
}












