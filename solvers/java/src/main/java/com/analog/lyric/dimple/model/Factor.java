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

import java.util.ArrayList;
import java.util.HashSet;

import com.analog.lyric.dimple.FactorFunctions.core.FactorFunction;
import com.analog.lyric.dimple.FactorFunctions.core.FactorFunctionBase;
import com.analog.lyric.dimple.FactorFunctions.core.FactorTable;
import com.analog.lyric.dimple.FactorFunctions.core.JointFactorFunction;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;

public class Factor extends FactorBase implements Cloneable
{
	private String _modelerFunctionName = "";
	protected ISolverFactor _solverFactor = null;
	private FactorFunction _factorFunction;
	protected VariableList _variables = null;
	int [] _directedTo = null;
	int [] _directedFrom = null;
	
	
	@Override
	public final Factor asFactor()
	{
		return this;
	}
	
	@Override
	public final boolean isFactor()
	{
		return true;
	}
	
	public String getModelerFunctionName()
	{
		return _modelerFunctionName;
	}
	
	public Factor(int id,FactorFunction factorFunc, VariableBase [] variables)
	{
		super(id);

		
		_factorFunction = factorFunc;
		_modelerFunctionName = factorFunc.getName();
		
		for (int i = 0; i < variables.length; i++)
			addVariable(variables[i]);
	}
	
	public Factor(int id,VariableBase [] variables, String modelerFunctionName)
	{
		super(id);
				
		_modelerFunctionName = modelerFunctionName;
		for (int i = 0; i < variables.length; i++)
		{
			connect(variables[i]);
			variables[i].connect(this);
		}
	}

	public FactorTable getFactorTable()
	{
		return getFactorFunction().getFactorTable(getDomains());
	}
	
	public boolean isDiscrete()
	{
		for (INode p : getSiblings())
		{
			VariableBase vb = (VariableBase)p;
			if (! vb.getDomain().isDiscrete())
			{
				return false;
			}
		}
		
		return true;
	}
	
	
	public FactorFunction getFactorFunction()
	{
		return _factorFunction;
	}
	
	public void setFactorFunction(FactorFunction function)
	{
		_factorFunction = function;
		if (_factorFunction.isDirected())	// Automatically set direction if inherent in factor function
			setDirectedTo(_factorFunction.getDirectedToIndices((_variables == null) ? 0 : _variables.size()));
	}
	
	protected void addVariable(VariableBase variable)
	{
		_variables = null;
		connect(variable);
		variable.connect(this);
	}
	

	
	@Override
	public String getLabel()
	{
		String name = _label;
		if(name == null)
		{
			name = _name;
			if(name == null)
			{
				name = getModelerFunctionName() + "_" + getId();
			}
		}
		return name;
	}

	
	@Override
	public ISolverFactor getSolver()
	{
		return _solverFactor;
	}

	@Override
	public String getClassLabel()
    {
    	return "Factor";
    }
	
	public void createSolverObject(ISolverFactorGraph factorGraph)
	{
		_variables = null;
		if (factorGraph != null)
		{
			_solverFactor = factorGraph.createFactor(this);
			_solverFactor.createMessages();
		}
		else
		{
			_solverFactor = null;
		}
	}
	
	public void replace(VariableBase oldVariable, VariableBase newVariable)
	{
		_variables = null;
		int index = _siblings.indexOf(oldVariable);
		_siblings.set(index, newVariable);
	}
	
	public Domain [] getDomains()
	{
		VariableList variables = getVariables();
		int numVariables = variables.size();
		
		Domain [] retval = new Domain[numVariables];
		
		for (int i = 0; i < numVariables; i++)
		{
			retval[i] = variables.getByIndex(i).getDomain();
		}
		
		return retval;
	}
	

	@Override
	public Factor clone()
	{
		_variables = null;
		
		/*******
		 * NOTE: Any derived class that defines instance variables that are
		 * objects (rather than primitive types) must implement clone(), which
		 * must first call super.clone(), and then deep-copy those instance
		 * variables to the clone.
		 *******/
		Factor f = (Factor) super.clone();
		
		//TODO: cloning solver factor?
		
		return f;
	}

	@Override
	public void initialize(int portNum)
	{
		if (_solverFactor != null)
			_solverFactor.resetEdgeMessages(portNum);
	}
    
	
	@Override
	public void initialize()
	{
		if (_solverFactor != null)
			_solverFactor.initialize();
		if (_factorFunction.isDirected())	// Automatically set direction if inherent in factor function
			setDirectedTo(_factorFunction.getDirectedToIndices((_variables == null) ? 0 : _variables.size()));
	}
	
	public VariableList getVariables()
	{
		//Cache the variables for performance reasons
		if (_variables == null)
		{
			_variables = new VariableList();
			for (int i = 0; i < _siblings.size(); i++)
				_variables.add((VariableBase)_siblings.get(i));
		}
		
		return _variables;
	}

	@Override
	public void update()
	{
		checkSolverNotNull();
		_solverFactor.update();
	}

	@Override
	public void updateEdge(int outPortNum)
	{
		checkSolverNotNull();
		_solverFactor.updateEdge(outPortNum);
	}
	
