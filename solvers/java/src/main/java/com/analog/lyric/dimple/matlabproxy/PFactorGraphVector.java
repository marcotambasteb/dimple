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

package com.analog.lyric.dimple.matlabproxy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import com.analog.lyric.dimple.FactorFunctions.NopFactorFunction;
import com.analog.lyric.dimple.FactorFunctions.core.FactorFunction;
import com.analog.lyric.dimple.FactorFunctions.core.TableFactorFunction;
import com.analog.lyric.dimple.matlabproxy.repeated.IPVariableStreamSlice;
import com.analog.lyric.dimple.matlabproxy.repeated.PFactorGraphStream;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Discrete;
import com.analog.lyric.dimple.model.DiscreteFactor;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.FactorBase;
import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.model.INode;
import com.analog.lyric.dimple.model.Node;
import com.analog.lyric.dimple.model.Real;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.model.repeated.FactorGraphStream;
import com.analog.lyric.dimple.model.repeated.IVariableStreamSlice;
import com.analog.lyric.dimple.schedulers.IScheduler;
import com.analog.lyric.dimple.schedulers.schedule.FixedSchedule;
import com.analog.lyric.dimple.schedulers.scheduleEntry.EdgeScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.IScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.NodeScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.SubScheduleEntry;
import com.analog.lyric.dimple.solvers.interfaces.IFactorGraphFactory;
import com.analog.lyric.util.misc.FactorGraphDiffs;
import com.analog.lyric.util.misc.MapList;
import com.analog.lyric.util.misc.Matlab;

@Matlab
public class PFactorGraphVector extends PFactorVector
{
	/*--------------
	 * Construction
	 */
	
	public PFactorGraphVector(FactorGraph f)
	{
		super(new Node [] {f});
	}
	
	public PFactorGraphVector(Node [] nodes)
	{
		super(nodes);
	}

	/*-----------------
	 * PObject methods
	 */

	@Override
	public boolean isDiscrete()
	{
		for (Factor f : getGraph().getFactorsFlat())
			if (!f.isDiscrete())
				return false;
		
		return true;
	}
	
	@Override
	public boolean isGraph() {
		return true;
	}

	/*---------------------
	 * PNodeVector methods
	 */
	
	@Override
	public PNodeVector createNodeVector(Node[] nodes) {
		return new PFactorGraphVector(nodes);
	}
	
	/*-----------------------------
	 * PFactorGraphVector methods
	 */

	private FactorGraph getGraph()
	{
		if (size() != 1)
			throw new DimpleException("operation not supported");
		return (FactorGraph)getModelerNode(0);
	}
	
	public String getMatlabSolveWrapper()
	{
		return getGraph().getSolver().getMatlabSolveWrapper();
	}
	
	public int getNumSteps()
	{
		return getGraph().getNumSteps();
	}
	
	public void setNumSteps(int numSteps)
	{
		getGraph().setNumSteps(numSteps);
	}
	
	public void setNumStepsInfinite(boolean val)
	{
		getGraph().setNumStepsInfinite(val);
	}
	public boolean getNumStepsInfinite()
	{
		return getGraph().getNumStepsInfinite();
	}
	
	public PFactorGraphVector addGraph(PFactorGraphVector childGraph, PVariableVector varVector)
	{
    	if (getGraph().isSolverRunning())
    		throw new DimpleException("No changes allowed while the solver is running.");

    	return new PFactorGraphVector(getGraph().addGraph(childGraph.getGraph(), varVector.getVariableArray()));
		
		
	}
	
	public boolean customFactorExists(String funcName)
	{
    	if (getGraph().isSolverRunning())
    		throw new DimpleException("No changes allowed while the solver is running.");

		return getGraph().customFactorExists(funcName);
	}
	
	public PFactorVector createFactor(PFactorTable factorTable, Object [] vars)
	{
    	if (getGraph().isSolverRunning())
    		throw new DimpleException("No changes allowed while the solver is running.");

    	Factor f = getGraph().addFactor(new TableFactorFunction("table", factorTable.getModelerObject()),PHelpers.convertToMVariablesAndConstants(vars));
    	
    	if (f.isDiscrete())
    		return new PDiscreteFactorVector((DiscreteFactor) f);
    	else
    		return new PFactorVector(f);
		
	}


	public PFactorVector createFactor(FactorFunction factorFunction, Object [] vars)
	{
    	if (getGraph().isSolverRunning())
    		throw new DimpleException("No changes allowed while the solver is running.");

    	Factor f = getGraph().addFactor(factorFunction,PHelpers.convertToMVariablesAndConstants(vars));
    	
    	if (f.isDiscrete())
    		return new PDiscreteFactorVector((DiscreteFactor) f);
    	else
    		return new PFactorVector(f);
		
	}
	
