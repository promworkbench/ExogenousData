package org.processmining.qut.exogenousdata.stochastic.equalities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.processmining.basicstochasticminer.solver.Constant;
import org.processmining.basicstochasticminer.solver.Division;
import org.processmining.basicstochasticminer.solver.Equation;
import org.processmining.basicstochasticminer.solver.Function;
import org.processmining.basicstochasticminer.solver.Product;
import org.processmining.basicstochasticminer.solver.Sum;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.qut.exogenousdata.ab.jobs.Tuple;
import org.processmining.qut.exogenousdata.data.ExogenousDataset;
import org.processmining.qut.exogenousdata.stochastic.choicedata.ChoiceDataPoint;
import org.processmining.qut.exogenousdata.stochastic.choicedata.ChoiceExogenousPoint;
import org.processmining.qut.exogenousdata.stochastic.equalities.Variables.SLPNEDVariable;
import org.processmining.qut.exogenousdata.stochastic.equalities.Variables.SLPNEDVariablePower;

public class EqualitiesGobalSumFactory {
	
	private EqualitiesGobalSumFactory() {};
	
	static public Transition global = new Transition("global-t", null);
	
	public static Tuple<List<Equation>,List<Function>> construct(
			Map<ChoiceDataPoint, Map<String,Integer>> frequencies,
			Collection<ExogenousDataset> datasets,
			Collection<Transition> transitions) {
//		make tmp storages
		List<Equation> equations = new ArrayList();
		List<Function> variables = new ArrayList();
		Map<String, Tuple<Function,Integer>> varlookup = new HashMap();
		VariableCounter vcounter = new VariableCounter();
//		initialise all equality variables
		prepareVariables(variables, varlookup, vcounter, datasets, transitions);
//		System.out.println("prepared variables...");
//		for each competition space, construct an equality between outcomes
		for(ChoiceDataPoint comp : frequencies.keySet()) {
//			if this space is about an enablement with only one option then skip
			if (comp.getEnabled().size() == 1) {
				continue;
			}
			Map<String, Integer> points = frequencies.get(comp);
			int totalPoints = points.values().stream().reduce(0,Integer::sum);
//			System.out.println("working on "+ comp.toString() + " with size of "+ totalPoints);
			if (totalPoints < 1) {
				continue;
			}
//			(a) if the competition space contains at least one observation for each enabled then,
//			one term can be removed
			if (points.keySet().size() == comp.getEnabled().size()) {
				int point = 1;
				for(Transition op : comp.getEnabled()) {
					String label = op.getId().toString();
					if (!points.containsKey(label)) {
						continue;
					}
					int subpoints = points.get(label);
					Equation eq = createTypeEquality(
							(subpoints * 1.0) / (totalPoints * 1.0), 
							subpoints, op, comp, varlookup, variables, vcounter
					);
//					System.out.println("(skipped term) constructed :: "+eq.toString());
					equations.add(eq);
					point += 1;
					if (point >= comp.getEnabled().size()) {
						break;
					}
				}
			}
			else {
//			(b) otherwise, we can drop all options that are not observed
//			(a + b) cannot be done, only one
				for(Transition op : comp.getEnabled()) {
					String label = op.getId().toString();
					if (!points.containsKey(label)) {
						continue;
					}
					int subpoints = points.get(label);
					Equation eq = createTypeEquality(
							(subpoints * 1.0) / (totalPoints * 1.0), 
							subpoints, op, comp, varlookup, variables, vcounter
					);
//					System.out.println("(zero removed) constructed :: "+eq.toString());
					equations.add(eq);
				}
			}
		}
		for (Equation eq : equations) {
//			System.out.println("Created equation of ::");
//			System.out.println("\t"+eq.getFunction().toLatex());
		}
		return new Tuple(equations, variables);
	}
	
	
	
