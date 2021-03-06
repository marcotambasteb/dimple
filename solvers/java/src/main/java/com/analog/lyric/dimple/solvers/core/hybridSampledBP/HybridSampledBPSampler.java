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

package com.analog.lyric.dimple.solvers.core.hybridSampledBP;

import java.util.Random;

import com.analog.lyric.dimple.model.Port;

public abstract class HybridSampledBPSampler 
{
	protected Random _random;
	protected Port _p;
	
	public HybridSampledBPSampler(Port p,Random random)
	{
		_p = p;
		_random = random;
	}
	/*
	public void setRandom(Random random)
	{
		_random = random;
	}
	*/
	
	/*
	public void attach(Port p,Random random)
	{
		_p = p;
		_random = random;
	}
	*/
	
	public abstract void initialize() ;
	public abstract void createMessage(Object msg);
	public abstract Object generateSample();
	public abstract Object getInputMsg();
	public abstract void moveMessages(HybridSampledBPSampler other);
}
