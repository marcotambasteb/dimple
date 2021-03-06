package com.analog.lyric.dimple.solvers.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import com.analog.lyric.dimple.FactorFunctions.core.FactorTable;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.FactorGraph;

public abstract class ParameterEstimator 
{
	private FactorGraph _fg;
	private FactorTable [] _tables;
	private Random _r;
	private HashMap<FactorTable,ArrayList<Factor>> _table2factors;
	private boolean _forceKeep;

	public ParameterEstimator(FactorGraph fg, FactorTable [] tables, Random r)
	{
		_fg = fg;
		_tables = tables;
		_r = r;
		
		HashMap<FactorTable,ArrayList<Factor>> table2factors = new HashMap<FactorTable, ArrayList<Factor>>();

		for (Factor f  : fg.getFactorsFlat())
		{
			FactorTable ft = f.getFactorTable();
			if (! table2factors.containsKey(ft))
				table2factors.put(ft,new ArrayList<Factor>());
			table2factors.get(ft).add(f);
		}

		//Verify directionality is consistent.
		_table2factors = table2factors;

	}
	
	public void setRandom(Random r)
	{
		_r = r;
	}
	
	public HashMap<FactorTable,ArrayList<Factor>> getTable2Factors()
	{
		return _table2factors;
	}

	public FactorTable [] getTables()
	{
		return _tables;
	}

	FactorTable [] saveFactorTables(FactorTable [] fts)
	{
		FactorTable [] savedFts = new FactorTable[fts.length];
		for (int i = 0; i < fts.length; i++)
			savedFts[i] = fts[i].copy();
		return savedFts;
	}

	FactorTable [] unique(FactorTable  [] factorTables)
	{
		HashSet<FactorTable> set = new HashSet<FactorTable>();
		for (int i = 0; i < factorTables.length; i++)
			set.add(factorTables[i]);
		factorTables = new FactorTable[set.size()];
		int i = 0;
		for (FactorTable ft : set)
		{
			factorTables[i] = ft;
			i++;
		}
		return factorTables;
	}
	
	public FactorGraph getFactorGraph()
	{
		return _fg;
	}
	
	public void setForceKeep(boolean val)
	{
		_forceKeep = val;
	}

	public void run(int numRestarts, int numSteps)
	{
		//make sure the factortable list is unique
		_tables = unique(_tables);

		//measure betheFreeEnergy
		_fg.solve();
		double currentBFE = _fg.getBetheFreeEnergy();
		FactorTable [] bestFactorTables = saveFactorTables(_tables);

		//for each restart
		for (int i = 0; i <= numRestarts; i++)
		{
			//if not first time, pick random weights
			if (i != 0)
				for (int j = 0; j < _tables.length; j++)
				{
					_tables[j].randomizeWeights(_r);
					if (_tables[j].isDirected())
						_tables[j].normalize();
				}

			//for numSteps
			for (int j = 0; j < numSteps; j++)
			{
				runStep(_fg);
			}

			_fg.solve();
			double newBetheFreeEnergy = _fg.getBetheFreeEnergy();

			//if betheFreeEnergy is better
			//store this is answer
			if (newBetheFreeEnergy < currentBFE || _forceKeep)
			{
				currentBFE = newBetheFreeEnergy;
				bestFactorTables = saveFactorTables(_tables);
			}

		}

		//Set weights to best answer
		for (int i = 0; i < _tables.length; i++)
		{
			_tables[i].copy(bestFactorTables[i]);
		}
	}

	public abstract void runStep(FactorGraph fg);

	public static class BaumWelch extends ParameterEstimator
	{

		public BaumWelch(FactorGraph fg, FactorTable[] tables, Random r) 
		{
			super(fg, tables, r);

			for (FactorTable table : getTable2Factors().keySet())
			{
				ArrayList<Factor> factors = getTable2Factors().get(table);
				int [] direction = null;
				for (Factor f : factors)
				{
					int [] tmp = f.getDirectedTo();
					if (tmp == null)
						throw new DimpleException("Baum Welch only works with directed Factors");
					if (direction == null)
						direction = tmp;
					else
					{
						if (tmp.length != direction.length)
							throw new DimpleException("Directions must be the same for all factors sharing a Factor Table");
						for (int i = 0; i < tmp.length; i++)
							if (tmp[i] != direction[i])
								throw new DimpleException("Directions must be the same for all factors sharing a Factor Table");
					}
				}
			}
		}



		@Override
		public void runStep(FactorGraph fg) 
		{

			//run BP
			fg.solve();

			//Assign new weights
			//For each Factor Table
			for (FactorTable ft : getTable2Factors().keySet())
			{
				//Calculate the average of the FactorTable beliefs
				ArrayList<Factor> factors = getTable2Factors().get(ft);

				double [] sum = new double[ft.getRows()];

				for (Factor f : factors)
				{
					double [] belief = (double[])f.getSolver().getBelief();
					for (int i = 0; i < sum.length; i++)
						sum[i] += belief[i];


				}				


				//Get first directionality
				int [] directedTo = factors.get(0).getDirectedTo();
				int [] directedFrom = factors.get(0).getDirectedFrom();

				//Set the weights to that
				ft.changeWeights(sum);
				ft.normalize(directedTo, directedFrom);
			}			
		}		
	}
}
