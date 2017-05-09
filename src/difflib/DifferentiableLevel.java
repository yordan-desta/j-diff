package difflib;

/**
 * Author: Yordanos Desta, on 5/8/17.
 */

public enum DifferentiableLevel {

    /**
     * This level wil track the differential for primitive types and Strings. for custom class objects and lists, it will not go deeper and set the new field as the diff result;
     */
    SHALLOW,

    /**
     * what SHALLOW does plus it will give the diff for the fields of object fields. For example; if <br>
     * <code>class A{ <br>
     * private int someInt; <br>
     * <p>
     * public void setSomeInt(int someInt){
     * <p>
     * } <br>
     * } <br>
     * Class B{ <br>
     * private A aInstance; <br>
     * }<br>
     * <p>
     * and we have two instances of B- b1 and b2; DEEP flag will check the difference in the aInstance fields between the two instances of B and returns the modified values in those aInstance fields of B;
     * <p>
     * </code>
     */
    DEEP;
}
