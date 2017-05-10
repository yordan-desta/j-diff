package difflib;

import com.sun.istack.internal.NotNull;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * Author : Yordanos Desta, on 5/10/17.
 *
 * This class performs a Differential operation on a {@link IDifferentiable} class asynchronously and returns {@link AsyncDiffResult} object
 * to the result
 */
public class DifferentialEntityAnalyzerAsync<C extends IDifferentiableCallback, T extends IDifferentiable>{

    private Runnable runnable = null;

    private Thread thread = null;

    private IDifferentialEntityAnalyzer differentialEntityAnalyzer;

    private static final Object syncLockObject = new Object();

    private final C context;

    public DifferentialEntityAnalyzerAsync(C context, @NotNull T oldEntity, @NotNull T newEntity, DifferentiableLevel differentiableLevel) {

        differentialEntityAnalyzer = new DifferentialEntityAnalyzer<T>(oldEntity, newEntity, differentiableLevel);

        this.context = context;
    }

    public DifferentialEntityAnalyzerAsync(C context, @NotNull T oldEntity, @NotNull T newEntity) {

        differentialEntityAnalyzer = new DifferentialEntityAnalyzer<T>(oldEntity, newEntity);

        this.context = context;

    }


    public void setDifferentiableLevel(DifferentiableLevel differentiableLevel) {

        differentialEntityAnalyzer.setDifferentiableLevel(differentiableLevel);
    }

    /**
     * starts running differential asynchronously. This method will call {@link IDifferentiableCallback} callbacks of the caller context/class
     */
    public void runDifferentialAsync() {

        if (runnable == null) {

            runnable = () -> {

                try {

                    HashMap<Field, Object> result = differentialEntityAnalyzer.runDifferential();

                    context.onSuccess(new AsyncDiffResult(result, differentialEntityAnalyzer.getPrettyJson(), differentialEntityAnalyzer.hasDifference()));

                } catch (Exception e) {

                    if (e instanceof DifferentialException)
                        context.onError((DifferentialException) e);

                    else
                        context.onError(new DifferentialException(e.getLocalizedMessage(), DifferentialException.RUNTIME_ERROR, e));

                } finally {

                    destroy();
                }
            };
        }

        if (thread == null || !thread.isAlive()) {

            thread = new Thread(runnable);

        } else return;

        thread.start();
    }

    /**
     * destroy the thread is any call was made
     */

    public void destroy(){

        if(thread != null){

            thread.interrupt();
            thread = null;
        }

        if(runnable != null){

            runnable = null;
        }
    }
}