	private void checkSolverNotNull()
	{
		if (_solverFactor == null)
			throw new DimpleException("solver must be set before performing this action.");
	}
	
	void replaceVariablesWithJoint(VariableBase [] variablesToJoin, VariableBase newJoint)
	{
		throw new DimpleException("not implemented");
	}
	
	Factor join(Factor other)
	{
		_variables = null;
		ArrayList<VariableBase> varList = new ArrayList<VariableBase>();
		
		//go through variables from first factor and
		ArrayList<Integer> map1 = new ArrayList<Integer>();
		
		
		//First get a list of variables in common.
		for (int i = 0; i < getSiblings().size(); i++)
		{
			map1.add(i);
			varList.add(getConnectedNodeFlat(i));
			
		}
		
		ArrayList<Integer> map2 = new ArrayList<Integer>();
		
		int newnuminputs = map1.size();
		
		for (int i = 0; i < other.getSiblings().size(); i++)
		{
			boolean found = false;
			
			for (int j = 0; j < getSiblings().size(); j++)
			{
				
				if (getConnectedNodeFlat(j) == other.getConnectedNodeFlat(i))
				{
					found = true;
					map2.add(j);
					break;
				}
				
			}
			if (!found)
			{
				map2.add(varList.size());
				newnuminputs++;
				varList.add(other.getConnectedNodeFlat(i));
			}
		}
		
		FactorFunctionBase ff1 = this.getFactorFunction();
		FactorFunctionBase ff2 = other.getFactorFunction();
		JointFactorFunction jff = getParentGraph().getJointFactorFunction(ff1,ff2,map1,map2);
		
		//Remove the two old factors.
		getParentGraph().remove(this);
		getParentGraph().remove(other);
		
		//Create the table factor using the variables
		VariableBase [] vars = new VariableBase[newnuminputs];
		varList.toArray(vars);
		return getParentGraph().addFactor(jff, vars);

	}

	

	@Override
	public double getScore()
	{
		if (_solverFactor == null)
			throw new DimpleException("solver needs to be set before calculating energy");
		
		return _solverFactor.getScore();
	}
	
	@Override
	public double getInternalEnergy()
	{
		if (_solverFactor == null)
			throw new DimpleException("solver needs to be set before calculating energy");
		
		return _solverFactor.getInternalEnergy();
		
	}

	@Override
	public double getBetheEntropy()
	{
		if (_solverFactor == null)
			throw new DimpleException("solver needs to be set before calculating energy");
		
		return _solverFactor.getBetheEntropy();
		
	}


	public boolean isDirected()
	{
		if (_directedTo != null)
			return true;
		else
			return false;
	}

	private void ensureDirectedToSet()
	{
		if(_directedTo==null)
			setFactorFunction(getFactorFunction());
	}
	
	public int [] getDirectedTo()
	{
		ensureDirectedToSet();
		return _directedTo;
	}
	public int [] getDirectedFrom()
	{
		ensureDirectedToSet();
		return _directedFrom;
	}
	
	public VariableList getDirectedToVariables()
	{
		ensureDirectedToSet();
		
		VariableList vl = new VariableList();
		if (isDirected())
		{
			for (int i = 0; i < _directedTo.length; i++)
			{
				vl.add(getVariables().getByIndex(_directedTo[i]));
			}
		}
		return vl;

	}

	public void setDirectedTo(VariableList vl)
	{
		int [] directedTo = new int[vl.size()];
		for (int i = 0; i < directedTo.length; i++)
			directedTo[i] = getPortNum(vl.getByIndex(i));
		
		setDirectedTo(directedTo);

	}

	public void setDirectedTo(VariableBase ... variables)
	{
		VariableList vl = new VariableList();
		vl.add(variables);
		setDirectedTo(vl);
	}
	
	public void setDirectedTo(int [] directedTo)
	{
		getVariables();
		
		HashSet<Integer> hs = new HashSet<Integer>();
		
		_directedFrom = new int[_variables.size()-directedTo.length];
		
		for (int i = 0; i < directedTo.length; i++)
		{
			if (hs.contains(directedTo[i]) || directedTo[i] > _variables.size())
				throw new DimpleException("invalid edge");
			hs.add(directedTo[i]);
		}
		
		int index = 0;
		for (int i = 0; i < _variables.size(); i++)
		{
			if (! hs.contains(i))
			{
				_directedFrom[index] = i;
				index++;
			}
		}
		
		_directedTo = directedTo;
		
		if (getFactorFunction().factorTableExists(getDomains()))
		{
			getFactorTable().setDirected(directedTo, _directedFrom);
		}
		
		if (_solverFactor != null)
		{
			_solverFactor.setDirectedTo(directedTo);
		}
		
	}
	
	public boolean isDirectedTo(VariableBase variable)
	{
		ensureDirectedToSet();

		if (_directedTo == null)
			return false;
		
		return getDirectedToVariables().contains(variable);
	}
	
	public boolean isDirectedTo(int edge)
	{
		ensureDirectedToSet();
		
		if (_directedTo == null)
			return false;

		for (int i = 0; i < _directedTo.length; i++)
			if (_directedTo[i] == edge)
				return true;
		return false;
	}
}
