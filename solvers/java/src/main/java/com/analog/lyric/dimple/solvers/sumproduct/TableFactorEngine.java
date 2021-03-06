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

package com.analog.lyric.dimple.solvers.sumproduct;

import java.util.ArrayList;

import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.INode;

/*
 * Provides the update and updateEdge logic for sumproduct
 */
public class TableFactorEngine 
{
	STableFactor _tableFactor;
	Factor _factor;
	
	public TableFactorEngine(STableFactor tableFactor)
	{
		_tableFactor = tableFactor;
		_factor = _tableFactor.getFactor();
	}
	
	public void updateEdge(int outPortNum) 
	{
		ArrayList<INode> siblings = _factor.getSiblings();
	    int[][] table = _tableFactor.getFactorTable().getIndices();
	    double[] values = _tableFactor.getFactorTable().getWeights();
	    int tableLength = table.length;
	    int numPorts = siblings.size();
	    
        double[] outputMsgs = _tableFactor.getOutPortMsgs()[outPortNum];
        double [][] inputMsgs = _tableFactor.getInPortMsgs();
        
        if (_tableFactor._dampingInUse)
        {
        	double damping = _tableFactor._dampingParams[outPortNum];
        	if (damping != 0)
        	{
        		double[] saved = _tableFactor._savedOutMsgArray[outPortNum];
        		for (int i = 0; i < outputMsgs.length; i++)
        			saved[i] = outputMsgs[i];
        	}
        }
        
    	int outputMsgLength = outputMsgs.length;
        for (int i = 0; i < outputMsgLength; i++) outputMsgs[i] = 0;
                
        
        for (int tableIndex = 0; tableIndex < tableLength; tableIndex++)
        {
        	double prob = values[tableIndex];
        	int[] tableRow = table[tableIndex];
    		int outputIndex = tableRow[outPortNum];
        	
    		
        	for (int inPortNum = 0; inPortNum < numPorts; inPortNum++)
        		if (inPortNum != outPortNum)
        			prob *= inputMsgs[inPortNum][tableRow[inPortNum]];
        	
        	outputMsgs[outputIndex] += prob;
        }
        
    	double sum = 0; 
    	for (int i = 0; i < outputMsgLength; i++) sum += outputMsgs[i];
		if (sum == 0)
		{
			throw new DimpleException("UpdateEdge failed in SumProduct Solver.  All probabilities were zero when calculating message for port " 
					+ outPortNum + " on factor " + _factor.getLabel());
		}

		for (int i = 0; i < outputMsgLength; i++) 
			outputMsgs[i] /= sum;
    	
		if (_tableFactor._dampingInUse)
		{
			double damping = _tableFactor._dampingParams[outPortNum];
			if (damping != 0)
			{
				double[] saved = _tableFactor._savedOutMsgArray[outPortNum];
				for (int i = 0; i < outputMsgLength; i++)
					outputMsgs[i] = (1-damping)*outputMsgs[i] + damping*saved[i];
			}
		}
    	
	}
	

	
	
	public void update() 
	{				

		
		ArrayList<INode> ports = _factor.getSiblings();
	    int[][] table = _tableFactor.getFactorTable().getIndices();
	    double[] values = _tableFactor.getFactorTable().getWeights();
	    int tableLength = table.length;
	    int numPorts = ports.size();
	    
	    
	    double [][] outMsgs = _tableFactor.getOutPortMsgs();
	    double [][] inMsgs = _tableFactor.getInPortMsgs();
	    
	    for (int outPortNum = 0; outPortNum < numPorts; outPortNum++)
	    {
	    	double[] outputMsgs = outMsgs[outPortNum];
	    		    	
	    	if (_tableFactor._dampingInUse)
	    	{
	    		double damping = _tableFactor._dampingParams[outPortNum];
	    		if (damping != 0)
	    		{
	    			double[] saved = _tableFactor._savedOutMsgArray[outPortNum];
	    			for (int i = 0; i < outputMsgs.length; i++)
	    				saved[i] = outputMsgs[i];
	    		}
	    	}
	    	
	    	int outputMsgLength = outputMsgs.length;
	    	for (int i = 0; i < outputMsgLength; i++) outputMsgs[i] = 0;

	    	for (int tableIndex = 0; tableIndex < tableLength; tableIndex++)
	    	{
	    		double prob = values[tableIndex];
	    		int[] tableRow = table[tableIndex];
	    		int outputIndex = tableRow[outPortNum];

	    		for (int inPortNum = 0; inPortNum < numPorts; inPortNum++)
	    			if (inPortNum != outPortNum)
	    			{	    				
	    				prob *= inMsgs[inPortNum][tableRow[inPortNum]];
	    			}
	    		outputMsgs[outputIndex] += prob;
	    	}

	    	double sum = 0; 
	    	for (int i = 0; i < outputMsgLength; i++) sum += outputMsgs[i];
    		if (sum == 0)
    		{
    			throw new DimpleException("Update failed in SumProduct Solver.  All probabilities were zero when calculating message for port " 
    					+ outPortNum + " on factor " +_factor.getLabel());
    		}

	    	for (int i = 0; i < outputMsgLength; i++) 
	    		outputMsgs[i] /= sum;
	    	
	    	if (_tableFactor._dampingInUse)
	    	{
	    		double damping = _tableFactor._dampingParams[outPortNum];
	    		if (damping != 0)
	    		{
	    			double[] saved = _tableFactor._savedOutMsgArray[outPortNum];
	    		for (int i = 0; i < outputMsgLength; i++)
	    			outputMsgs[i] = (1-damping)*outputMsgs[i] + damping*saved[i];
	    		}
	    	}
	    	
	    }
	}
}
