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


public class LinearEquation extends FactorFunction
{
	protected double _beta;
	protected double[] _constant;
	public LinearEquation(double[] constant) {this(constant,1);}
	public LinearEquation(double[] constant, double smoothing) {super("LinearEquation"); _beta = 1/smoothing; _constant=constant;}
	
    @Override
    public double evalEnergy(Object ... input)
    {
    	int length = input.length;
    	double out = (Double)input[0];
    	
    	double sum= 1;
    	for (int i = 1; i < length; i++)
    		sum += _constant[i-1]*(Double)input[i];


    	double diff = sum - out;
    	double potential = diff*diff;
    	
    	return potential*_beta;
    }
}