	public PFactorVector createFactor(PFactorFunction factorFunction, Object [] vars)
	{
    	
    	FactorFunction ff = factorFunction.getModelerObject();
    	return createFactor(ff,vars);
	}
	
	
	
    public void solve()
    {
    	if (getGraph().isSolverRunning())
    		throw new DimpleException("No changes allowed while the solver is running.");

    	getGraph().solve();
    }
    
    public void startContinueSolve()
    {
    	getGraph().getSolver().continueSolve();
    }
    
    public void continueSolve()
    {
    	if (getGraph().isSolverRunning())
    		throw new DimpleException("No changes allowed while the solver is running.");

    	getGraph().continueSolve();
    	
    }
    
    public void solveOneStep()
    {
    	if (getGraph().isSolverRunning())
    		throw new DimpleException("No changes allowed while the solver is running.");

    	getGraph().solveOneStep();
    }
    
    public void startSolveOneStep()
    {
    	getGraph().getSolver().startSolveOneStep();
    }
    
    public boolean isSolverRunning()
    {
    	return getGraph().isSolverRunning();
    }
  
    
    public void startSolver()
    {
    	getGraph().getSolver().startSolver();
    }
    
	public PVariableVector getVariableVector(int relativeNestingDepth,int forceIncludeBoundaryVariables)
	{
    	if (isSolverRunning())
    		throw new DimpleException("No changes allowed while the solver is running.");

		PVariableVector tmp =  PHelpers.convertToVariableVector(getGraph().getVariables(relativeNestingDepth,forceIncludeBoundaryVariables!=0));
		return tmp;
	}
	
    public PFactorVector getFactors(int relativeNestingDepth)
    {
    	return getFactors(getGraph().getFactors(relativeNestingDepth));
    }

    public PFactorVector getFactors(MapList<FactorBase> factors)
    {
    	if (getGraph().isSolverRunning())
    		throw new DimpleException("No changes allowed while the solver is running.");
    	
    	
    	int size = factors.values().size();
    	Node [] nodes = new Node[size];
    	int i = 0;
    	for (FactorBase fb : factors.values())
    	{
    		nodes[i] = fb;
    		i++;
    	}
    	 
    	return PHelpers.convertToFactorVector(nodes);
    	//return new PFactorVector(nodes);
    }
    
	public int [][] getAdjacencyMatrix()
	{
		return getGraph().getAdjacencyMatrix();
	}

    //Returns an adjacency matrix with the given nesting depth.
	public int [][] getAdjacencyMatrix(int relativeNestingDepth)
	{
		return getGraph().getAdjacencyMatrix(relativeNestingDepth);
	}
	
	//Returns an adjacency matrix of the given objects.
	public int [][] getAdjacencyMatrix(Object [] objects)
	{
		ArrayList<Node> alNodes = new ArrayList<Node>();
		
		for (int i = 0; i < objects.length; i++)
		{
			PNodeVector tmp = (PNodeVector)objects[i];
			for (int j= 0; j < tmp.size(); j++)
			{
				alNodes.add(tmp.getModelerNode(j));
			}
				
		}
		
		Node [] array = new Node[alNodes.size()];
		for (int i =0 ; i < array.length; i++)
			array[i] = alNodes.get(i);
		
		return getGraph().getAdjacencyMatrix(array);
	}
	
	public void interruptSolver()
	{
		getGraph().getSolver().interruptSolver();
	}
	

	
	public PFactorVector addFactorVectorized(PFactorVector factor, Object [] vars, Object [] indices)
	{
		PNodeVector [] nodes = PHelpers.convertObjectArrayToNodeVectorArray(vars);
		int [][][] intIndices = PHelpers.extractIndicesVectorized(indices);
		
		PNodeVector [][] args = PHelpers.extractVectorization(nodes, intIndices);
		
		Node [] retval = new Node[args.length];
		for (int i = 0; i < args.length; i++)
			retval[i] = createFactor(factor.getFactorFunction(),args[i]).getModelerNode(0);
	
		return PHelpers.convertToFactorVector(retval);
	}
	
