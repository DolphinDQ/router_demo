package mrtech.smarthome.router.Models;


import mrtech.smarthome.router.Router;

/**
 * 路由器回调
 * @param <E> 回调数据结构
 */
public interface RouterCallback<E> {
    /**
     * 获取路由器对象
     * @return 路由器对象
     */
    Router getRouter();

    /**
     * 获取回调数据
     * @return 回调数据
     */
    E getData();
}
