package difflib;

/**
 * Author: Yordanos Desta, on 5/9/17.
 */

public interface IDifferentiable {
    /**
     * required by any differentiable entity to be implemented
     * @param o
     * @return
     */
    public boolean equals(Object o);

    public String getClazzName();
}
