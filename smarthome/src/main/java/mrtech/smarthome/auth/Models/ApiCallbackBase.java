package mrtech.smarthome.auth.Models;

/**
 * 接口回调
 */
public class ApiCallbackBase {

    private boolean IsError;
    private String Message;

    /**
     * 判断是否有错误
     * @return boolean 判断结果
     */
    public boolean isError() {
        return IsError ;
    }

    /**
     * 获取提示信息
     * @return String 提示信息
     */
    public String getMessage() {
        return Message;
    }
}
