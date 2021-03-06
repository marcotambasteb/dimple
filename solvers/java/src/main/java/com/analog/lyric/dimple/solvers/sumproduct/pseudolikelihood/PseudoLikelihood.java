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

package com.analog.lyric.dimple.solvers.sumproduct.pseudolikelihood;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;
import com.analog.lyric.dimple.FactorFunctions.core.FactorTable;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Discrete;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.model.FactorList;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.model.VariableList;
import com.analog.lyric.dimple.solvers.core.ParameterEstimator;

/*
 * The pseudolikelihood class uses the Pseudolikelihood algorithm
 * to estimate parameters of a factor graph.
 */
public class PseudoLikelihood extends ParameterEstimator
{

	private double _scaleFactor;
	private HashMap<Factor,FactorInfo> _factor2factorInfo = new HashMap<Factor, FactorInfo>();
	private HashMap<VariableBase, VariableInfo> _var2varInfo = new HashMap<VariableBase, VariableInfo>();
	private int [][] _data;
	private HashMap<VariableBase,Integer> _var2index = new HashMap<VariableBase, Integer>();
	private VariableBase [] _vars;
	
	//The constructor saves the factor graph, the tables of interest, and the variables
	//It also builds the NodeInfo object mappings.
	public PseudoLikelihood(FactorGraph fg, 
			FactorTable[] tables, 
			VariableBase [] vars) 
	{
		super(fg, tables, new Random());
	
		
		_vars = vars;
		
		//Get all factors for this grah
		FactorList fl = fg.getFactorsFlat();
		
		//create a mapping from the input variables to the index into the input variable array
		for (int i = 0; i < vars.length; i++)
			_var2index.put(vars[i], i);
		
		//Factor infos will be used to store joint empirical distributions over the factor
		for (Factor f : fl)
			_factor2factorInfo.put(f,new FactorInfo(f,_var2index));

		//Retrieve all the variables that are connected to factors in the graph.
		HashSet<VariableBase> varsConnectedToFactors = new HashSet<VariableBase>();
		for (Factor f : fl)
			varsConnectedToFactors.addAll(f.getVariables());
		
		//for each variable, create a variable info
		//This will be used to store a joint empirical distribution over all of the 
		//variable's neighbors.
		//Additionally it will be used to calculate the probability of a setting of a variable given
		//the emperical distribution and the current factor weights.
		for (VariableBase v : varsConnectedToFactors)
			_var2varInfo.put(v,VariableInfo.createVariableInfo(v, _var2index));

	}
	
	//Users can set data directly
	public void setData(Object [][] data)
	{
		setData(convertObjects2Indices(data));
	}
	
	//Users can set data using indices.
	//This routine builds up the empirical distributions.
	public void setData(int [][] data)
	{
		_data = data;

		//First reset the nodeinfos.
		for (FactorInfo fi : _factor2factorInfo.values())
			fi.reset();
		for (VariableInfo vi : _var2varInfo.values())
			vi.reset();
		
		//Then go through the data and add samples to all the factorinfos and
		//variableinfos.
		for (int i = 0; i < data.length; i++)
		{
			for (FactorInfo fi : _factor2factorInfo.values())
				fi.addSample(data[i]);
			for (VariableInfo vi : _var2varInfo.values())
				vi.addSample(data[i]);
		}

	}
	
	//users can set the scale factor.
	public void setScaleFactor(double scale)
	{
		_scaleFactor = scale;
	}
	
	//The learn function sets the data, num steps, scale factor and runs the gradient descent
	public void learn(Object [][] data, int numSteps,double scaleFactor)
	{
		setForceKeep(true);
		setData(data);
		setScaleFactor(scaleFactor);
		super.run(0,numSteps);
	}
	
	//The learn function sets the data, num steps, scale factor and runs the gradient descent
	public void learn(int [][] data, int numSteps, double scaleFactor)
	{
		setForceKeep(true);
		setData(data);
		setScaleFactor(scaleFactor);
		super.run(0,numSteps);
	}

	//Users cannot call run directly.
	public void run(int numRestarts,int numSteps)
	{
		throw new DimpleException("Not supported");
	}
	
	//To do gradient descent, we have to be able to calculate the gradient.
	public double [][] calculateGradient()
	{
		//gradient = FactorDegree * imperical distribution of factor
		//     - sum over all variables
		//          sum over all unique neighbor sample settings
		//             Pd(neighbors)*p(var | neighbors)
		
		if (_data == null)
			throw new DimpleException("Must set data first");
		
		//Get the list of tables of interest.
		FactorTable [] tables = getTables();
		HashMap<FactorTable,ArrayList<Factor>> table2factors = getTable2Factors();
		
		//initialize the gradient
		double [][] gradients = new double[tables.length][];
		
		//Invalidate the distributions because parameters may have changed.
		for (VariableInfo vi : _var2varInfo.values())
			vi.invalidateDistributions();
		
		//for each unique factor table
		for (int i = 0; i < tables.length; i++)
		{
			//cache some stuff.
			double [] weights = tables[i].getWeights();
			int [][] indices = tables[i].getIndices();
			int degree = indices[0].length;
			
			//TODO: avoid this new?
			gradients[i] = new double[weights.length];
			
			ArrayList<Factor> factors = table2factors.get(tables[i]);
			
			//If this table actually is related to this graph
			if (factors != null)
			{
				//for each factor
				for (int k = 0; k < factors.size(); k++)
				{
					Factor f = factors.get(k);
					VariableList vl = f.getVariables();
					FactorInfo fi = _factor2factorInfo.get(f);	
	
					//for each weight
					for (int j = 0; j < weights.length; j++)
					{
						//add degree * pd(indices)
						double impericalFactorD = fi.getDistribution().get(indices[j]);
						gradients[i][j] += degree*impericalFactorD;
					}
	
					//for each variable
					for (VariableBase v : vl)
					{
						VariableInfo vi = _var2varInfo.get(v);
						
						//for each element of the variables domain
						for (int d = 0; d < v.asDiscreteVariable().getDiscreteDomain().size(); d++)
						{
							Set<LinkedList<Integer>> samples = vi.getUniqueSamples();
							
							//for each unique sample
							for (LinkedList<Integer> sample : samples)
							{
								//calculate pneighbors
								double prob = vi.getProb(d,sample);
								
								//find weight index from variable domain and unique sample
								int index = vi.getFactorTableIndex(f, d, sample);
								
								//subtract prob
								gradients[i][index] -= prob;
								
							}
						}
					}
				}
			}
		}

		return gradients;
	}
	
