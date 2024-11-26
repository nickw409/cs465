package dynNet.operationsImpl;

import dynNet.dynCalculator.Operation;

public class RootOperation {
    public float calculate(float firstNumber, float secondNumber)
    {
        return (float)Math.pow(firstNumber, (1.0 / secondNumber));
 
