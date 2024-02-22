package org.processmining.qut.exogenousaware.stochastic.discovery;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.basicstochasticminer.solver.Equation;
import org.processmining.basicstochasticminer.solver.Function;
import org.processmining.qut.exogenousaware.ab.jobs.Tuple;
import org.processmining.qut.exogenousaware.data.ExogenousAnnotatedLog;
import org.processmining.qut.exogenousaware.data.ExogenousDataset;
import org.processmining.qut.exogenousaware.stochastic.choicedata.ChoiceCollector;
import org.processmining.qut.exogenousaware.stochastic.choicedata.ChoiceDataPoint;
import org.processmining.qut.exogenousaware.stochastic.equalities.EqualitiesFactory;
import org.processmining.qut.exogenousaware.stochastic.model.StochasticLabelledPetriNetWithExogenousData;
import org.processmining.qut.exogenousaware.stochastic.solver.Solver;

public class SLPNEDDiscovery {
	
	public static StochasticLabelledPetriNetWithExogenousData discoverFromLog(
			ExogenousAnnotatedLog xlog, 
			AcceptingPetriNet net) throws Exception {
		return discover(xlog.getEndogenousLog(), xlog.getExogenousDatasets(), net);
	}
	
	public static StochasticLabelledPetriNetWithExogenousData discover(
			XLog xlog,
			List<ExogenousDataset> datasets,
			AcceptingPetriNet net) 
	throws Exception
	{
//		collect choice data
		System.out.println("building choice data...");
		Iterator<ChoiceDataPoint> choiceData = ChoiceCollector.collect(xlog, datasets, net, 
				ChoiceCollector.ChoiceCollectorParameters.builder().build());
		System.out.println("built lazy iterator for choice data...");
		Map<ChoiceDataPoint, Map<String,Integer>> frequencies = new HashMap();
		while( choiceData.hasNext()) {
			ChoiceDataPoint point = choiceData.next();
			String label = point.getFired().getId().toString();
			if (frequencies.containsKey(point)) {
				Map<String, Integer> points = frequencies.get(point);
				
				if (points.containsKey(label)) {
					points.put(label, points.get(label)+1);
				} else {
					points.put(label, 1);
				}
			} else {
				Map<String, Integer> newMap = new HashMap();
				newMap.put(label, 1);
				frequencies.put(point, newMap);
			}
		}
		System.out.println("constructed immediate choice representation");
		System.out.println(choiceData.toString());
		System.out.println("sending choice data to equalities factory...");
		Tuple<List<Equation>,List<Function>> equalities = 
				EqualitiesFactory.construct( 
						frequencies, 
						datasets,
						net.getNet().getTransitions()
				);
		System.out.println("constructed equalities...");
		int[] fixed = new int[equalities.getRight().size()];
		int[] nonzero = new int[equalities.getRight().size()];
		double[] inital = new double[equalities.getRight().size()];
		for(int i = 0; i < equalities.getRight().size(); i++) {
			fixed[i] = 0;
			inital[i] =  1.0;			
			nonzero[i] = 1;
		}
		System.out.println("solving equalities...");
		double[] solvedvalues = Solver.solve(equalities.getLeft(), equalities.getRight().size(),
				fixed, nonzero, inital);
		System.out.println("equalities solved...");
		Map<Function, Double> solvedVariables = new HashMap();
		for (int i =0; i < equalities.getRight().size(); i++) {
			System.out.println("Solved variable : "+ equalities.getRight().get(i) + " as : "+solvedvalues[i]);
			solvedVariables.put(equalities.getRight().get(i), solvedvalues[i]);
		}
		System.out.println("creating slpned...");
		StochasticLabelledPetriNetWithExogenousData outnet = 
				new StochasticLabelledPetriNetWithExogenousData(net, solvedVariables, datasets);
		System.out.println("returning slpned...");
		return outnet;
	}

}
