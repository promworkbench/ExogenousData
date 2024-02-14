package org.processmining.qut.exogenousaware.stochastic.equalities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.processmining.basicstochasticminer.solver.Division;
import org.processmining.basicstochasticminer.solver.Equation;
import org.processmining.basicstochasticminer.solver.Function;
import org.processmining.basicstochasticminer.solver.Product;
import org.processmining.basicstochasticminer.solver.Sum;
import org.processmining.basicstochasticminer.solver.Variable;
import org.processmining.basicstochasticminer.solver.VariablePower;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.qut.exogenousaware.ab.jobs.Tuple;
import org.processmining.qut.exogenousaware.data.ExogenousDataset;
import org.processmining.qut.exogenousaware.stochastic.choicedata.ChoiceDataPoint;
import org.processmining.qut.exogenousaware.stochastic.choicedata.ChoiceExogenousPoint;

public class EqualitiesFactory {
	
	private EqualitiesFactory() {};
	
	public static Tuple<List<Equation>,List<Function>> construct(
			Map<ChoiceDataPoint, List<ChoiceDataPoint>> choiceData,
			Collection<ExogenousDataset> datasets,
			Collection<Transition> transitions) {
//		make tmp storages
		List<Equation> equations = new ArrayList();
		List<Function> variables = new ArrayList();
		Map<String, Tuple<Function,Integer>> varlookup = new HashMap();
		VariableCounter vcounter = new VariableCounter();
//		initialise all equality variables
		prepareVariables(variables, varlookup, vcounter, datasets, transitions);
		System.out.println("prepared variables...");
//		for each competition space, construct an equality between outcomes
		for(ChoiceDataPoint comp : choiceData.keySet()) {
			List<ChoiceDataPoint> points = choiceData.get(comp);
			System.out.println("working on "+ comp.toString() + " with size of "+ points.size());
			for(Transition op : comp.getEnabled()) {
				List<ChoiceDataPoint> subpoints = points
						.stream()
						.filter(x -> {return x.getFired() == op;})
						.collect(Collectors.toList());
				if (subpoints.isEmpty()) {
					continue;
				}
				Equation eq = createTypeEquality(
						(subpoints.size() * 1.0) / (points.size() * 1.0), 
						subpoints.size(), subpoints.get(0), varlookup, variables, vcounter
				);
				System.out.println("constructed :: "+eq.toString());
				equations.add(eq);
			}
		}
		return new Tuple(equations, variables);
	}
	
	public static String getBaseWeightName(Transition trans) {
		String transName = trans.getLabel().toString().replace(" ", "_");
		return String.format(BASE_WEIGHT, transName);
	}
	
	public static String getExogenousAdjust(Transition trans, ChoiceExogenousPoint point) {
		String transName = trans.getLabel().toString().replace(" ", "_");
		return String.format(EXO_WEIGHT, transName, point.getName().replace(" ", "_"));
	}
	
