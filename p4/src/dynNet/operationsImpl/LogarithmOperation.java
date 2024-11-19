package dynNet.operationsImpl;

import dynNet.dynCalculator.Operation;

/**
 * Class [LogarithmOperation]
 * <p>
 * This is a concrete operation class, that implements the
 * interface <code>Operation</code>.
 *
 * 
 */
public class LogarithmOperation implements Operation {

    public float calculate(float firstNumber, float secondNumber) {
        return (float) (Math.log(firstNumber) / Math.log(secondNumber));
    }
}
