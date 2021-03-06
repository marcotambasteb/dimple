package com.analog.lyric.dimple.test.solvers.lp;

import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;

import org.junit.Ignore;
import org.junit.Test;

import com.analog.lyric.dimple.FactorFunctions.Cos;
import com.analog.lyric.dimple.FactorFunctions.core.FactorFunction;
import com.analog.lyric.dimple.FactorFunctions.core.FactorFunctionUtilities;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Discrete;
import com.analog.lyric.dimple.model.DiscreteDomain;
import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.model.Real;
import com.analog.lyric.dimple.model.RealDomain;
import com.analog.lyric.dimple.solvers.lp.SFactorGraph;
import com.analog.lyric.dimple.solvers.lp.Solver;

public class TestLPSolver
{
	/**
	 * Deterministic factor that asserts that only one argument may be true.
	 */
	public static class OnlyOneTrue extends FactorFunction
	{
		static public OnlyOneTrue INSTANCE = new OnlyOneTrue();
		
		@Override
		public double evalEnergy(Object ... args)
		{
			int nTrue = 0;
			for (Object arg : args)
			{
				if (FactorFunctionUtilities.toBoolean(arg))
				{
					if (++nTrue > 1)
					{
						return Double.POSITIVE_INFINITY;
					}
				}
			}
			return 0;
		}
	}
	
	/**
	 * Deterministic factor that asserts there are no duplicate arguments.
	 */
	public static class NoDups extends FactorFunction
	{
		static public NoDups INSTANCE = new NoDups();
		
		@Override
		public double evalEnergy(Object ... args)
		{
			for (int i = args.length; --i >= 0;)
			{
				Object arg = args[i];
				for (int j = i; --j >= 0;)
				{
					if (arg.equals(args[j]))
					{
						return Double.POSITIVE_INFINITY;
					}
				}
			}
			
			return 0;
		}
	}
	
