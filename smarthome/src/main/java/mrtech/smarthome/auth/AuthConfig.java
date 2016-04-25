package mrtech.smarthome.auth;

import mrtech.smarthome.auth.Models.AccessToken;

/**
 * 验证模块配置
 */
public class AuthConfig {

    private boolean autoLogin;
    private long loginTime;
    private String user;
    private String password;
    private AccessToken token;

    /**
     * 获取自动登录状态
     * @return 登录状态
     */
    public boolean getAutoLogin() {
        return autoLogin;
    }

    /**
     * 设置自动登录，设置为true时，AccessToken会自动续期
     * @param autoLogin 登录状态
     */
    public void setAutoLogin(boolean autoLogin) {
        this.autoLogin = autoLogin;
    }

    /**
     * 获取登录时间
     * @return 登录时间
     */
    public long getLoginTime() {
        return loginTime;
    }

    /**
     * 设置登录时间
     * @param loginTime 登录时间
     */
    public void setLoginTime(long loginTime) {
        this.loginTime = loginTime;
    }

    /**
     * 获取登录账户
     * @return 登录账户
     */
    public String getUser() {
        return user;
    }

    /**
     * 设置登录账户
     * @param user 登录账户
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * 获取登录密码
     * @return 登录密码
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置登录密码
     * @param password 登录密码
     */
    void setPassword(String password) {
        this.password = password;
    }

    /**
     * 获取访问令牌
     * @return 访问令牌
     */
    public AccessToken getToken() {
        return token;
    }

    /**
     * 设置访问令牌
     * @param token 访问令牌
     */
    public void setToken(AccessToken token) {
        this.token = token;
    }
}
