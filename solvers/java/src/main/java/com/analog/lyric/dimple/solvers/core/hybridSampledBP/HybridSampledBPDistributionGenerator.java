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

import java.util.ArrayList;

import com.analog.lyric.dimple.model.Port;

public abstract class HybridSampledBPDistributionGenerator 
{
	protected Port _p;
	//protected Random _random;
	
	public HybridSampledBPDistributionGenerator(Port p)
	{
		_p = p;
	}
	
	public abstract void initialize();
	public abstract void createMessage(Object msg);
	public abstract void generateDistributionInPlace(ArrayList<Object> input);
	public abstract void setOutputMsg(Object message);
	public abstract Object getOutputMsg();
	public abstract void moveMessages(HybridSampledBPDistributionGenerator other);

}
