package difflib;

/**
 * Author : Yordanos Desta, on 5/10/17.
 *
 * An Interface that will be called by {@link DifferentialEntityAnalyzerAsync}, when the operation completes.
 */
public interface IDifferentiableCallback {

    public void onSuccess(AsyncDiffResult asyncDiffResult);

    public void onError(DifferentialException exception);
}
