package mrtech.models;

import android.view.View;

/**
 * 路由器列表项数据。
 * Created by sphynx on 2016/1/6.
 */
public class RouterListItemData {

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * 路由器是否被选中。
     */
    private boolean active;

}