	/*
	 * Handles preparing all known variables for the given datasets and transitions before building
	 * equalities.
	 */
	private static void prepareVariables(
			List<Function> variables, 
			Map<String, Tuple<Function, Integer>> varlookup,
			VariableCounter vcounter, 
			Collection<ExogenousDataset> datasets, 
			Collection<Transition> transitions) {
//		loop through transitions, creating for each one a base weight
		for(Transition trans : transitions) {
			String transName = trans.getId().toString().replace(" ", "_");
			createVarIfNeeded(String.format(Variables.BASE_WEIGHT, transName), varlookup, variables, vcounter, trans, null);
//			then for each dataset, create two adjusters
			for(ExogenousDataset dataset : datasets) {
				createVarIfNeeded(
						String.format(Variables.EXO_WEIGHT, 
								global.getId().toString().replace(" ",  "_"),
								dataset.getName().replace(" ", "_")), 
						varlookup, variables, vcounter, global, dataset.getName());
				createVarIfNeeded(
						String.format(Variables.NOT_EXO_WEIGHT, 
								global.getId().toString().replace(" ",  "_"), 
								dataset.getName().replace(" ", "_")),
						varlookup, variables, vcounter, global, dataset.getName());
			}
		}
//		debug out variables
		for(Function var : variables) {
//			System.out.println("created variable :: "+var.toString());
		}
		assert( variables.size() ==  datasets.size() * 2 + transitions.size());
		
	}
	
	public static Equation createTypeEquality(
			double sumsTo,
			int occurances,
			Transition fired,
			ChoiceDataPoint point,
			Map<String, Tuple<Function,Integer>> varlookup, 
			List<Function> variables, 
			VariableCounter vcounter) {
		
//		build top (always the same)
//		get functions for variables
		Function b1 = createVarIfNeeded(
				Variables.getBaseWeightName(fired),
				varlookup, variables, vcounter, fired, null);
		Function x1 = null;
		ChoiceExogenousPoint[] powers = point.getPowers();
		for(int i=0 ; i < powers.length; i++) {
			// for each xpower construct the following
			// [check] . power . xadj + [not check] . nxadj
//			cycle through adjustments
			ChoiceExogenousPoint power = powers[i];
			boolean knower = power.isKnown();
			String xadj = Variables.getExogenousAdjust(global, power);
			String nxadj = Variables.getNotExogenousAdjust(global, power);
			double xadjcheck = knower ? 1.0 : 0.0;
			double xadjpower = knower ? power.getValue() : 1.0;
			double nxadjcheck = knower ? 0.0 : 1.0;
			Function xadj_f = new Product(
					new Constant(xadjcheck),
					new Product(
						new Constant(xadjpower),
						createVarIfNeeded(xadj, varlookup, variables, vcounter, global ,power.getName(), 1.0)
					)
			);
			Function nxadj_f = new Product(
					new Constant(nxadjcheck),
					createVarIfNeeded(nxadj, varlookup, variables, vcounter, global ,power.getName(), 1.0)
			);
			Function adjuster = new Sum(xadj_f, nxadj_f);
			if (x1 == null) {
//				first adjustment
				x1 = adjuster;
			} else {
//				for every other make a chain of products
				x1 = new Sum(x1, adjuster);
			}
		}
//		top equation should be badj + a large sum over 
		Sum top = new Sum(b1 , x1);
//		build bottom (different)
		Sum bottom = null;
		for (Transition trans : point.getEnabled()) {
			String botBase = Variables.getBaseWeightName(trans);
			Function adjusters = null;
			for (int i=0; i < powers.length; i++) {
				ChoiceExogenousPoint power = powers[i];
				boolean knower = powers[i].isKnown();
				String xadj = Variables.getExogenousAdjust(global, power);
				String nxadj = Variables.getNotExogenousAdjust(global, power);
				double xadjcheck = knower ? 1.0 : 0.0;
				double xadjpower = knower ? power.getValue() : 1.0;
				double nxadjcheck = knower ? 0.0 : 1.0;
				Function xadj_f = new Product(
						new Constant(xadjcheck),
						new Product(
							new Constant(xadjpower),
							createVarIfNeeded(xadj, varlookup, variables, vcounter, global ,power.getName(), 1.0)
						)
				);
				Function nxadj_f = new Product(
						new Constant(nxadjcheck),
						createVarIfNeeded(nxadj, varlookup, variables, vcounter, global ,power.getName(), 1.0)
				);
				Function adjuster = new Sum(xadj_f, nxadj_f);
//				String ajname = knower ? 
//					getExogenousAdjust(trans, powers[i]) :
//					getNotExogenousAdjust(trans, powers[i]);
//				double apower = knower ? powers[i].getValue() : 1;
//				Function  adjuster = createVarIfNeeded(ajname, varlookup, variables, vcounter, trans, powers[i].getName(), apower );
				if (adjusters == null) {
					adjusters = adjuster;
				} else {
					adjusters = new Sum(adjusters, adjuster);
				}
			}
			if (bottom == null) {
				bottom = new Sum(
					new Sum( 
						createVarIfNeeded(botBase, varlookup, variables, vcounter, trans, null),
						adjusters
					)
				);
			} else {
				bottom = new Sum(bottom,
					new Sum( 
							createVarIfNeeded(botBase, varlookup, variables, vcounter, trans, null),
							adjusters
					)
				);
			}
			
		}
		Division right = new Division(top, bottom);
//		set value for right
		return new Equation(sumsTo, right, occurances);
	}	

