package difflib;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * Author : Yordanos Desta, on 5/10/17.
 *
 * This is a result object that is returned by {@link DifferentialEntityAnalyzerAsync} successful callback {@link IDifferentiableCallback}
 */
public class AsyncDiffResult {

    private final HashMap<Field, Object> rawResult;

    private final String jsonResult;

    private final boolean hasDifference;


    AsyncDiffResult(HashMap<Field, Object> rawResult, String jsonResult, boolean hasDifference) {

        this.rawResult = rawResult;

        this.jsonResult = jsonResult;

        this.hasDifference = hasDifference;
    }

    public HashMap<Field, Object> getRawResult() {
        return rawResult;
    }

    public String getJsonResult() {
        return jsonResult;
    }

    public boolean isHasDifference() {
        return hasDifference;
    }
}
