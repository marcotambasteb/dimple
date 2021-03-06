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

package com.analog.lyric.dimple.solvers.gaussian;

import java.util.Arrays;
import com.analog.lyric.dimple.model.RealJointDomain;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.solvers.core.SVariableBase;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;

public class MultivariateVariable extends SVariableBase 
{

	private int _numVars;
	private MultivariateMsg _input;
	private MultivariateMsg [] _outputMsgs = new MultivariateMsg[0];
	private MultivariateMsg [] _inputMsgs = new MultivariateMsg[0];
	
	public MultivariateVariable(VariableBase var) 
	{
		super(var);
		
		_numVars = ((RealJointDomain)_var.getDomain()).getNumVars();
	}
	
	@Override
	public void setInputOrFixedValue(Object input,Object fixedValue, boolean hasFixedValue)
	{
		if (input == null)
			_input = (MultivariateMsg) createDefaultMessage();
		else
			_input = (MultivariateMsg)input;
	}

	
	//TODO: remove this when I fix it for real.
	public void moveInputs(ISolverVariable other)
	{
		_input = ((MultivariateVariable)other)._input;
		
	}
	

	@Override
	public Object getBelief()  
	{
		MultivariateMsg m = new MultivariateMsg(_input.getMeans(), _input.getCovariance());		
		doUpdate(m,-1);
		return m;
	}

	@Override
	public void updateEdge(int outPortNum)  
	{
		MultivariateMsg outMsg = _outputMsgs[outPortNum];
		doUpdate(outMsg,outPortNum);
	}

	private void doUpdate(MultivariateMsg outMsg,int outPortNum) 
	{
		double [] vector = _input.getInformationVector();		
		double [][] matrix = _input.getInformationMatrix();		
		
		//MultivariateMsg outMsg = (MultivariateMsg)_var.getPorts().get(outPortNum).getOutputMsg();
		
		for (int i = 0; i < _outputMsgs.length; i++ )
		{
			if (i != outPortNum)
			{				
				//_var.getInputObject();
				MultivariateMsg inMsg = _inputMsgs[i];
				
				double [] inMsgVector = inMsg.getInformationVector();
				
				for (int j = 0; j < vector.length; j++)
				{
					vector[j] += inMsgVector[j];
				}
				
				double [][] inMsgMatrix = inMsg.getInformationMatrix();
				
				for (int j = 0; j < inMsgMatrix.length; j++)
				{
					for (int k = 0; k < inMsgMatrix[j].length; k++)
					{
						matrix[j][k] += inMsgMatrix[j][k];
					}
				}
			}
		}
		
		outMsg.setInformation(vector,matrix);
	}

	@Override
	public Object [] createMessages(ISolverFactor factor) 
	{
		int portNum = getModelObject().getPortNum(factor.getModelObject());
		int arrayLength = Math.max(_inputMsgs.length, portNum+1);
		_inputMsgs = Arrays.copyOf(_inputMsgs, arrayLength);
		_inputMsgs[portNum] = (MultivariateMsg)createDefaultMessage();
		_outputMsgs = Arrays.copyOf(_outputMsgs,arrayLength);
		_outputMsgs[portNum] = (MultivariateMsg)createDefaultMessage();
		return new Object [] {_inputMsgs[portNum],_outputMsgs[portNum]};
	}

	public MultivariateMsg createDefaultMessage() 
	{
		MultivariateMsg mm = new MultivariateMsg();
		return (MultivariateMsg)resetInputMessage(mm);
	}

	@Override
	public Object resetInputMessage(Object message) 
	{
		double [] means = new double[_numVars];
		double [][] covariance = new double[_numVars][];
		
		
		for (int i = 0; i < covariance.length; i++)
		{
			covariance[i] = new double[_numVars];
			covariance[i][i] = Double.POSITIVE_INFINITY;
		}
		((MultivariateMsg)message).setMeanAndCovariance(means, covariance);
		return message;
	}

	@Override
	public void resetEdgeMessages( int i ) 
	{
		_inputMsgs[i] = (MultivariateMsg)resetInputMessage(_inputMsgs[i]);
		_outputMsgs[i] = (MultivariateMsg)resetOutputMessage(_outputMsgs[i]);		
	}
	
	@Override
	public void moveMessages(ISolverNode other, int portNum, int otherPort) 
	{
		MultivariateVariable s = (MultivariateVariable)other;
	
		_inputMsgs[portNum] = s._inputMsgs[otherPort];
		_outputMsgs[portNum] = s._outputMsgs[otherPort];

	}
	
	@Override
	public Object getInputMsg(int portIndex) 
	{
		return _inputMsgs[portIndex];
	}

	@Override
	public Object getOutputMsg(int portIndex) {
		return _outputMsgs[portIndex];
	}
	@Override
	public void setInputMsg(int portIndex, Object obj) {
		_inputMsgs[portIndex] = (MultivariateMsg)obj;
	}
}
