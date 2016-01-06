package mrtech.smarthome.auth.Models;

/**
 * Created by sphynx on 2015/12/31.
 */
public class PageResult<E> {
    public E[] getResult() {
        return Result;
    }

    private E[] Result;

    public int getTotal() {
        return Total;
    }

    private int Total;
}
