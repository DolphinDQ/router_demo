package mrtech.smarthome.router;

import android.util.Log;

import com.google.protobuf.InvalidProtocolBufferException;
import com.orm.SugarRecord;
import com.orm.dsl.Table;
import com.squareup.okhttp.Route;

import java.util.List;

import mrtech.smarthome.SmartHomeApp;
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

    private static void trace(String msg) {

        if (SmartHomeApp.DEBUG)
            Log.d(RouterCacheProvider.class.getName(), msg);
    }


    public RouterCacheProvider(Router router, CommunicationManager communicationManager) {
        mRouter = router;
        communicationManager.subscribeResponse(new Action1<Messages.Response>() {
            @Override
            public void call(Messages.Response response) {
                final int typeValue = RequestUtil.getRequestTypeValue(response);

                final List<ResponseCache> responseCaches = SugarRecord.find(ResponseCache.class,
                        " type = ? and ROUTER_ID = ?", "" + typeValue, mRouter.getConfig().getId() + "");
                boolean isSave = responseCaches.size() > 0 && response.getErrorCode() == Messages.Response.ErrorCode.SUCCESS;
                trace("request type of " + Messages.Request.RequestType.valueOf(typeValue) + " need to be cached ? " + isSave);
                if (isSave) {
                    final ResponseCache cache = responseCaches.get(0);
                    if (cache != null) {
                        cache.data = response.toByteArray();
                        cache.save();
                    }
                }
            }
        });
    }

    private ResponseCache getCache(Messages.Request.RequestType type) {
        int typeValue = type.getNumber();
        final List<ResponseCache> caches = SugarRecord.find(ResponseCache.class,
                "type = ? and ROUTER_ID = ?", typeValue + "", mRouter.getConfig().getId() + "");
        ResponseCache result = null;
        if (caches.size() > 0) {
            result = caches.get(0);
        }
        if (result == null) {
            result = new ResponseCache(mRouter.getConfig().getId(), typeValue);
            result.save();
        }
        return result;
    }

    public Messages.Response getResponseCache(Messages.Request.RequestType type) {
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
