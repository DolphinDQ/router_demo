package mrtech.smarthome.auth.Models;

/**
 * 访问令牌
 */
public class AccessToken {

    private String access_token;
    private int expires_in;
    private String token_type;

    /**
     * 获取访问令牌
     * @return 访问令牌
     */
    public String getAccessToken() {
        return access_token;
    }

    /**
     * 获取令牌过期时间，单位为秒
     * @return 过期时间
     */
    public int getExpiresIn() {
        return expires_in;
    }

    /**
     * 获取令牌类型
     * @return 令牌类型
     */
    public String getTokenType() {
        return token_type;
    }
}
