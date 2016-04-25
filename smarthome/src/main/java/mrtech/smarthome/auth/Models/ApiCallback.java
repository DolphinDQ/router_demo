package mrtech.smarthome.auth.Models;

/**
 * 接口回调
 */
public class ApiCallback<T> extends ApiCallbackBase {

    private T Data;

    /**
     * 获取数据
     * @return 数据
     */
    public T getData() {
        return Data;
    }
}
