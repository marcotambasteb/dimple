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

package com.analog.lyric.dimple.solvers.core.proposalKernels;

import com.analog.lyric.dimple.solvers.core.SolverRandomGenerator;

public class CircularNormalProposalKernel implements IProposalKernel
{
	protected double _standardDeviation = 1;
	protected double _min = -Math.PI;
	protected double _max = Math.PI;
	protected double _range = _max-_min;

	public Proposal next(Object currentValue)
	{
		double value = (Double)currentValue + _standardDeviation * SolverRandomGenerator.rand.nextGaussian();
		value = ((((value - _min) % _range) + _range) % _range) + _min;		// Wrap from -pi to pi
		return new Proposal(value);
	}
	
	public void setParameters(Object... parameters)
	{
		_standardDeviation = (Double)parameters[0];
		if (parameters.length > 1)
			_min = (Double)parameters[1];
		if (parameters.length > 2)
			_max = (Double)parameters[2];
		_range = _max-_min;
	}
	
	public Object[] getParameters()
	{
		Object[] parameters = new Object[3];
		parameters[0] = _standardDeviation;
		parameters[1] = _min;
		parameters[2] = _max;
		return parameters;
	}
}

