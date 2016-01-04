package mrtech.smarthome.auth;

import mrtech.smarthome.auth.Models.AccessToken;

/**
 * 验证模块配置。
 * Created by sphynx on 2015/12/30.
 */
public class AuthConfig {
    public boolean getAutoLogin() {
        return autoLogin;
    }

    public void setAutoLogin(boolean autoLogin) {
        this.autoLogin = autoLogin;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public AccessToken getToken() {
        return token;
    }

    public void setToken(AccessToken token) {
        this.token = token;
    }

    /**
     * 自动登录，设置为true时，AccessToken会自动续期。
     */
    private boolean autoLogin;
    /**
     * 登录时间，毫秒。
     */
    private long loginTime;
    /**
     * 登录账户。
     */
    private String user;
    /**
     * 登录密码。
     */
    private String password;
    /**
     * 登录令牌。
     */
    private AccessToken token;

    public long getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(long loginTime) {
        this.loginTime = loginTime;
    }
}
