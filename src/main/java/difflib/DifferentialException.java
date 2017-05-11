package difflib;

/**
 * Author: Yordanos Desta, on 5/2/17.
 */

class DifferentialException extends RuntimeException {

    private static final long serialVersionUID = 2544587;

    private final int code;

    public static final int NULL_ENTITY_EXCEPTION = 2001;

    public static final int TYPE_MISMATCH_EXCEPTION = 2002;

    public static final int NON_DIFFERENTIABLE_CLASS_EXCEPTION = 2003;

    public static final int RUNTIME_ERROR= 2004;

    DifferentialException(String detailMessage, int code, Throwable throwable) {
        super(detailMessage, throwable);

        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