	//One step of gradient descent simply calculates the gradient
	//and applies it.
	@Override
	public void runStep(FactorGraph fg) 
	{
		double [][] gradient = calculateGradient();
		applyGradient(gradient);
	}
	
	//Given a gradient, change the parameters.
	private void applyGradient(double [][] gradient)
	{
		FactorTable [] tables = getTables();
		
		// for each table
		for (int i = 0; i < tables.length; i++)
		{
			double [] ws = tables[i].getWeights();
			double normalizer = 0;
			
			//for each weight
			for (int j = 0; j < ws.length; j++)
			{
				//update the parameter
				double tmp = Math.log(ws[j]);
				tmp = tmp + gradient[i][j]*_scaleFactor;
				ws[j] = Math.exp(tmp);
				
				//build a normalizing constant
				normalizer += ws[j];
			}
			
			//normalize
			for (int j = 0; j < ws.length; j++)
				ws[j] /= normalizer;
			
			//save the changed weights.
			tables[i].changeWeights(ws);
		}
	}
	

	//Calculate the numerical gradient.  Useful for debugging.
	public double calculateNumericalGradient(FactorTable table, int weight, double delta)
	{
		if (_data == null)
			throw new DimpleException("Must set data first");
			
		//numerical gradient = change of pseudo likelihood / change of parameter 
		
		double y1 = calculatePseudoLikelihood();
		double oldval = table.getWeights()[weight];
		double newval = oldval * Math.exp(delta);
		table.getWeights()[weight] = newval;
		double y2 = calculatePseudoLikelihood();
		table.getWeights()[weight] = oldval;		
		return (y2-y1)/delta;
	}

	//Used for calculating the numerical gradient
	public double calculatePseudoLikelihood()
	{
		//1/M sum(m) sum(i) sum(j in neighbors(i)) tehta(xj,xi)
		//- 1/M sum(m) sum(i) log Z(neighbors(i))
		
		//retrieve the factor graph and variables
		FactorGraph fg = getFactorGraph();
		VariableList vl = fg.getVariablesFlat();
		
		double total = 0;
		
		//1/M sum(m) sum(i) sum(j in neighbors(i)) tehta(xj,xi)
		
		//for each data point
		for (int m = 0; m < _data.length; m++)
		{
			//for each variable
			for (VariableBase v : vl)
			{
				//for each factor associated with the variable
				for (Factor f : v.getFactorsFlat())
				{
					//Build the indices associated with these variables
					VariableList fvs = f.getVariables();
					int [] indices = new int[fvs.size()];
					for (int i = 0; i < indices.length; i++)
						indices[i] = _data[m][_var2index.get(fvs.getByIndex(i))];
					
					//add the term.
					double tmp = Math.log(f.getFactorTable().getWeights()[f.getFactorTable().getWeightIndexFromTableIndices(indices)]);
					total += tmp;
				}
			}
		}
		
		total /= _data.length;
		
		//- 1/M sum(m) sum(i) log Z(neighbors(i))
		double total2 = 0;
		
		//for each data point
		for (int m = 0; m < _data.length; m++)
		{
			//for each variable
			for (VariableBase v : vl)
			{
				double sum = 0;
				
				//for each domain value.
				for (int d = 0; d < ((Discrete)v).getDomain().size(); d++)
				{
					double product = 1;
					
					//for every factor connected to the variable.
					for (Factor f : v.getFactorsFlat())
					{
						//build up the list of indices associated with that factor.
						VariableList fvs = f.getVariables();
						int [] indices = new int[fvs.size()];
						for (int i = 0; i < indices.length; i++)
						{
							VariableBase fv = fvs.getByIndex(i);
							if (fv == v)
								indices[i] = d;
							else
								indices[i] = _data[m][_var2index.get(fv)];
						}
						
						//multiply in that term.
						product *=  f.getFactorTable().getWeights()[f.getFactorTable().getWeightIndexFromTableIndices(indices)];
					}
					
					//add terms together.
					sum += product;
				}
				
				//take the log of the partition function.
				total2 += Math.log(sum);
			}
		}
		
		//return the totals
		total -= total2 / _data.length;		
		return total;
	}
	
	//Used for dealing with data that is provided as domain objects rather than indices.
	final private int [][] convertObjects2Indices(Object [][] data)
	{
		int [][] retval = new int[data.length][data[0].length];
		for (int i = 0; i < retval.length; i++)
		{
			for (int j = 0; j < retval[i].length; j++)
			{
				retval[i][j] = _vars[j].asDiscreteVariable().getDiscreteDomain().getIndex(data[i][j]);
			}
		}
		
		return retval;
	}
}