	@Test
	public void test()
	{
		RealDomain realDomain = new RealDomain(0.0, 1.0);
		FactorGraph fg0 = new FactorGraph();
		fg0.setSolverFactory(new Solver());
		assertTrue(fg0.getSolver() instanceof SFactorGraph);
		Real real1 = new Real(realDomain);
		real1.setName("real1");
		try
		{
			fg0.addVariables(real1);
			fail("expected exception");
		}
		catch (DimpleException ex)
		{
			assertThat(ex.getMessage(), containsString("Variable 'real1' is not discrete"));
		}
		try
		{
			fg0.addFactor(new Cos(), real1);
			fail("expected exception");
		}
		catch (DimpleException ex)
		{
			assertThat(ex.getMessage(), containsString("is not a DiscreteFactor"));
		}
			
		DiscreteDomain booleanDomain = new DiscreteDomain(false,true);
		
		FactorGraph fg1 = new FactorGraph();
		Discrete x = new Discrete(booleanDomain);
		x.setName("x");
		Discrete y = new Discrete(booleanDomain);
		y.setName("y");
		Discrete z = new Discrete(booleanDomain);
		z.setName("z");

		fg1.addFactor(OnlyOneTrue.INSTANCE, x, y);
		fg1.addFactor(OnlyOneTrue.INSTANCE, y, z);
		fg1.addFactor(OnlyOneTrue.INSTANCE, x, z);
		
		x.setInput(.3, .7);
		y.setInput(.2, .8);
		z.setInput(.6, .3);
		
		LPSolverTestCase case1 = new LPSolverTestCase(fg1);
		case1.expectedConstraints = new String[] {
			"p(x=false) + p(x=true) = 1",
			"p(y=false) + p(y=true) = 1",
			"p(z=false) + p(z=true) = 1",
			
			"-p(x=false) + p(x=false,y=false) + p(x=false,y=true) = 0",
			"-p(x=true) + p(x=true,y=false) = 0",
			"-p(y=false) + p(x=false,y=false) + p(x=true,y=false) = 0",
			"-p(y=true) + p(x=false,y=true) = 0",

			"-p(y=false) + p(y=false,z=false) + p(y=false,z=true) = 0",
			"-p(y=true) + p(y=true,z=false) = 0",
			"-p(z=false) + p(y=false,z=false) + p(y=true,z=false) = 0",
			"-p(z=true) + p(y=false,z=true) = 0",

			"-p(x=false) + p(x=false,z=false) + p(x=false,z=true) = 0",
			"-p(x=true) + p(x=true,z=false) = 0",
			"-p(z=false) + p(x=false,z=false) + p(x=true,z=false) = 0",
			"-p(z=true) + p(x=false,z=true) = 0",
		};
		case1.testLPState();
		
		// Fix Y to false, but don't use fixed value API
		y.setInput(1, 0);
		LPSolverTestCase case2 = new LPSolverTestCase(fg1);
		case2.expectedConstraints = new String[] {
			"p(x=false) + p(x=true) = 1",
			"p(z=false) + p(z=true) = 1",
			
			"-p(x=false) + p(x=false,y=false) = 0",
			"-p(x=true) + p(x=true,y=false) = 0",

			"-p(z=false) + p(y=false,z=false) = 0",
			"-p(z=true) + p(y=false,z=true) = 0",

			"-p(x=false) + p(x=false,z=false) + p(x=false,z=true) = 0",
			"-p(x=true) + p(x=true,z=false) = 0",
			"-p(z=false) + p(x=false,z=false) + p(x=true,z=false) = 0",
			"-p(z=true) + p(x=false,z=true) = 0",
		};
		case2.testLPState();
		
		// Fix Z to true
		z.setFixedValue(true);
		LPSolverTestCase case3 = new LPSolverTestCase(fg1);
		case3.expectedConstraints = new String[] {
			"p(x=false) + p(x=true) = 1",
			
			"-p(x=false) + p(x=false,y=false) = 0",
			"-p(x=true) + p(x=true,y=false) = 0",

			"-p(x=false) + p(x=false,z=true) = 0",
		};
		case3.testLPState();
		
		// Now fix X
		x.setFixedValue(false);
		LPSolverTestCase case4 = new LPSolverTestCase(fg1);
		case4.expectedConstraints = new String[0];
		case4.testLPState();
		
		DiscreteDomain stoogeDomain = new DiscreteDomain("Moe", "Larry", "Curly");
		FactorGraph fg2 = new FactorGraph();
		Discrete a = new Discrete(stoogeDomain);
		a.setName("a");
		Discrete b = new Discrete(stoogeDomain);
		b.setName("b");
		Discrete c = new Discrete(stoogeDomain);
		c.setName("c");
		fg2.addFactor(NoDups.INSTANCE, a, b);
		fg2.addFactor(NoDups.INSTANCE, b, c);
		
		a.setInput(0.0, .5, .5); // Not Moe
		b.setInput(.3, .5, .2);
		c.setInput(.7, .1, .2);
		
		LPSolverTestCase stoogeCase1 = new LPSolverTestCase(fg2);
		stoogeCase1.expectedConstraints = new String[] {
			"p(a=Larry) + p(a=Curly) = 1",
			"p(b=Moe) + p(b=Larry) + p(b=Curly) = 1",
			"p(c=Moe) + p(c=Larry) + p(c=Curly) = 1",
			
			"-p(a=Larry) + p(a=Larry,b=Moe) + p(a=Larry,b=Curly) = 0",
			"-p(a=Curly) + p(a=Curly,b=Moe) + p(a=Curly,b=Larry) = 0",
			"-p(b=Moe) + p(a=Larry,b=Moe) + p(a=Curly,b=Moe) = 0",
			"-p(b=Larry) + p(a=Curly,b=Larry) = 0",
			"-p(b=Curly) + p(a=Larry,b=Curly) = 0",
			
			"-p(b=Moe) + p(b=Moe,c=Larry) + p(b=Moe,c=Curly) = 0",
			"-p(b=Larry) + p(b=Larry,c=Moe) + p(b=Larry,c=Curly) = 0",
			"-p(b=Curly) + p(b=Curly,c=Moe) + p(b=Curly,c=Larry) = 0",
			"-p(c=Moe) + p(b=Larry,c=Moe) + p(b=Curly,c=Moe) = 0",
			"-p(c=Larry) + p(b=Moe,c=Larry) + p(b=Curly,c=Larry) = 0",
			"-p(c=Curly) + p(b=Moe,c=Curly) + p(b=Larry,c=Curly) = 0",
		};
		stoogeCase1.testLPState();
	}


	@Test
	@Ignore
	public void testGLPK()
	{
		DiscreteDomain booleanDomain = new DiscreteDomain(false,true);
		
		FactorGraph fg1 = new FactorGraph();
		Discrete x = new Discrete(booleanDomain);
		x.setName("x");
		Discrete y = new Discrete(booleanDomain);
		y.setName("y");
		Discrete z = new Discrete(booleanDomain);
		z.setName("z");

		fg1.addFactor(OnlyOneTrue.INSTANCE, x, y);
		fg1.addFactor(OnlyOneTrue.INSTANCE, y, z);
		fg1.addFactor(OnlyOneTrue.INSTANCE, x, z);
		
		x.setInput(.3, .7);
		y.setInput(.2, .8);
		z.setInput(.6, .3);

		SFactorGraph solver = new Solver().createFactorGraph(fg1);
		solver.setLPSolverName("GLPK");
		solver.solve();
	}
}
