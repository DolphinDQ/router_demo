package mrtech.smarthome.auth.Models;

/**
 * Created by sphynx on 2015/12/29.
 */
public class ApiCallback<T> extends ApiCallbackBase {
    public T getData() {
        return Data;
    }

    private T Data;
}
