package mrtech.smarthome.auth.Models;

/**
 * Created by sphynx on 2016/1/4.
 */
public class PagingQuery<E> {
    public PagingQuery() {
    }

    public PagingQuery(Integer page, Integer size) {
        Page = page;
        Size = size;
    }

    private Integer Page;
    private Integer Size;
    private E Condition;

    public E getCondition() {
        return Condition;
    }

    public void setCondition(E condition) {
        Condition = condition;
    }
}
