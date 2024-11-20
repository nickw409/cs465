package dynNet.operationsImpl;
import dynNet.dynCalculator.Operation;
import java.lang.Math;

public class RootOperation implements Operation
{
	public float calculate(float first, float second)
	{
		return Math.pow(first, 1.0 / second);
	}
}
