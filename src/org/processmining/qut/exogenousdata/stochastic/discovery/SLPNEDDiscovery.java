package org.processmining.qut.exogenousdata.stochastic.discovery;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.basicstochasticminer.solver.Equation;
import org.processmining.basicstochasticminer.solver.Function;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.qut.exogenousdata.ab.jobs.Tuple;
import org.processmining.qut.exogenousdata.data.ExogenousAnnotatedLog;
import org.processmining.qut.exogenousdata.data.ExogenousDataset;
import org.processmining.qut.exogenousdata.stochastic.choicedata.ChoiceCollector;
import org.processmining.qut.exogenousdata.stochastic.choicedata.ChoiceDataPoint;
import org.processmining.qut.exogenousdata.stochastic.choicedata.ChoiceExogenousPoint;
import org.processmining.qut.exogenousdata.stochastic.equalities.EqualitiesFactory;
import org.processmining.qut.exogenousdata.stochastic.model.SLPNEDSemantics;
import org.processmining.qut.exogenousdata.stochastic.model.StochasticLabelledPetriNetWithExogenousData;
import org.processmining.qut.exogenousdata.stochastic.solver.Solver;

import cern.colt.Arrays;

public class SLPNEDDiscovery {
	
	private static String dumpLoc = "";
	
	public static void setDumpLoc(String loc) {
		dumpLoc = loc;
	}
	
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
		choiceData = null;
//		System.out.println("constructed immediate choice representation");
		System.out.println("sending choice data to equalities factory...");
		Tuple<List<Equation>,List<Function>> equalities = 
				EqualitiesFactory.construct( 
						frequencies, 
						datasets,
						net.getNet().getTransitions()
				);
		System.out.println("constructed equalities...");
		System.out.println("constructed "+equalities.getLeft().size()+" equalities....");
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
		System.out.println("but not before exporting visualisation data.");
		dumpSampleData(frequencies, outnet);
		System.out.println("finished exporting sample data.");
		return outnet;
	}

	public static void dumpSampleData(
			Map<ChoiceDataPoint, Map<String,Integer>> frequencies,
			StochasticLabelledPetriNetWithExogenousData outnet
		) throws FileNotFoundException {
		
		
		
		PrintWriter w = new PrintWriter(
				new File(dumpLoc + outnet.getName() + ".datadum")
		);
		
		w.println("comp,fired,powers,real,bprob,prob,freq");
		
		for( ChoiceDataPoint p : frequencies.keySet()) {
			SLPNEDSemantics sem = outnet.getDefaultSemantics();
			for (Transition fired : p.getFiringSeq()) {
				sem.executeTransition(outnet.findTransitionId(fired));
			}
			String line = "";
			BitSet enabled = sem.getEnabledTransitions();
			boolean hasTau = false;
			for(int trans = 0;  trans < sem.getNumberOfTransitions(); trans++) {
				if (enabled.get(trans)) {
					if (!sem.isTransitionSilent(trans)) {
						line += sem.getTransitionLabel(trans) + "|";
					} else {
						hasTau = true;
					}
					
				}
			}
			if (hasTau) {
				line += "tau|";
			}
			line = line.substring(0, line.length()-1);
			line += ",";
			
			double baseTotal = 0.0;
			for(Transition fired : p.getEnabled()) {
				baseTotal += outnet.calcBaseWeight(outnet.findTransitionId(fired));
			}
			
			double[] powers = new double[p.getPowers().length];
			int[] known = new int[p.getPowers().length];
			int powerIndex = 0;
			for (ChoiceExogenousPoint power : p.getPowers()) {
				powers[powerIndex] = power.getValue();
				known[powerIndex] = power.isKnown() ? 1 :0;
				powerIndex++;
			}
			double weightTotal = sem.getTotalWeightOfEnabledTransitions(powers, known);
			Map<String, Integer> pmap = frequencies.get(p);
			for(Transition fired : p.getEnabled()) {
				
				String label = fired.isInvisible() ? "tau" : fired.getLabel();
				String subline = label+",";
				
				subline += Arrays.toString(p.getPowers()).replace(",",";")+",";
				
				subline += fired.equals(p.getFired()) ? "true," : "false,";
				
				subline += Double.toString(outnet.calcBaseWeight(outnet.findTransitionId(fired)) / baseTotal) + ",";
				double likelihood = sem.getTransitionWeight(outnet.findTransitionId(fired), powers, known)
						/ weightTotal;
//				System.out.println("[SamplerExporter] firing likelihood :: "+likelihood);
				subline += Double.toString(likelihood) + ",";
				
				if (pmap.containsKey(fired.getId().toString())) {
					subline += Integer.toString(pmap.get(fired.getId().toString()));
				} else {
					subline += "" + 0 + "";
				}
				
				w.println(line+subline);
			}
			
			if (IntStream.of(known).reduce(0, Integer::sum) >= 1) {
				for (int flip  : IntStream.rangeClosed(0, known.length-1)
					.filter(i -> known[i] == 1)
					.toArray()) {
					known[flip] = 0;
					for(Transition fired : p.getEnabled()) {
						String label = fired.isInvisible() ? "tau" : fired.getLabel();
						String subline = label+",";
						
						subline += "[";
						for(int i : IntStream.rangeClosed(0, known.length-1).toArray()) {
							if (known[i] == 1) {
								subline += Double.toString(powers[i]) +";";
							} else {
								subline +="?;";
							}
						}
						subline = subline.substring(0, subline.length()-1);
						subline += "],";
						
						subline += "false,";
						
						subline += Double.toString(outnet.calcBaseWeight(outnet.findTransitionId(fired)) / baseTotal) + ",";
						
						weightTotal = sem.getTotalWeightOfEnabledTransitions(powers, known);
						double likelihood = sem.getTransitionWeight(outnet.findTransitionId(fired), powers, known)
								/ weightTotal;
//						System.out.println("[SamplerExporter] firing likelihood :: "+likelihood);
						subline += 
								Double.toString(
										likelihood
								) + ",";
						
						if (pmap.containsKey(fired.getId().toString())) {
							subline += Integer.toString(pmap.get(fired.getId().toString()));
						} else {
							subline += "" + 0 + "";
						}
						
						w.println(line+subline);
					}
				}
			}
		}
		
		w.close();
	}

}
