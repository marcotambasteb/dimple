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

import java.util.Random;
import com.analog.lyric.dimple.FactorFunctions.core.FactorFunctionWithConstants;
import com.analog.lyric.dimple.FactorFunctions.core.FactorTable;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.solvers.core.ParameterEstimator;
import com.analog.lyric.dimple.solvers.core.SFactorGraphBase;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;

public class SFactorGraph extends SFactorGraphBase
{
	private double _damping = 0;
	private FactorTable _currentFactorTable = null;

	public SFactorGraph(com.analog.lyric.dimple.model.FactorGraph factorGraph) 
	{
		super(factorGraph);
		
	}

	public ISolverFactor createCustomFactor(com.analog.lyric.dimple.model.Factor factor)  
	{
		String funcName = factor.getFactorFunction().getName();
		if (funcName.equals("finiteFieldMult"))
		{
			//VariableList variables = factor.getVariables();
			
			if (factor.getFactorFunction() instanceof FactorFunctionWithConstants)
				return new FiniteFieldConstMult(factor);
			else
				return new FiniteFieldMult(factor);
		}
		else if (funcName.equals("finiteFieldAdd"))
		{
			return new FiniteFieldAdd(factor);    		
		}
		else if (funcName.equals("finiteFieldProjection"))
		{
			return new FiniteFieldProjection(factor);
		}
		else
			throw new DimpleException("Not implemented");
	}
	
	public ISolverVariable createVariable(VariableBase var)  
	{
		if (var.getModelerClassName().equals("FiniteFieldVariable"))
			return new FiniteFieldVariable(var);
		else
			return new SVariable(var);
	}

	@Override
	public boolean customFactorExists(String funcName) 
	{
		if (funcName.equals("finiteFieldMult"))
			return true;
		else if (funcName.equals("finiteFieldAdd"))
			return true;
		else if (funcName.equals("finiteFieldProjection"))
			return true;
		else
			return false;	
	}


	private static Random _rand = new Random();

	public static Random getRandom()
	{
		return _rand;
	}
	
	public void setSeed(long seed)
	{
		_rand = new Random(seed);
	}
	
	@Override
	public ISolverFactor createFactor(Factor factor)  
	{
		if (customFactorExists(factor.getFactorFunction().getName()))
		{
			return createCustomFactor(factor);
		}
		else
		{
	
			STableFactor tf = new STableFactor(factor);
			if (_damping != 0)
				setDampingForTableFunction(tf);
			return tf;
		}
	}
	

	/*
	 * Set the global solver damping parameter.  We have to go through all factor graphs
	 * and update the damping parameter on all existing table functions in that graph.
	 */
	public void setDamping(double damping) 
	{		
		_damping = damping;
		for (Factor f : _factorGraph.getNonGraphFactors())
		{
			STableFactor tf = (STableFactor)f.getSolver();
			setDampingForTableFunction(tf);
		}
	}
	
	public double getDamping()
	{
		return _damping;
	}

	/*
	 * This method applies the global damping parameter to all of the table function's ports
	 * and all of the variable ports connected to it.  This might cause problems in the future
	 * when we support different damping parameters per edge.
	 */
	protected void setDampingForTableFunction(STableFactor tf)
	{
		
		for (int i = 0; i < tf.getFactor().getSiblings().size(); i++)
		{
			tf.setDamping(i,_damping);
			VariableBase var = (VariableBase)tf.getFactor().getConnectedNodesFlat().getByIndex(i);
			for (int j = 0; j < var.getSiblings().size(); j++)
			{
				SVariable svar = (SVariable)var.getSolver();
				svar.setDamping(j,_damping);
			}
		}		

	}
	
	public void baumWelch(FactorTable [] fts, int numRestarts, int numSteps)
	{
		ParameterEstimator pe = new ParameterEstimator.BaumWelch(_factorGraph, fts, SFactorGraph.getRandom());
		pe.run(numRestarts, numSteps);
	}
	
	
	public class GradientDescent extends ParameterEstimator
	{
		private double _scaleFactor;

