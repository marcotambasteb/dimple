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

package com.analog.lyric.dimple.model;

import java.util.Arrays;

public class RealJointDomain extends Domain 
{
	private RealDomain [] _domains;
	
	public RealJointDomain(int size)
	{
		_domains = new RealDomain[size];
		
		for (int i = 0; i < size; i++)
			_domains[i] = new RealDomain();
	}
	
	public RealJointDomain(RealDomain ... domains)
	{
		_domains = domains.clone();
	}
	
	@Override
	public boolean isDiscrete() 
	{
		return false;
	}
	
	public RealDomain [] getRealDomains()
	{
		return _domains;
	}
	
	public int getNumVars()
	{
		return _domains.length;
	}
	
	public boolean isJoint()
	{
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(this._domains);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof RealJointDomain))
			return false;

		RealJointDomain other = (RealJointDomain) obj;
		if (!Arrays.equals(this._domains, other._domains))
			return false;
		return true;
	}
}