	public PFactorGraphVector addGraphVectorized(PFactorGraphVector graph, Object [] vars, Object [] indices)
	{
		PNodeVector [] nodes = PHelpers.convertObjectArrayToNodeVectorArray(vars);
		int [][][] intIndices = PHelpers.extractIndicesVectorized(indices);
		PNodeVector [][] args = PHelpers.extractVectorization(nodes, intIndices);
		
		PVariableVector varVector = new PVariableVector();
		
		Node [] retval = new Node[args.length];
		for (int i = 0; i < args.length; i++)
		{
			varVector = (PVariableVector)varVector.concat(args[i]);

			retval[i] = addGraph(graph,varVector).getModelerNode(0);
		}
	
		return new PFactorGraphVector(retval);

	}
	
	public void setSolver(IFactorGraphFactory solver)
	{
		getGraph().setSolverFactory(solver);
	}

	public PFactorVector createCustomFactor(String funcName,PVariableVector varVector)
	{
    	if (getGraph().isSolverRunning())
    		throw new DimpleException("No changes allowed while the solver is running.");
    	
		VariableBase [] vars = varVector.getVariableArray();
		Factor f = getGraph().addFactor(new NopFactorFunction(funcName), vars);
		return new PFactorVector(f);
	}

	
	public PFactorVector createCustomFactor(String funcName, Object [] variables)
	{
    	if (getGraph().isSolverRunning())
    		throw new DimpleException("No changes allowed while the solver is running.");

    	Factor f = getGraph().addFactor(new NopFactorFunction(funcName),PHelpers.convertToMVariablesAndConstants(variables));
		return new PFactorVector(f);
	}

	public PFactorGraphVector [] getNestedGraphs()
	{
		Collection<FactorGraph> graphs = getGraph().getNestedGraphs();
		
		PFactorGraphVector [] retval = new PFactorGraphVector[graphs.size()];
		
		int i = 0;
		for (FactorGraph g : graphs)
		{
			retval[i] = new PFactorGraphVector(g);
			i++;
		}

		return retval;
	}

	public boolean isTree(int relativeNestingDepth)
	{
		return getGraph().isTree(relativeNestingDepth);
	}





	public PNodeVector [] depthFirstSearch(PNodeVector root, int searchDepth, int relativeNestingDepth)
	{
		if (root.size() != 1)
			throw new DimpleException("choose one root");
		
		MapList<INode> nodes = getGraph().depthFirstSearch(root.getModelerNode(0), searchDepth,relativeNestingDepth);
		
		PNodeVector [] retval = new PNodeVector[nodes.size()];
		
		
		for (int i = 0; i < retval.length; i++)
		{
			retval[i] = PHelpers.wrapObject(nodes.getByIndex(i));
		}
		
		return retval;
	}
	
	public PFactorGraphStream addRepeatedFactor(PFactorGraphVector nestedGraph, int bufferSize,Object ... vars)
	{
		//Object [] arr = new Object[vars.length];
		ArrayList<Object> al = new ArrayList<Object>();
				
		for (int i = 0; i < vars.length; i++)
		{
			if (vars[i] instanceof PVariableVector)
			{
				PVariableVector pvv = (PVariableVector)vars[i];
				if (pvv.size() != 1)
					throw new DimpleException("only support one var for now");
				al.add(pvv.getModelerNode(0));
			}
			else if (vars[i] instanceof IPVariableStreamSlice)
			{
				IVariableStreamSlice [] slices = ((IPVariableStreamSlice)vars[i]).getModelerObjects();
				for (int j = 0; j < slices.length; j++)
					al.add(slices[j]);
			}
			else
			{
				throw new DimpleException("when this happen?");
				//arr[i] = vars[i];
			}
		}
		
		Object [] newarray = al.toArray();
	
		FactorGraphStream rfg = getGraph().addRepeatedFactor(nestedGraph.getGraph(), bufferSize, newarray);
		return new PFactorGraphStream(rfg);
	}

