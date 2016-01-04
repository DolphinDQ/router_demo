package mrtech.smarthome.auth.Models;

/**
 * Created by sphynx on 2015/12/29.
 */
public class AccessToken {
    public String getAccessToken() {
        return access_token;
    }

    public int getExpiresIn() {
        return expires_in;
    }

    public String getTokenType() {
        return token_type;
    }

    private String access_token;
    private int expires_in;
    private String token_type;

}
