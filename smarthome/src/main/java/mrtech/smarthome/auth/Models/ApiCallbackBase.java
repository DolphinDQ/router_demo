package mrtech.smarthome.auth.Models;

/**
 * Created by sphynx on 2015/12/29.
 */
public class ApiCallbackBase {
    private boolean IsError;
    private String Message;

    public boolean isError() {
        return IsError ;
    }

    public String getMessage() {
        return Message;
    }
}
