package dynNet.operationsImpl;

import dynNet.dynCalculator.Operation;

/**
 * Class [RootOperation]
 * <p>
 * This is a concrete operation class, that implements the
 * interface <code>Operation</code>.
 *
 * 
 */
public class RootOperation implements Operation {

    public float calculate(float firstNumber, float secondNumber) {
        return (float) Math.pow(firstNumber, 1.0 / secondNumber);
    }
}
