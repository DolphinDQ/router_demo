package mrtech.smarthome.auth;

import android.accounts.AuthenticatorException;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.lang.reflect.Type;

import mrtech.smarthome.auth.Models.AccessToken;
import mrtech.smarthome.auth.Models.ApiCallback;
import mrtech.smarthome.auth.Models.PageResult;
import mrtech.smarthome.auth.Models.PagingQuery;
import mrtech.smarthome.auth.Models.RouterCloudData;
import mrtech.smarthome.router.Models.RouterCallback;
import mrtech.smarthome.router.Router;
import mrtech.smarthome.router.RouterManager;
import mrtech.smarthome.util.CharUtil;
import mrtech.smarthome.util.Constants;
import rx.functions.Action1;
import rx.functions.Action2;

/**
 * 用户管理器。管理用户登录信息。
 * Created by sphynx on 2015/12/29.
 */
public class UserManager {
    private final RouterManager mRouterManager;
    private Context mContext;

    private static void trace(String msg) {
        Log.e(UserManager.class.getName(), msg);
    }

    /**
     * API 定义未登录返回信息为：Unauthorized
     */
    private static final String UNAUTHORIZED_MARK = "Unauthorized";
    private static final String CONFIG_FILE = "USER_CONFIG";
    private static final String AUTH_CONFIG = "auth_key";
    private static UserManager ourInstance = new UserManager();

    private static String getLogonUrl() {
        return Constants.TOKEN_SERVER.concat("connect/token");
    }

    private static String getLogoffUrl() {
        return Constants.TOKEN_SERVER.concat("connect/revocation");
    }

    private static String getLogonContent(String account, String password) {
        String body = "grant_type=password&username=%s&password=%s&scope=api&client_id=%s&client_secret=%s";
        return String.format(body, account, password, Constants.apiKey, Constants.apiSecret);
    }

    private static String getLogoffContent(String accessToken) {
        String body = "token=%s&token_type_hint=access_token&scope=api&client_id=%s&client_secret=%s";
        return String.format(body, accessToken, Constants.apiKey, Constants.apiSecret);
    }

    public final static MediaType MEDIA_TYPE_URLENCODED = MediaType.parse("application/x-www-form-urlencoded");

    public final static MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json");

    public final static OkHttpClient httpclient = new OkHttpClient();

    public final static Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();

    public static UserManager getInstance() {
        return ourInstance;
    }

    public boolean isLogin() {
        return config.getToken() != null && System.currentTimeMillis() < (config.getLoginTime() + config.getToken().getExpiresIn() * 1000);
    }

    public AuthConfig getConfig() {
        return config;
    }

    private AuthConfig config;

    private UserManager() {
        mRouterManager = RouterManager.getInstance();
    }

    /**
     * 用户管理模块初始化。
     */
    public void init(Context context) {
        if (context == null) throw new IllegalArgumentException("context can not be null.");
        mContext = context;
        loadConfig();
        mRouterManager.subscribeRouterCreateEvent(new Action1<RouterCallback<Boolean>>() {
            @Override
            public void call(RouterCallback<Boolean> booleanRouterCallback) {
                if (booleanRouterCallback.getData()) {
                    uploadRouter(booleanRouterCallback.getRouter(), null);
                } else {
                    final Object source = booleanRouterCallback.getRouter().getSource();
                    if (source != null) {
                        removeRouter((Integer) source, null);
                    }
                }
            }
        });
    }

    private void removeRouter(int id, final Action1<Throwable> callback) {
        try {
            executeApiRequest(new TypeToken<ApiCallback<Integer>>() {
                    }.getType(), createApiRequestBuilder(Constants.ServerUrl.ROUTER_CONFIGURATION_DELETE + id)
                            .delete().build(),
                    new Action1<ApiCallback<Integer>>() {
                        @Override
                        public void call(ApiCallback<Integer> apiCallback) {
                            if (apiCallback.isError()) {
                                trace("delete router error : " + apiCallback.getMessage());
                                if (callback != null)
                                    callback.call(new Exception(apiCallback.getMessage()));
                            } else {
                                if (callback != null) callback.call(null);
                            }
                        }
                    });
        } catch (AuthenticatorException e) {
            if (callback != null)
                callback.call(e);
        }
    }

