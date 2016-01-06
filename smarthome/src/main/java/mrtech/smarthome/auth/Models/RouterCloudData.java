package mrtech.smarthome.auth.Models;

import java.util.Date;

/**
 * Created by sphynx on 2015/12/31.
 */
public class RouterCloudData {
    public RouterCloudData(){}
    public RouterCloudData(String name,String connectionKey){
        Name=name;
        ConnectionKey=connectionKey;
    }

    private int ID;
    private String Name;

    public String getConnectionKey() {
        return ConnectionKey;
    }

    public String getName() {
        return Name;
    }

    private String ConnectionKey;
    private Date LastConfigurationBackup;
    private boolean AutoBackup;


    public int getID() {
        return ID;
    }


}
