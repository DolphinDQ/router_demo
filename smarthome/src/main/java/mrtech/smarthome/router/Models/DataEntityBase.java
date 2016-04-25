package mrtech.smarthome.router.Models;


import com.orm.dsl.Table;

/**
 * 数据实体基类
 */
@Table
public abstract class DataEntityBase {

    protected Long id ;

    /**
     * 获取实体ID
     * @return 实体ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置实体ID
     * @param id 实体ID
     */
    public void setId(long id) {
        this.id = id;
    }
}