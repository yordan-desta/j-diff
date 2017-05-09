package difflib;

/**
 * Author: Yordanos Desta, on 5/2/17.
 */

@Differentiable
public abstract class DifferentiableEntity implements IDifferentiable {

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

    public String getClazzName() {
        return clazzName;
    }

    @Override
    public boolean equals(Object o) {

        if(o != null && o instanceof DifferentiableEntity){

            return ((DifferentiableEntity) o).getUuid().equals(this.uuid);
        }

        return false;
    }
}