    public void uploadRouter(final Router router, final Action1<Throwable> callback) {
        try {
            final RouterCloudData data = new RouterCloudData(router.getName(), router.getSN());
            if (router.getSource() != null) {
                executeApiRequest(new TypeToken<ApiCallback<Object>>() {
                        }.getType(), createApiRequestBuilder(Constants.ServerUrl.ROUTER_CONFIGURATION_PUT + router.getSource())
                                .put(RequestBody.create(MEDIA_TYPE_JSON, GSON.toJson(data))).build(),
                        new Action1<ApiCallback<Object>>() {
                            @Override
                            public void call(ApiCallback<Object> apiCallback) {
                                if (apiCallback.isError()) {
                                    trace("put router error : " + apiCallback.getMessage());
                                    if (callback != null)
                                        callback.call(new Exception(apiCallback.getMessage()));
                                } else {
                                    if (callback != null) callback.call(null);
                                }
                            }
                        });
            } else {
                executeApiRequest(new TypeToken<ApiCallback<Integer>>() {
                        }.getType(), createApiRequestBuilder(Constants.ServerUrl.ROUTER_CONFIGURATION_POST)
                                .post(RequestBody.create(MEDIA_TYPE_JSON, GSON.toJson(data))).build(),
                        new Action1<ApiCallback<Integer>>() {
                            @Override
                            public void call(ApiCallback<Integer> apiCallback) {
                                if (apiCallback.isError()) {
                                    trace("post router error : " + apiCallback.getMessage());
                                    if (callback != null)
                                        callback.call(new Exception(apiCallback.getMessage()));
                                } else {
                                    router.setSource(apiCallback.getData());
                                    if (callback != null) callback.call(null);
                                }
                            }
                        });
            }
        } catch (AuthenticatorException e) {
            if (callback != null)
                callback.call(e);
        }
    }

    private void loadConfig() {
        final SharedPreferences sharedPreferences = mContext.getSharedPreferences(CONFIG_FILE, Context.MODE_PRIVATE);
        final String configString = sharedPreferences.getString(AUTH_CONFIG, null);
        if (configString == null) {
            saveConfig();
        } else {
            try {
                config = GSON.fromJson(CharUtil.decryptMsg(Base64.decode(configString, Base64.DEFAULT)), AuthConfig.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void saveConfig() {
        if (config == null) {
            config = new AuthConfig();
        }
        final String json = GSON.toJson(config);
        try {
            final SharedPreferences sharedPreferences = mContext.getSharedPreferences(CONFIG_FILE, Context.MODE_PRIVATE);
            final SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putString(AUTH_CONFIG, new String(Base64.encode(CharUtil.encryptMsg(json), Base64.DEFAULT), "UTF-8"));
            edit.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void logon(final String account, final String password, final Action1<Throwable> callback) {
        final Request request = new Request.Builder()
                .url(getLogonUrl())
                .post(RequestBody.create(MEDIA_TYPE_URLENCODED, getLogonContent(account, password)))
                .build();
        executeRequest(AccessToken.class, request, new Action2<AccessToken, Throwable>() {
            @Override
            public void call(AccessToken accessToken, Throwable throwable) {
                if (accessToken != null) {
                    config.setUser(account);
                    config.setPassword(password);
                    config.setToken(accessToken);
                    config.setLoginTime(System.currentTimeMillis());
                    saveConfig();
                }
                if (callback != null) {
                    callback.call(throwable);
                }
            }
        });
    }

    private void clearLoginData() {
        config.setToken(null);
        config.setPassword(null);
        saveConfig();
    }

    /**
     * 执行API请求方法。实现自动重新登录功能。（如果已经缓存账户密码：config.getAutoLogin()==true）
     *
     * @param type
     * @param request
     * @param callback
     * @param <T>
     */
    public <T> void executeApiRequest(final Type type, final Request request, final Action1<ApiCallback<T>> callback) {
        executeRequest(type, request, new Action2<ApiCallback<T>, Throwable>() {
            @Override
            public void call(ApiCallback<T> httpCallback, final Throwable throwable) {
                if (callback == null) return;
                if (httpCallback != null && throwable == null) {
                    callback.call(httpCallback);
                } else {
                    String message = throwable.getMessage();
                    if (message.equals(UNAUTHORIZED_MARK)) {
                        final String user = config.getUser();
                        final String password = config.getPassword();
                        if (config.getAutoLogin() && user != null && password != null) {
                            trace("正在重新登录...");
                            clearLoginData();
                            logon(user, password, new Action1<Throwable>() {
                                @Override
                                public void call(final Throwable throwable1) {
                                    executeApiRequest(type, request, callback);
                                }
                            });
                        } else {
                            message = "未登录";
                        }
                    }
                    final String output = message;
                    callback.call(new ApiCallback<T>() {
                        @Override
                        public boolean isError() {
                            return true;
                        }

                        @Override
                        public String getMessage() {
                            return output;
                        }
                    });
                }
            }
        });
    }

    public <T> void executeRequest(final Type type, final Request request, final Action2<T, Throwable> callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    trace("http executing : " + request);
                    final Response response = httpclient.newCall(request).execute();
                    final String json = response.body().string();
                    trace("http execute result : " + json);
                    if (callback != null) {
                        callback.call((T) GSON.fromJson(json, type), null);
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                    if (callback != null)
                        callback.call(null, e);
                }
            }
        }).start();
    }

    public <T> void executeRequest(final Class<T> cls, final Request request, final Action2<T, Throwable> callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    trace("http executing : " + request);
                    final Response response = httpclient.newCall(request).execute();
                    final String json = response.body().string();
                    trace("http execute result : " + json);
                    if (callback != null) {
                        callback.call(GSON.fromJson(json, cls), null);
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                    if (callback != null)
                        callback.call(null, e);
                }
            }
        }).start();
    }

