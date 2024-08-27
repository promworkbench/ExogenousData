package org.processmining.qut.exogenousdata.stochastic.discovery;

import java.util.List;
import java.util.Map;

import org.processmining.basicstochasticminer.solver.Equation;
import org.processmining.basicstochasticminer.solver.Function;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.qut.exogenousdata.ab.jobs.Tuple;
import org.processmining.qut.exogenousdata.stochastic.solver.Solver;

public class SLPNEDDiscoveryWithContext extends SLPNEDDiscovery {
	
	protected UIPluginContext context = null;
	
	public SLPNEDDiscoveryWithContext() {
		super();
	}
	
	
	
	protected Map<Function, Double> solveEquations(Tuple<List<Equation>, List<Function>> equalities) {
		// TODO Auto-generated method stub
		Solver.CONTEXT = context;
		Solver.PROG = context.getProgress();
		Map<Function, Double> ret = super.solveEquations(equalities);
		Solver.CONTEXT = null;
		Solver.PROG = null;
		return ret;
	}



	public SLPNEDDiscoveryWithContext(UIPluginContext context) {
		super();
		this.context = context;
	}
	
	public void setContext(UIPluginContext newContext) {
		context = newContext;
	}
	
	protected void log(String message) {
		super.log(message);
		if (context != null) {
			context.log(message);
		}
	}
	
	protected void ProgSetVal(int val) {
		if (context != null) {
			context.getProgress().setValue(val);
		}
	}

	protected void ProgIncr() {
		if (context != null) {
			context.getProgress().inc();
		}
	}

	protected void ProgSetMax(int max) {
		if (context != null) {
			context.getProgress().setMaximum(max);
		}
	}

	protected int ProgGetMax() {
		if (context != null) {
			return context.getProgress().getMaximum();
		}
		return super.ProgGetMax();
	}
	
	

}
