package mrtech.smarthome.router;


import com.orm.annotation.Table;

import mrtech.smarthome.router.Models.DataEntityBase;

/**
 * 路由器配置对象
 * Created by sphynx on 2015/12/23.
 */
@Table
public class RouterConfig extends DataEntityBase {

    public RouterConfig() {
    }

    private String name;
    private String sn;
    private long lastUpdateTime;

    RouterConfig(String sn) {
        this.sn = sn;
    }

    /**
     * 获取路由器序列码
     * @return 序列码
     */
    public String getSn() {
        return sn;
    }

    /**
     * 获取路由器缓存的名称
     * @return 路由器名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获取路由器数据最后更新事件
     * @return 时间（毫秒）
     */
    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    void setName(String name) {
        this.name = name;
    }
}