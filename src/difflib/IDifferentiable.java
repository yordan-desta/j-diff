package difflib;

/**
 * Author: Yordanos Desta, on 5/9/17.
 */

public interface IDifferentiable {
    /**
     * required by any differentiable entity to be implemented
     * @param o object to be compared with
     * @return boolean
     */
    boolean equals(Object o);

    String getClazzName();
}
