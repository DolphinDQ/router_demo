package mrtech.smarthome.auth.Models;

/**
 * 分页结果
 */
public class PageResult<E> {

    private E[] Result;
    private int Total;

    /**
     * 获取当前页数据结果
     * @return 当前页数据结果
     */
    public E[] getResult() {
        return Result;
    }

    /**
     * 获取数据库数据总条数
     * @return 数据库数据总条数
     */
    public int getTotal() {
        return Total;
    }
}
