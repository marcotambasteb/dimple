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

package com.analog.lyric.dimple.solvers.core;

import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.INode;
import com.analog.lyric.dimple.model.Port;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.model.repeated.BlastFromThePastFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverBlastFromThePastFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;

public class SBlastFromThePast implements ISolverBlastFromThePastFactor
{
	private BlastFromThePastFactor _factor;
	protected Port _portForOtherVar;
	protected Port _portForBlastVar;
	
	public SBlastFromThePast(BlastFromThePastFactor f)
	{
		_factor = f;
	}
	
	public BlastFromThePastFactor getFactor()
	{
		return _factor;
	}
	
	public Port getOtherVariablePort()
	{
		return _portForOtherVar;
	}
	
	@Override
	public Object getBelief() 
	{
		throw new DimpleException("Not implemented");
	}

	@Override
	public double getInternalEnergy() 
	{
		throw new DimpleException("Not implemented");
	}

	@Override
	public double getBetheEntropy() 
	{
		throw new DimpleException("Not implemented");
	}

	@Override
	public void createMessages()
	{
		throw new DimpleException("not supported");
	}

	@Override
	public void createMessages(VariableBase varConnectedToBlast, Port portForOtherVar)
	{
		
	    for (int index = 0; index < _factor.getVariables().size(); index++)
	    {
	    	ISolverVariable is = _factor.getVariables().getByIndex(index).getSolver();
	    	is.createMessages(this);	    	
	    }
	    
	    _portForOtherVar = portForOtherVar;
	    _portForBlastVar = new Port(varConnectedToBlast,varConnectedToBlast.getPortNum(getModelObject()));
	}

	@Override
	public void moveMessages(ISolverNode other) 
	{
		throw new DimpleException("Not implemented");
	}

	@Override
	public int[][] getPossibleBeliefIndices() 
	{
		throw new DimpleException("Not implemented");
	}

	@Override
	public void update() 
	{
	}

	@Override
	public void updateEdge(int outPortNum) 
	{
	}

	@Override
	public void initialize() 
	{
	}

	@Override
	public void resetEdgeMessages(int portNum) 
	{
		throw new DimpleException("Not implemented");
	}

	@Override
	public ISolverFactorGraph getParentGraph() 
	{
		throw new DimpleException("Not implemented");
	}

	@Override
	public ISolverFactorGraph getRootGraph() 
	{
		throw new DimpleException("Not implemented");
	}

	@Override
	public double getScore() 
	{
		throw new DimpleException("Not implemented");
	}

	@Override
	public INode getModelObject() 
	{
		return _factor;
	}

	@Override
	public Object getInputMsg(int portIndex) 
	{
		throw new DimpleException("Not implemented");
	}

	@Override
	public Object getOutputMsg(int portIndex) 
	{
		throw new DimpleException("Not implemented");
	}

	@Override
	public void setInputMsg(int portIndex, Object obj) 
	{
		throw new DimpleException("Not implemented");
	}

	@Override
	public void setOutputMsg(int portIndex, Object obj) 
	{
		VariableBase var = _factor.getVariables().getByIndex(portIndex);
		int index = var.getPortNum(getModelObject());
		var.getSolver().setInputMsg(index,obj);		
	}

	@Override
	public void setInputMsgValues(int portIndex, Object obj) 
	{
		throw new DimpleException("Not implemented");
	}

	@Override
	public void setOutputMsgValues(int portIndex, Object obj) 
	{
		throw new DimpleException("Not implemented");
	}

	@Override
	public void moveMessages(ISolverNode other, int thisPortNum,
			int otherPortNum) 
	{
		throw new DimpleException("Not implemented");
	}

	@Override
	public void advance() 
	{
		_portForBlastVar.node.getSolver().moveMessages(_portForOtherVar.node.getSolver(), 
				_portForBlastVar.index,_portForOtherVar.index);
	}

	@Override
	public void setDirectedTo(int[] indices) 
	{
		//NOP
	}

	

}