		public GradientDescent(FactorGraph fg, FactorTable[] tables, Random r, double scaleFactor) 
		{
			super(fg, tables, r);
			_scaleFactor = scaleFactor;
		}

		@Override
		public void runStep(FactorGraph fg) 
		{
			//_factorGraph.solve();
			for (FactorTable ft : getTables())
			{
				double [] weights = ft.getWeights();
			      //for each weight
				for (int i = 0; i < weights.length; i++)
				{
			           //calculate the derivative
					double derivative = calculateDerivativeOfBetheFreeEnergyWithRespectToWeight(ft, i);
					
			        //move the weight in that direction scaled by epsilon
					ft.changeWeight(i,weights[i] - weights[i]*derivative*_scaleFactor);
				}
			}
		}
		
	}
	
	public void pseudoLikelihood(FactorTable [] fts, 
			VariableBase [] vars,
			Object [][] data,
			int numSteps, 
			double stepScaleFactor)
	{
		
	}
	
	public static int [][] convertObjects2Indices(VariableBase [] vars, Object [][] data)
	{
		
		return null;
	}

	
	@Override
	public void estimateParameters(FactorTable [] fts, int numRestarts, int numSteps, double stepScaleFactor)
	{
		new GradientDescent(_factorGraph, fts, getRandom(), stepScaleFactor).run(numRestarts, numSteps);
	}

	
	public double calculateDerivativeOfBetheFreeEnergyWithRespectToWeight(FactorTable ft,
			int weightIndex)
	{
		//BFE = InternalEnergy - BetheEntropy
		//InternalEnergy = Sum over all factors (Internal Energy of Factor) 
		//                   + Sum over all variables (Internal Energy of Variable)
		//BetheEntropy = Sum over all factors (BetheEntropy(factor)) 
		//                  + sum over all variables (BetheEntropy(variable)
		//So derivative of BFE = Sum over all factors that contain the weight
		//                                              (derivative of Internal Energy of Factor
		//                                              - derivative of BetheEntropy of Factor)
		//                        
		
		_currentFactorTable = ft;
		
				
		for (Factor f : _factorGraph.getFactorsFlat())
		{
			((STableFactor)f.getSolver()).initializeDerivativeMessages(ft.getWeights().length);
		}
		for (VariableBase vb : _factorGraph.getVariablesFlat())
			((SVariable)vb.getSolver()).initializeDerivativeMessages(ft.getWeights().length);
		
		setCalculateDerivative(true);
		
		double result = 0;
		try
		{
			_factorGraph.solve();
			for (Factor f : _factorGraph.getFactorsFlat())
			{
				STableFactor stf = (STableFactor)f.getSolver();
				result += stf.calculateDerivativeOfInternalEnergyWithRespectToWeight(weightIndex);
				result -= stf.calculateDerivativeOfBetheEntropyWithRespectToWeight(weightIndex);
						
			}
			for (VariableBase v : _factorGraph.getVariablesFlat())
			{
				SVariable sv = (SVariable)v.getSolver();
				result += sv.calculateDerivativeOfInternalEnergyWithRespectToWeight(weightIndex);
				result += sv.calculateDerivativeOfBetheEntropyWithRespectToWeight(weightIndex);
			}
		} 
		finally
		{
			setCalculateDerivative(false);
		}
		
		return result;
	}
	
	public void setCalculateDerivative(boolean val)
	{
		for (Factor f : _factorGraph.getFactorsFlat())
		{
			STableFactor stf = (STableFactor)f.getSolver();
			stf.setUpdateDerivative(val);
		}
		for (VariableBase vb : _factorGraph.getVariablesFlat())
		{
			SVariable sv = (SVariable)vb.getSolver();
			sv.setCalculateDerivative(val);
		}
	}
	
	
	public FactorTable getCurrentFactorTable()
	{
		return _currentFactorTable;
	}



	

}
