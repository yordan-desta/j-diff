package difflib;

/**
 * Author: Yordanos Desta, on 5/2/17. <br>
 *
 * This class can help as a base class for other entities to extend from. By default evey class that extends from this class is considered
 *
 * differentiable. But you can use the {@link Differentiable} annotation to change its behaviour
 */

@Differentiable
public abstract class DifferentiableEntity implements IDifferentiableEntity {

    @Differentiable(ignoreDiff = true)
    private final String clazzName = this.getClass().getSimpleName();

    protected String uuid = clazzName;

    public String getUuid() {
        return uuid;
    }

    /**
     * override this method to have a unique identifier of your own, ignore otherwise. This property will set a definition for two objects to be comparable or not.
     *<br><br>
     *<b>Remark: </b> If you override this property, two objects with different uuid will not be analyzed and treated as two different objects. use this when you
     * want to narrow down analyzing diffs to the same object references (like different data for the same db object)
     * <br>
     * @param uuid unique universal identifier for the object
     */

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * returns the class name
     * @return class name
     */
    public String getClazzName() {
        return clazzName;
    }

    /**
     * This method determines whether the two objects are comparable or not. Two Differentiable POJOs should be equal inorder to be comparable;
     * By default, i.e unless the {@link #uuid} property is set by {@link #setUuid(String)} method two objects of the same class instance are
     * considered equal and eligible to differentiate.
     *
     * @param o object to be compared with
     * @return boolean - whether the should be comparable or not
     */
    @Override
    public boolean isEqual(Object o) {

        return o != null && o instanceof DifferentiableEntity && ((DifferentiableEntity) o).getUuid().equals(this.uuid);
    }
}
