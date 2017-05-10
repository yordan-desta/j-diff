package difflib;

/**
 * Author: Yordanos Desta, on 5/9/17.
 */

public interface IDifferentiable {
    /**
     * required by any differentiable entity to be implemented
     * @param object object to be compared with
     * @return boolean
     */
    boolean isEqual(Object object);

    String getClazzName();
}
