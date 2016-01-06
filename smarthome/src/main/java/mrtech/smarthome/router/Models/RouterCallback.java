package mrtech.smarthome.router.Models;


import mrtech.smarthome.router.Router;

/**
 * 路由器回调。
 *
 * @param <E> 回调数据结构
 */
public interface RouterCallback<E> {
    Router getRouter();

    E getData();
}