    public void logoff(final Action1<Throwable> callback) {
        final AccessToken token = config.getToken();
        if (token == null) {
            if (callback != null) callback.call(new Throwable("未登录"));
        } else {
            final Request request = new Request.Builder()
                    .url(getLogoffUrl())
                    .post(RequestBody.create(MEDIA_TYPE_URLENCODED, getLogoffContent(token.getAccessToken())))
                    .build();
            executeRequest(Object.class, request, new Action2<Object, Throwable>() {
                @Override
                public void call(Object o, Throwable throwable) {
                    if (throwable == null) {
                        clearLoginData();
                    }
                    if (callback != null) {
                        callback.call(throwable);
                    }
                }
            });
        }
    }

    /**
     * 创建API访问构造器，注：需要登录后才能创建。
     *
     * @param url 接口url，参考Constants.ServerUrl类。
     * @return
     */
    public Request.Builder createApiRequestBuilder(String url) throws AuthenticatorException {
        final AccessToken token = config.getToken();
        if (!isLogin()) {
            String message = "未登录，无法访问API";
            trace(message);
            throw new AuthenticatorException(message);
        }
        return new Request.Builder()
                .addHeader("Authorization", token.getTokenType() + " " + token.getAccessToken())
                .url(Constants.HTTPS_SERVER.concat(url));
    }

    /**
     * 执行同步路由器任务。
     * 执行算法：
     * 1、下载云端路由器配置列表；
     * 2、将路由器配置项ID，对应赋值到RouterManager列表中Router.Source中。
     * 3、遍历RouterManager路由器列表，将为获取到Source的路由器配置上传至云端。
     *
     * @param callback 执行结果回调。
     */
    public void syncRouterList(final Action1<Throwable> callback) {
        try {
            trace("开始同步...");
            final Request request = createApiRequestBuilder(Constants.ServerUrl.ROUTER_CONFIGURATION_QUERY_PAGING)
                    .post(RequestBody.create(MEDIA_TYPE_JSON, GSON.toJson(new PagingQuery(1, 100)))).build();
            executeApiRequest(new TypeToken<ApiCallback<PageResult<RouterCloudData>>>() {
            }.getType(), request, new Action1<ApiCallback<PageResult<RouterCloudData>>>() {
                @Override
                public void call(ApiCallback<PageResult<RouterCloudData>> apiCallback) {
                    if (apiCallback.isError()) {
                        if (callback != null) {
                            callback.call(new Exception(apiCallback.getMessage()));
                        }
                    } else {
                        final RouterCloudData[] result = apiCallback.getData().getResult();
                        if (result != null) {
                            for (RouterCloudData routerCloudData : result) {
                                final Router router = mRouterManager.getRouter(routerCloudData.getConnectionKey());
                                if (router != null) {
                                    router.setSource(routerCloudData.getID());
                                } else {
                                    mRouterManager.addRouter(new Router(routerCloudData.getID(), routerCloudData.getName(), routerCloudData.getConnectionKey()));
                                }
                            }
                        }
                        trace("开始上传...");
                        tryUploadRouterList(callback);
                    }
                }
            });
        } catch (AuthenticatorException e) {
            e.printStackTrace();
            if (callback != null)
                callback.call(e);
        }
    }

    public void tryUploadRouterList(final Action1<Throwable> callback) {
        for (Router router : mRouterManager.getRouterList()) {
            if (router.getSource() == null) {
                uploadRouter(router, callback);
            }
        }
        if (callback != null) {
            callback.call(null);
        }
    }

}
