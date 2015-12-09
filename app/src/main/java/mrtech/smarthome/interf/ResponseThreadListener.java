/**
 * 
 */
package mrtech.smarthome.interf;

import mrtech.smarthome.rpc.Messages.Callback;

/**
 * 请求回调接口。
 * @author CJ
 * @date 2015年4月14日 上午12:39:48
 * @version 1.0
 */
public interface ResponseThreadListener
{
	/**
	 * 请求成功接口
	 * @param callback	服务器回传的callback对象。
	 */
	public void onRequestSuccess(Callback callback);
	
	/**
	 * 请求失败接口。
	 * @param message	错误信息
	 * @param throwable
	 */
	public void onRequestFailure(String message, Throwable throwable);
}