	public static String getNotExogenousAdjust(Transition trans, ChoiceExogenousPoint point) {
		String transName = trans.getLabel().toString().replace(" ", "_");
		return String.format(NOT_EXO_WEIGHT, transName, point.getName().replace(" ", "_"));
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
			String transName = trans.getLabel().toString().replace(" ", "_");
			createVarIfNeeded(String.format(BASE_WEIGHT, transName), varlookup, variables, vcounter, trans);
//			then for each dataset, create two adjusters
			for(ExogenousDataset dataset : datasets) {
				createVarIfNeeded(
						String.format(EXO_WEIGHT, transName, dataset.getName().replace(" ", "_")), 
						varlookup, variables, vcounter, trans);
				createVarIfNeeded(
						String.format(NOT_EXO_WEIGHT, transName, dataset.getName().replace(" ", "_")),
						varlookup, variables, vcounter, trans);
			}
		}
//		debug out variables
		for(Function var : variables) {
			System.out.println("created variable :: "+var.toString());
		}
		
	}
	
	public static Equation createTypeEquality(
			double sumsTo,
			int occurances,
			ChoiceDataPoint point,
			Map<String, Tuple<Function,Integer>> varlookup, 
			List<Function> variables, 
			VariableCounter vcounter) {
		
//		build top (always the same)
//		get functions for variables
		Function b1 = createVarIfNeeded(
				getBaseWeightName(point.getFired()),
				varlookup, variables, vcounter, point.getFired());
		Function x1 = null;
		ChoiceExogenousPoint[] powers = point.getPowers();
		for(int i=0 ; i < powers.length; i++) {
//			cycle through adjustments
			ChoiceExogenousPoint power = powers[i];
			boolean knower = power.isKnown();
			String ajname = knower ?
				getExogenousAdjust(point.getFired(), power) :
				getNotExogenousAdjust(point.getFired(), power);
			double ajpower = knower ? power.getValue() : 1;
			Function adjuster = createVarIfNeeded(ajname, varlookup, variables, vcounter, point.getFired() ,ajpower);
			if (x1 == null) {
//				first adjustment
				x1 = adjuster;
			} else {
//				for every other make a chain of products
				x1 = new Product(x1, adjuster);
			}
		}
		Product top = new Product(b1 , x1);
//		build bottom (different)
		Sum bottom = null;
		for (Transition trans : point.getEnabled()) {
			String botBase = getBaseWeightName(trans);
			Function adjusters = null;
			for (int i=0; i < powers.length; i++) {
				boolean knower = powers[i].isKnown();
				String ajname = knower ? 
					getExogenousAdjust(trans, powers[i]) :
					getNotExogenousAdjust(trans, powers[i]);
				double apower = knower ? powers[i].getValue() : 1;
				Function  adjuster = createVarIfNeeded(ajname, varlookup, variables, vcounter, trans, apower);
				if (adjusters == null) {
					adjusters = adjuster;
				} else {
					adjusters = new Product(adjusters, adjuster);
				}
			}
			if (bottom == null) {
				bottom = new Sum(new Product( 
						createVarIfNeeded(botBase, varlookup, variables, vcounter, trans),
						adjusters
				));
			} else {
				bottom = new Sum(bottom,
						new Product( 
								createVarIfNeeded(botBase, varlookup, variables, vcounter, trans),
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
			Transition trans){
		return createVarIfNeeded(varName, varlookup, variables, vcounter, trans, 1);
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
			double power) {
		Function out;
		
		if (varlookup.containsKey(varName)) {
			Function old = varlookup.get(varName).getLeft();
			if (power != 1) {
				out = new SLPNEDVariablePower(
						varlookup.get(varName).getRight(), 
						varName, 
						power, 
						trans
				);
			} else {
				out = new SLPNEDVariable(
						varlookup.get(varName).getRight(), 
						varName,
						trans);
			}
		} else {
			if (power != 1) {
			out = new SLPNEDVariablePower(vcounter.get(), varName, power, trans);
			} else {
				out = new SLPNEDVariable(vcounter.get(), varName, trans);
			}
			varlookup.put(varName, new Tuple<>(out, vcounter.get()));
			variables.add(out);
			vcounter.incr();
		}
		
		return out;
	}
	
	/**
	 * Minor wrapper to keep track of the transition that the variable relates to.
	 * @author Adam Banham
	 */
	public static class SLPNEDVariablePower extends VariablePower {
		
		private Transition trans;
		
		public SLPNEDVariablePower(int parameterIndex, String name, double power, Transition trans) {
			super(parameterIndex, name, power);
			this.trans = trans;
		}
		
		public Transition getTransition() {
			return this.trans;
		}
	}
	
	public static class SLPNEDVariable extends Variable {
		
		private Transition trans;

		public SLPNEDVariable(int parameterIndex, String name, Transition trans) {
			super(parameterIndex, name);
			this.trans = trans;
		}
		
		public Transition getTransition() {
			return this.trans;
		}
	}
	
//	String templates
	public static final String BASE_WEIGHT = "BW_%s";
	public static final String EXO_WEIGHT = "AX_%s_%s";
	public static final String NOT_EXO_WEIGHT = "NAX_%s_%s";

}
