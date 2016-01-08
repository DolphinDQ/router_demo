package mrtech.smarthome.router;


import com.orm.SugarRecord;
import com.orm.dsl.Table;

import mrtech.smarthome.router.Models.DataEntityBase;

/**
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

    public String getSn() {
        return sn;
    }

    public String getName() {
        return name;
    }

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