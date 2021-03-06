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

package com.analog.lyric.dimple.FactorFunctions.core;

import java.util.Random;

public abstract class HybridSampledBPFactorFunction extends FactorFunction
{
	protected Random _random;
	
	public HybridSampledBPFactorFunction() 
	{
		super();
	}
	public HybridSampledBPFactorFunction(String name) 
	{
		super(name);
	}
	
	public void attachRandom(Random random)
	{
		_random = random;
	}

	public abstract double acceptanceRatio(int outPortIndex, Object ... inputs);
	public abstract Object generateSample(int outPortIndex, Object ... inputs);
}