    public void baumWelch(Object [] factorsAndTables,int numRestarts,int numSteps)
    {
		Object [] mfandt = new Object[factorsAndTables.length];
		for (int i = 0; i < factorsAndTables.length; i++)
		{
			if (factorsAndTables[i] instanceof PFactorTable)
				mfandt[i] = ((PFactorTable)factorsAndTables[i]).getModelerObject();
			else if (factorsAndTables[i] instanceof PFactorVector)
			{
				PFactorVector pfv = (PFactorVector)factorsAndTables[i];
				if (pfv.size() != 1)
					throw new DimpleException("for now we only support factor vectors with a single factor");
				
				mfandt[i] = pfv.getModelerNode(0);
			}
			else
				throw new DimpleException("Unsupported argument to estimateParameters");
		}
		this.getGraph().baumWelch(mfandt,numRestarts,numSteps);
    }

	
	public void estimateParameters(Object [] factorsAndTables,int numRestarts,int numSteps, double stepScaleFactor)
	{
		Object [] mfandt = new Object[factorsAndTables.length];
		for (int i = 0; i < factorsAndTables.length; i++)
		{
			if (factorsAndTables[i] instanceof PFactorTable)
				mfandt[i] = ((PFactorTable)factorsAndTables[i]).getModelerObject();
			else if (factorsAndTables[i] instanceof PFactorVector)
			{
				PFactorVector pfv = (PFactorVector)factorsAndTables[i];
				if (pfv.size() != 1)
					throw new DimpleException("for now we only support factor vectors with a single factor");
				
				mfandt[i] = pfv.getModelerNode(0);
			}
			else
				throw new DimpleException("Unsupported argument to estimateParameters");
		}
		this.getGraph().estimateParameters(mfandt,numRestarts,numSteps,stepScaleFactor);
	}

	public void advance()
	{
		getGraph().advance();
	}

	
	public boolean hasNext()
	{
		return getGraph().hasNext();
	}
	
	
	public boolean isAncestorOf(Object o)
	{
		if (! (o instanceof PNodeVector))
			return false;
		
		PNodeVector pn = (PNodeVector)o;
		if (pn.size() != 1)
			throw new DimpleException("only support variable of size 1");
		
		return getGraph().isAncestorOf(pn.getModelerNode(0));
	}
		
	 /*
	 * Let's the user specify a fixed schedule.  Expects a list of items
	 * where each item is one of the following:
	 * -A VariableVector
	 * -A FactorVector
	 * -An edge (specified with a list of two connected nodes)
	 * 
	 * TODO: Push this down
	 */
	public void setSchedule(Object [] schedule)
	{
		ArrayList<IScheduleEntry> alEntries = new ArrayList<IScheduleEntry>();
		
		//Convert schedule to a list of nodes and edges
		for (int i = 0; i < schedule.length; i++)
		{
			Object obj = schedule[i];
			
			if (obj instanceof Object [])
			{
				Object [] objArray = (Object[])obj;
				if (objArray.length != 2)
				{
					throw new DimpleException("length of array containing edge must be 2");
				}
				
				//Should be an edge
				INode node1 = PHelpers.convertToNode(objArray[0]);
				INode node2 = PHelpers.convertToNode(objArray[1]);
				int portNum = node1.getPortNum(node2);
				alEntries.add(new EdgeScheduleEntry(node1, portNum));
			}
			else
			{
				if (obj instanceof PFactorGraphVector)
				{
					Node [] nodes = PHelpers.convertToNodeArray(obj);
					for (Node n : nodes)
						alEntries.add(new SubScheduleEntry(((FactorGraph)n).getSchedule()));
					
				}
				else
				{
					Node [] nodes = PHelpers.convertToNodeArray(obj);
					for (Node n : nodes)
						alEntries.add(new NodeScheduleEntry(n));
				}
			}
		}

		IScheduleEntry [] entries = new IScheduleEntry[alEntries.size()];
		alEntries.toArray(entries);
		
		getGraph().setSchedule(new FixedSchedule(entries));
	}
	
	public void removeFactor(PFactorVector factor)
	{
		Node [] factors = factor.getModelerNodes();
		
		for (Node n : factors)
			getGraph().remove((Factor)n);
	}
	
	
	public String serializeToXML(String FgName, String targetDirectory)
	{
		return getGraph().serializeToXML(FgName, targetDirectory);
	}

	public void initialize()
    {
    	if (getGraph().isSolverRunning())
    		throw new DimpleException("No changes allowed while the solver is running.");

    	getGraph().initialize();
    }
    

    
    public PFactorVector [] getNonGraphFactors(int relativeNestingDepth)
    {
    	return getNonGraphFactors(getGraph().getNonGraphFactors(relativeNestingDepth));
    }
    
    public PFactorVector [] getNonGraphFactors(MapList<Factor> factors)
    {
    	if (getGraph().isSolverRunning())
    		throw new DimpleException("No changes allowed while the solver is running.");
    	
    	
    	return PHelpers.convertFactorListToFactors(factors.values());
    }

    
	public boolean hasParentGraph()
	{
		return getGraph().hasParentGraph();
	}
	

	public PFactorGraphVector getParentGraph()
	{
		FactorGraph mgraph = getGraph().getParentGraph();
		if(mgraph != null)
			return new PFactorGraphVector(mgraph);
		else
			return null;
	}
	
	public PFactorGraphVector getRootGraph()
	{
		return new PFactorGraphVector(getGraph().getRootGraph());
	}

