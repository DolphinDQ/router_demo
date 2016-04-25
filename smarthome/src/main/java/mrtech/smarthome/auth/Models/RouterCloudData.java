package mrtech.smarthome.auth.Models;

import java.util.Date;

/**
 * 路由器云数据.
 */
public class RouterCloudData {

    private int ID;
    private String Name;
    private String ConnectionKey;
    private Date LastConfigurationBackup;
    private boolean AutoBackup;

    /**
     * 路由器云数据
     */
    public RouterCloudData(){}

    /**
     * 路由器云数据
     * @param name 路由器名称
     * @param connectionKey 连接键
     */
    public RouterCloudData(String name,String connectionKey){
        Name=name;
        ConnectionKey=connectionKey;
    }

    /**
     * 获取路由器连接键
     * @return 连接键
     */
    public String getConnectionKey() {
        return ConnectionKey;
    }

    /**
     * 获取路由器名称
     * @return 路由器名称
     */
    public String getName() {
        return Name;
    }

    /**
     * 获取路由器ID
     * @return 路由器ID
     */
    public int getID() {
        return ID;
    }
}
