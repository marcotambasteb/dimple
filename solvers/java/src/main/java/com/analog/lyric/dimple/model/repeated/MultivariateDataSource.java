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

package com.analog.lyric.dimple.model.repeated;


import java.util.LinkedList;

import com.analog.lyric.dimple.solvers.gaussian.MultivariateMsg;

public class MultivariateDataSource extends GenericDataSource<MultivariateMsg> 
{
	
	public MultivariateDataSource()
	{
		_data = new LinkedList<MultivariateMsg>();
	}
	
	public void add(double [] means, double [][] covar)
	{
		_data.add(new MultivariateMsg(means, covar));
	}
	
	
}
