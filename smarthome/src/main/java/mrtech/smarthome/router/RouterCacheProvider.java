package mrtech.smarthome.router;

import android.util.Log;

import com.google.protobuf.InvalidProtocolBufferException;
import com.orm.SugarRecord;
import com.orm.annotation.Table;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import mrtech.smarthome.SmartHomeApp;
import mrtech.smarthome.router.Models.CacheType;
import mrtech.smarthome.router.Models.CommunicationManager;
import mrtech.smarthome.rpc.Messages;
import mrtech.smarthome.util.RequestUtil;
import rx.functions.Action1;

/**
 * 数据缓存器。
 * Created by sphynx on 2015/12/11.
 */
class RouterCacheProvider {
    private final Router mRouter;
    /**
     * 缓存列表.key和value均为 requestTypeValue
     */
    private ConcurrentHashMap<Integer, Integer> cacheList = new ConcurrentHashMap<>();

    /**
     * 临时缓存目录。key为RequestId
     */
    private ConcurrentHashMap<Integer, Messages.Request> tempCacheList = new ConcurrentHashMap<>();


    private static void trace(String msg) {
        if (SmartHomeApp.DEBUG)
            Log.d(RouterCacheProvider.class.getName(), msg);
    }

    public RouterCacheProvider(Router router, CommunicationManager communicationManager) {
        mRouter = router;
        communicationManager.subscribeResponse(new Action1<Messages.Response>() {
            @Override
            public void call(Messages.Response response) {
                if (response == null) return;
                final int requestId = response.getRequestId();
                if (response.getErrorCode() == Messages.Response.ErrorCode.SUCCESS) {
                    int typeValue = RequestUtil.getRequestTypeValue(response);
                    boolean isSave = false;
                    if (tempCacheList.containsKey(requestId)) {
                        final CacheType cacheType = CacheType.getCacheType(tempCacheList.get(requestId));
                        if (cacheType != null) { //优先使用CacheType
                            typeValue = cacheType.getCacheTypeValue();
                            isSave = true;
                        }
                    }
                    if (!isSave) {
                        isSave = cacheList.containsKey(typeValue);
                    }
                    trace("request type of " + typeValue + " need to be cached ? " + isSave);
                    if (isSave) {
                        final ResponseCache cache = getCache(typeValue);
                        if (cache != null) {
                            cache.data = response.toByteArray();
                            cache.save();
                        }
                    }
                }
                if (tempCacheList.containsKey(requestId))
                    tempCacheList.remove(requestId);
            }
        });
    }

    private ResponseCache getCache(int type) {
        final List<ResponseCache> caches = SugarRecord.find(ResponseCache.class,
                "type = ? and ROUTER_ID = ?", type + "", mRouter.getConfig().getId() + "");
        ResponseCache result = null;
        if (caches.size() > 0) {
            result = caches.get(0);
        }
        if (result == null) {
            result = new ResponseCache(mRouter.getConfig().getId(), type);
            result.save();
        }
        return result;
    }

    private Messages.Response getResponseCache(int type) {
        final ResponseCache cache = getCache(type);
        if (cache != null && cache.data != null && cache.data.length > 0) {
            try {
                return Messages.Response.parseFrom(cache.data, RouterManager.registry);
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private void registerCacheType(int type) {
        if (cacheList.containsKey(type)) return;
        cacheList.put(type, type);
    }

    public Messages.Response getResponseCache(Messages.Request request) {
        final CacheType cacheType = CacheType.getCacheType(request);
        Messages.Response response = null;
        if (cacheType != null) {
            response = getResponseCache(cacheType.getCacheTypeValue());
        }
        if (response == null) {
            final int type = request.getType().getNumber();
            registerCacheType(type);
            response = getResponseCache(type);
        }
        return response;
    }

    public void  setNewCache(Messages.Request request){
        tempCacheList.put(request.getRequestId(), request);
    }

    @Table
    public static class ResponseCache extends SugarRecord {

        public ResponseCache() {
        }

        public ResponseCache(long routerId, int type) {
            this.routerId = routerId;
            this.type = type;
        }

        private int type;
        private long routerId;
        private byte[] data;
        private long lastUpdateTime;

        public long getRouterId() {
            return routerId;
        }

        public byte[] getData() {
            return data;
        }

        public int getType() {
            return type;
        }

        public long getLastUpdateTime() {
            return lastUpdateTime;
        }

        public void setData(byte[] data) {
            this.data = data;
        }

        @Override
        public long save() {
            lastUpdateTime = System.currentTimeMillis();
            return super.save();
        }
    }
}
