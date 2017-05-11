package difflib;

/**
 * Author: Yordanos Desta, on 5/9/17.
 * <p>
 * Every differentiable entity or java object must implement this interface.
 * <p>
 * <br><b>throws</b> - DifferentiableException
 * </p>
 */
public interface IDifferentiableEntity {
    /**
     * required by any differentiable entity to be implemented. This method determines whether they are
     *
     * @param object object to be compared with
     * @return boolean
     */
    boolean isEqual(Object object);
}
