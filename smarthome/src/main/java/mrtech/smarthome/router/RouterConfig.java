package mrtech.smarthome.router;


import com.orm.SugarRecord;
import com.orm.dsl.Table;

/**
 * Created by sphynx on 2015/12/23.
 */
@Table
public class RouterConfig extends  SugarRecord {
    private String sn;
    private long lastUpdateTime;
    public RouterConfig(){

    }
    public RouterConfig(String sn){
        this.sn=sn;
    }
    public String getSn() {
        return sn;
    }
    public long getLastUpdateTime() {
        return lastUpdateTime;
    }
    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }
}