	/*
	 * Helper to handle creating a constant variable counter.
	 */
	public static class VariableCounter {
			
			int count = 0;
			
			public VariableCounter() {
			}
			
			public int get() {
				return this.count;
			}
			
			public void incr() {
				this.count++;
			}
	}
	
	/**
	 * A function to create equality variables or look up an existing one.
	 * 
	 * @param name the name of variable being created/looked for
	 * @param lookup mapping of names to functions and their index in the solution vector
	 * @param storage a list of known variables
	 * @param var_count variable counter for making new variables
	 * @return a constant referenced variable to a power of 1.
	 */
	public static Function createVarIfNeeded(
			String varName, 
			Map<String, Tuple<Function,Integer>> varlookup, 
			List<Function> variables, 
			VariableCounter vcounter,
			Transition trans,
			String dname){
		return createVarIfNeeded(varName, varlookup, variables, vcounter, trans, dname, 1);
	}
	
	/**
	 * A function to create equality variables or look up an existing one with a new power.
	 * 
	 * @param varName the name of variable being created/looked for
	 * @param varlookup mapping of names to functions and their index in the solution vector
	 * @param variables a list of known variables
	 * @param vcounter variable counter for making new variables
	 * @param power the power of the returned variable.
	 * @return a constant referenced variable to some power.
	 */
	public static Function createVarIfNeeded(
			String varName, 
			Map<String, Tuple<Function,Integer>> varlookup, 
			List<Function> variables, 
			VariableCounter vcounter,
			Transition trans,
			String dname,
			double power) {
		Function out;
		
		if (varlookup.containsKey(varName)) {
			Function old = varlookup.get(varName).getLeft();
			if (power != 1) {
				out = new SLPNEDVariablePower(
						varlookup.get(varName).getRight(), 
						varName, 
						power, 
						trans,
						dname
				);
			} else {
				out = new SLPNEDVariable(
						varlookup.get(varName).getRight(), 
						varName,
						trans,
						dname);
			}
		} else {
			if (power != 1) {
			out = new SLPNEDVariablePower(vcounter.get(), varName, power, trans, dname);
			} else {
				out = new SLPNEDVariable(vcounter.get(), varName, trans, dname);
			}
			varlookup.put(varName, new Tuple<>(out, vcounter.get()));
			variables.add(out);
			vcounter.incr();
		}
		
		return out;
	}
}