	public PVariableVector getVariableByName(String name)
	{
		VariableBase mo = getGraph().getVariableByName(name);
		if (mo != null)
			return (PVariableVector)PHelpers.wrapObject(mo);
		else
			return null;
	}
	
	public PFactorVector getFactorByName(String name)
	{
		Factor mo = getGraph().getFactorByName(name);
		if(mo != null)
			return (PFactorVector)PHelpers.wrapObject(mo);
		else
			return null;
	}
	
	public PFactorGraphVector getGraphByName(String name)
	{
		FactorGraph mo = getGraph().getGraphByName(name);
		if(mo != null)
			return new PFactorGraphVector(mo);
		else
			return null;
	}
	
	public PVariableVector getVariableByUUID(UUID uuid)
	{
		VariableBase mo = getGraph().getVariableByUUID(uuid);
		if(mo != null)
			return (PVariableVector)PHelpers.wrapObject(mo);
		else
			return null;
	}
	
	public PFactorVector getFactorByUUID(UUID uuid)
	{
		Factor mo = getGraph().getFactorByUUID(uuid);
		if(mo != null)
			return (PFactorVector) PHelpers.wrapObject(mo);
		else
			return null;
	}
	
	public PFactorGraphVector getGraphByUUID(UUID uuid)
	{
		FactorGraph mo = getGraph().getGraphByUUID(uuid);
		if(mo != null)
			return (PFactorGraphVector)PHelpers.wrapObject(mo);
		else
			return null;
	}
	
	public void setScheduler(IScheduler scheduler)
	{
    	if (getGraph().isSolverRunning())
    		throw new DimpleException("No changes allowed while the solver is running.");

    	getGraph().setScheduler(scheduler);
	}
	
		
	public PFactorGraphStream [] getFactorGraphStreams()
	{
		PFactorGraphStream [] retval = new PFactorGraphStream[getGraph().getFactorGraphStreams().size()];
		
		for (int i = 0; i < retval.length; i++)
		{
			FactorGraphStream fgs = getGraph().getFactorGraphStreams().get(i);
			retval[i] = new PFactorGraphStream(fgs);
		}
		
		return retval;
	}
	
	public FactorGraphDiffs getFactorGraphDiffsByName(PFactorGraphVector b)
	{
		return FactorGraphDiffs.getFactorGraphDiffs(
				   getGraph(),
				   b.getGraph(),
				   false,
				   true);
	}
	
	public PFactorVector joinFactors(Object [] factors)
	{
		//convert Object [] to Factor array
		Factor [] facs = new Factor[factors.length];
		
		for (int i = 0; i < factors.length; i++)
		{
			//TODO: error check?
			facs[i] = (Factor)PHelpers.convertToNode(factors[i]);
		}
		
		Factor f = getGraph().join(facs);
		return (PFactorVector)PHelpers.wrapObject(f);
	}
	
	public PVariableVector joinVariables(Object [] variables)
	{
		VariableBase [] vars = new VariableBase[variables.length];
		
		for (int i = 0; i < variables.length; i++)
		{
			if (! (variables[i] instanceof PVariableVector))
				throw new DimpleException("only variable bases supported");
			
			vars[i] = (VariableBase)PHelpers.convertToNode(variables[i]);
			
		}
		return (PVariableVector)PHelpers.wrapObject(getGraph().join(vars));

	}

	
	public PVariableVector split(PVariableVector variable, Object [] factors)
	{
		Factor [] pfactors = {};
		
		if (factors != null)
			pfactors = PHelpers.convertObjectArrayToFactors(factors);
		Node n = PHelpers.convertToNode(variable);

		if (n instanceof Discrete)
			return new PDiscreteVariableVector(getGraph().split((VariableBase)n,pfactors));
		else
			return new PRealVariableVector((Real)(getGraph().split((VariableBase)n,pfactors)));
			
	}
	
	public double getBetheFreeEnergy()
	{
		return getGraph().getBetheFreeEnergy();
	}

	
	// For operating collectively on groups of variables that are not already part of a variable vector
	public int defineVariableGroup(Object[] variables)
	{
		ArrayList<VariableBase> variableList = new ArrayList<VariableBase>();
		for (int i = 0; i < variables.length; i++)
		{
			VariableBase[] modelerVariables = ((PVariableVector)variables[i]).getModelerVariables();
			for (int j = 0; j < modelerVariables.length; j++)
				variableList.add(modelerVariables[j]);
		}

		return getGraph().defineVariableGroup(variableList);
	}

}
