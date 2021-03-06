/*******************************************************************************
*   Copyright 2013 Analog Devices, Inc.
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
********************************************************************************/

package com.analog.lyric.dimple.FactorFunctions;

import com.analog.lyric.dimple.FactorFunctions.core.FactorFunction;
import com.analog.lyric.dimple.FactorFunctions.core.FactorFunctionUtilities;


/**
 * Deterministic equals function. This is a deterministic directed factor.
 * 
 * The variables are ordered as follows in the argument list:
 * 
 * 1) Output (Logical output = all other values equal)
 * 2...) Arbitrary length list of values (double or integer)
 * 
 */
public class Equals extends FactorFunction 
{
    @Override
    public double evalEnergy(Object... arguments)
    {
    	int index = 0;
    	boolean indicator = FactorFunctionUtilities.toBoolean(arguments[index++]);
    	
    	if (arguments.length <= 2)										// One value, must be equal
        	return indicator ? 0 : Double.POSITIVE_INFINITY;
    	
    	boolean allEqual = true;
    	double firstVal = FactorFunctionUtilities.toDouble(arguments[index++]);
    	for (; index < arguments.length; index++)
    		if (FactorFunctionUtilities.toDouble(arguments[index]) != firstVal)
    			allEqual = false;
    	
    	return (indicator == allEqual) ? 0 : Double.POSITIVE_INFINITY;
    }

    
    @Override
    public final boolean isDirected()	{return true;}
    @Override
	public final int[] getDirectedToIndices() {return new int[]{0};}
    @Override
	public final boolean isDeterministicDirected() {return true;}
    @Override
	public final void evalDeterministicFunction(Object... arguments)
    {
    	if (arguments.length <= 2)										// One value, must be equal
    	{
        	arguments[0] = FactorFunctionUtilities.toDouble(true);		// Replace the output value
        	return;
    	}
    		
    	int index = 1;
    	boolean allEqual = true;
    	double firstVal = FactorFunctionUtilities.toDouble(arguments[index++]);
    	for (; index < arguments.length; index++)
    		if (FactorFunctionUtilities.toDouble(arguments[index]) != firstVal)
    			allEqual = false;
    	
    	arguments[0] = FactorFunctionUtilities.toDouble(allEqual);		// Replace the output value
    }
}
