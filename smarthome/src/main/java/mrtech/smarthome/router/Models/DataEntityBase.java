package mrtech.smarthome.router.Models;


import com.orm.dsl.Table;

/**
 * Created by sphynx on 2015/12/23.
 */
@Table
public abstract class DataEntityBase {
    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    protected Long id ;
}