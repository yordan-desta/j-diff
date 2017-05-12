package difflib;

/**
 * Author: Yordanos Desta, on 5/8/17.
 */

public enum DifferentiableLevel {

    /**
     * This level wil track the differential for primitive types and Strings. for custom class objects and lists, set the new field as the diff result. it will not go deeper;
     */
    SHALLOW_UPDATE,

    /**
     * This level will check the differential for primitive types and Strings. It will ignore for other objects
     */
    SHALLOW_IGNORE,

    /**
     * what SHALLOW_UPDATE does plus it will give the diff for the fields of object fields. For example; if <br>
     * <code>class A{ <br>
     * private int someInt; <br>
     *
     * public void setSomeInt(int someInt){
     * } <br>
     * } <br>
     * Class B{ <br>
     * private A aInstance; <br>
     * }<br>
     * 
     * and we have two instances of B- b1 and b2; DEEP flag will check the difference in the aInstance fields between the two instances of B and returns the modified values in those aInstance fields of B;
     * </code>
     */
    DEEP;
}
