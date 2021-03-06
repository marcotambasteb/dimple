/*******************************************************************************
*   Copyright 2012 Analog Devices, Inc.
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
 * Deterministic greater-than function. This is a deterministic directed factor.
 * 
 * The variables are ordered as follows in the argument list:
 * 
 * 1) Output (Logical output = FirstValue > SecondValue)
 * 2) FirstValue (double or integer)
 * 3) SecondValue (double or integer)
 * 
 */
public class GreaterThan extends FactorFunction 
{
    @Override
    public double evalEnergy(Object... arguments)
    {
    	boolean indicator = FactorFunctionUtilities.toBoolean(arguments[0]);
    	double firstVal = FactorFunctionUtilities.toDouble(arguments[1]);
    	double secondVal = FactorFunctionUtilities.toDouble(arguments[2]);
    	
    	if (indicator == (firstVal > secondVal))
    		return 0;
    	else
    		return Double.POSITIVE_INFINITY;
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
    	double firstVal = FactorFunctionUtilities.toDouble(arguments[1]);
    	double secondVal = FactorFunctionUtilities.toDouble(arguments[2]);
    	
    	arguments[0] = FactorFunctionUtilities.toDouble(firstVal > secondVal);		// Replace the output value
    }
}
