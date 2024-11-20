package dynNet.operationsImpl;
import dynNet.dynCalculator.Operation;
import java.lang.Math;

public class LogarithmOperation implements Operation
{
	public float calculate(float first, float base)
	{
		return Math.log(first) / Math.log(base); 
	}
}
