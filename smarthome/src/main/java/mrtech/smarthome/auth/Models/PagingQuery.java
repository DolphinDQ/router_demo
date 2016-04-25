package mrtech.smarthome.auth.Models;

/**
 * 分页查询
 */
public class PagingQuery<E> {

    private Integer Page;
    private Integer Size;
    private E Condition;

    /**
     *  分页查询
     */
    public PagingQuery() {
    }

    /**
     * 分页查询
     * @param page 页码
     * @param size 分页大小
     */
    public PagingQuery(Integer page, Integer size) {
        Page = page;
        Size = size;
    }

    /**
     * 获取查询条件
     * @return 查询条件
     */
    public E getCondition() {
        return Condition;
    }

    /**
     * 设置查询条件
     * @param condition 查询条件
     */
    public void setCondition(E condition) {
        Condition = condition;
    }
}
