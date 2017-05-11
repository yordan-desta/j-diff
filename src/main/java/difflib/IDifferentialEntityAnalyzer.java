package difflib;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * Author : Yordanos Desta, on 5/10/17.
 */
public interface IDifferentialEntityAnalyzer {

    HashMap<Field, Object> runDifferential();

    HashMap<Field, Object> getDifferenceValues();

    void setDifferentiableLevel(DifferentiableLevel differentiableLevel);

    boolean hasDifference();

    String getPrettyJson();

    void setMaxDepthCount(int maxDepthCount);
}
