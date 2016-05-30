package mrtech.smarthome.router.Models;

import java.util.HashMap;

import mrtech.smarthome.rpc.Messages;
import mrtech.smarthome.rpc.Models;
import mrtech.smarthome.util.RequestUtil;

/**
 * 数据缓存类型。
 * Created by sphynx on 2016/4/20.
 */
public enum CacheType {

    /**
     * 摄像头缓存。
     */
    CAMERA_DEVICE(Messages.Request.RequestType.QUERY_DEVICE, 1),
    /**
     * Zigbee设备缓存。
     */
    ZIGBEE_DEVICE(Messages.Request.RequestType.QUERY_DEVICE, 2),
    /**
     * 缓存新时间线。
     */
    TIMELINE_NEW(Messages.Request.RequestType.QUERY_TIMELINE, 1),;
    private final int cacheType;

    /**
     * 新添加存储类型，需要指定请求类型，以及编号。
     * 注意：添加枚举类型，需要修改getCacheType方法，指定Request或Response 需要缓存的类型。
     *
     * @param type  缓存的请求类型。
     * @param value 缓存编号。
     */
    CacheType(Messages.Request.RequestType type, int value) {
        cacheType = value << 16 | type.getNumber();

    }

    public int getCacheTypeValue() {
        return cacheType;
    }

    /**
     * 通过请求数据获取缓存类型。
     *
     * @param request 请求
     * @return 缓存类型。null为不缓存
     */
    public static CacheType getCacheType(Messages.Request request) {
        if (request != null) {
            final Messages.Request.RequestType requestType = request.getType();
            if (requestType != null) {
                switch (requestType) {
                    case QUERY_DEVICE:
                        final Messages.QueryDeviceRequest queryDeviceRequest = request.getExtension(Messages.QueryDeviceRequest.request);
                        if (queryDeviceRequest != null && queryDeviceRequest.getQuery() != null) {
                            switch (queryDeviceRequest.getQuery().getType()) { //缓存查询设备响应数据，条件为：返回设备类型
                                case DEVICE_TYPE_CAMERA:
                                    return CAMERA_DEVICE;
                                case DEVICE_TYPE_ZIGBEE:
                                    return ZIGBEE_DEVICE;
                            }
                        }
                        break;
                    case QUERY_TIMELINE:
                        final Messages.QueryTimelineRequest queryTimelineRequest = request.getExtension(Messages.QueryTimelineRequest.request);
                        if (queryTimelineRequest != null) {
                            final Models.TimelineQuery query = queryTimelineRequest.getQuery();
                            if (query != null &&
                                    query.getPage() == 0 &&
                                    query.getPageSize() > 0 &&
                                    !query.hasLevel() &&
                                    !query.hasType()) { //缓存新时间线条件。
                                return TIMELINE_NEW;
                            }
                        }
                        break;
                    default:
                        break;
                }
            }

        }
        return null;
    }


}
