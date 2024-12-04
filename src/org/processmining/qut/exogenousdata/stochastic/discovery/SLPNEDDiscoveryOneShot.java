package org.processmining.qut.exogenousdata.stochastic.discovery;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
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
import org.processmining.framework.util.ui.widgets.helper.UserCancelledException;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.qut.exogenousdata.ab.jobs.Tuple;
import org.processmining.qut.exogenousdata.data.ExogenousAnnotatedLog;
import org.processmining.qut.exogenousdata.data.ExogenousDataset;
import org.processmining.qut.exogenousdata.steps.slicing.data.SubSeries.Scaling;
import org.processmining.qut.exogenousdata.stochastic.choicedata.ChoiceCollector;
import org.processmining.qut.exogenousdata.stochastic.choicedata.ChoiceDataPoint;
import org.processmining.qut.exogenousdata.stochastic.choicedata.ChoiceExogenousPoint;
import org.processmining.qut.exogenousdata.stochastic.equalities.EqualitiesGobalSumFactory;
import org.processmining.qut.exogenousdata.stochastic.equalities.EqualitiesProdFactory;
import org.processmining.qut.exogenousdata.stochastic.equalities.EqualitiesSumFactory;
import org.processmining.qut.exogenousdata.stochastic.model.SLPNEDSemantics;
import org.processmining.qut.exogenousdata.stochastic.model.StochasticLabelledPetriNetWithExogenousData;
import org.processmining.qut.exogenousdata.stochastic.model.StochasticLabelledPetriNetWithExogenousData.WeightForm;
import org.processmining.qut.exogenousdata.stochastic.solver.Solver;

import nl.tue.astar.AStarException;

public class SLPNEDDiscoveryOneShot implements SLPNEDDiscoverer {
	
	public final static double DEFAULT_ROUNDING = 1e-2;
	public final static int DEFAULT_BATCH = 1000;
	public final static Scaling DEFAULT_TIME_SCALE = Scaling.hour;
	public final static double DEFAULT_SOLVING_VALUE = 2.0;
	public final static WeightForm DEFAULT_FORM = WeightForm.INDIVMUT;
	
	
	protected String dumpLoc = "";
	protected boolean shouldDump = false;
	protected int batchsize = DEFAULT_BATCH;
	protected double defaultSolveValue = DEFAULT_SOLVING_VALUE;
	protected double rounding = DEFAULT_ROUNDING;
	protected Scaling timeScaling = DEFAULT_TIME_SCALE;
	protected WeightForm form = DEFAULT_FORM;
	
	public void configure(double rounding) {
		this.configure(rounding, DEFAULT_BATCH, 
				DEFAULT_TIME_SCALE,	DEFAULT_SOLVING_VALUE);
	}
	
	public void configure(double rounding, int batchsize) {
		this.configure(rounding, batchsize, 
				DEFAULT_TIME_SCALE,	DEFAULT_SOLVING_VALUE);
	}
	
	public void configure(double rounding, int batchsize,
			Scaling timeScaling) {
		this.configure(rounding, batchsize, 
				timeScaling, DEFAULT_SOLVING_VALUE);
	}
	
	public void configure(double rounding, int batchsize, 
			Scaling timeScaling,
			double defaultParameterValue) {
		this.configure(rounding, batchsize, 
				timeScaling, defaultParameterValue,
				DEFAULT_FORM);
	}
	
	public void configure(double rounding, int batchsize, 
			Scaling timeScaling,
			double defaultParameterValue,
			WeightForm form) {
		this.rounding = rounding;
		this.batchsize = batchsize;
		this.defaultSolveValue = defaultParameterValue;
		this.timeScaling = timeScaling;
		this.form = form;
	}

	public void setDumpLoc(String loc) {
		dumpLoc = loc;
		shouldDump = true;
	}
	
	public StochasticLabelledPetriNetWithExogenousData discoverFromLog(
			ExogenousAnnotatedLog xlog, 
			AcceptingPetriNet net) throws Exception {
		return discover(xlog.getEndogenousLog(), xlog.getExogenousDatasets(), net);
	}
	
	public StochasticLabelledPetriNetWithExogenousData discover(
			XLog xlog,
			List<ExogenousDataset> datasets,
			AcceptingPetriNet net) 
	throws Exception
	{
		ProgSetVal(0);
		ProgSetMax(4 + (shouldDump ? 1:0));
//		collect choice data
		log("starting discovery of slpned");
		Map<ChoiceDataPoint, Map<String,Integer>> frequencies = collectChoiceData(
				xlog, datasets, net);
		ProgIncr();
//		System.out.println("constructed immediate choice representation");
		log("sending choice data to equalities factory...");
		Tuple<List<Equation>,List<Function>> equalities = 
				constructEquations( 
						frequencies, 
						datasets,
						net
		);
		ProgIncr();
		log("constructed equalities...");
		log("constructed "+equalities.getLeft().size()+" equalities....");
		Map<Function, Double> solvedVariables = solveEquations(equalities);
		ProgIncr();
		log("creating slpned...");
		StochasticLabelledPetriNetWithExogenousData outnet = 
				makeNet(net, solvedVariables, datasets);
		ProgIncr();
		log("returning slpned...");
		if (shouldDump) {
			diagonsticDump(frequencies, equalities, solvedVariables, outnet);
			ProgIncr();
		}
		return outnet;
	}

	/**
	 * 
	 * @return
	 * @throws UserCancelledException 
	 * @throws AStarException 
	 */
	protected Map<ChoiceDataPoint, Map<String,Integer>> collectChoiceData(
			XLog xlog,
			List<ExogenousDataset> datasets,
			AcceptingPetriNet net			
			) throws AStarException, UserCancelledException {
		log("building choice data...");
		Iterator<ChoiceDataPoint> choiceData = ChoiceCollector.collect(
				xlog, datasets, net, 
				ChoiceCollector.ChoiceCollectorParameters
				.builder()
				.rounding(rounding)
				.timeScaling(timeScaling)
				.build());
		Map<ChoiceDataPoint, Map<String,Integer>> ret = new HashMap();
		log("built lazy iterator for choice data...");
		while( choiceData.hasNext()) {
			ChoiceDataPoint point = choiceData.next();
			String label = point.getFired().getId().toString();
			if (ret.containsKey(point)) {
				Map<String, Integer> points = ret.get(point);
				
				if (points.containsKey(label)) {
					points.put(label, points.get(label)+1);
				} else {
					points.put(label, 1);
				}
			} else {
				Map<String, Integer> newMap = new HashMap();
				newMap.put(label, 1);
				ret.put(point, newMap);
			}
		}
		log("finished lazy iterator for choice data...");
		return ret;
	}
	
	protected Tuple<List<Equation>,List<Function>> constructEquations(
			Map<ChoiceDataPoint, Map<String,Integer>> frequencies,
			List<ExogenousDataset> datasets,
			AcceptingPetriNet net) {
		if (this.form.equals(WeightForm.INDIVMUT)) {
			return EqualitiesProdFactory.construct( 
					frequencies, 
					datasets,
					net.getNet().getTransitions()
			);
		} else if (this.form.equals(WeightForm.INDIVADD)) {
			return EqualitiesSumFactory.construct( 
					frequencies, 
					datasets,
					net.getNet().getTransitions()
			);
		} else if (this.form.equals(WeightForm.GLOBALADD)) {
			return EqualitiesGobalSumFactory.construct( 
					frequencies, 
					datasets,
					net.getNet().getTransitions()
			);
		} else {
//			shouldn't get here
			return null;
		}
		
	}
	
	protected Map<Function, Double> solveEquations(
			Tuple<List<Equation>,List<Function>> equalities) {
		log("setting up variable matrixes...");
		int[] fixed = new int[equalities.getRight().size()];
		int[] nonzero = new int[equalities.getRight().size()];
		double[] inital = new double[equalities.getRight().size()];
		for(int i = 0; i < equalities.getRight().size(); i++) {
				fixed[i] = 0; // should the variable not change
				inital[i] =  defaultSolveValue; // the initial guess for solver
				nonzero[i] = 1; // should the variable not be zero
			
		}
		log("sending first-shot equations to solver...");
		double[] solvedvalues = Solver.solve(
					equalities.getLeft(), 
					equalities.getRight().size(),
					fixed, nonzero, inital);
			
		log("finished one-shot solution to equations");
		Map<Function, Double> solvedVariables = new HashMap();
		for (int i =0; i < equalities.getRight().size(); i++) {
			log("Solved variable : "
					+ equalities.getRight().get(i) 
					+ " as : "+solvedvalues[i]
			);
			if (Math.abs(solvedvalues[i] - defaultSolveValue) < 1e-4) {
				solvedVariables.put(equalities.getRight().get(i), 1.0);
			} else {
				solvedVariables.put(equalities.getRight().get(i), solvedvalues[i]);
			}
			
		}
		return solvedVariables;
	}
	
	protected StochasticLabelledPetriNetWithExogenousData makeNet(
			AcceptingPetriNet net, Map<Function, Double> solution,
			List<ExogenousDataset> datasets) throws Exception {
		return new 
				StochasticLabelledPetriNetWithExogenousData(
					net, solution, 
					datasets, form
				);
	}
	
	protected void diagonsticDump(
			Map<ChoiceDataPoint, Map<String,Integer>> frequencies,
			Tuple<List<Equation>,List<Function>> equalities,
			Map<Function, Double> solution,
			StochasticLabelledPetriNetWithExogenousData outnet
		) {
		log("but not before exporting visualisation data.");
		try {
			dumpSampleData(frequencies, outnet);
			log("finished exporting sample data.");
		} catch (Exception e) {
			log("failed to perform diagonstic dump.");
		}
	}
	
	
//	dump state for visualisation 
	public void dumpSampleData(
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
			boolean added = false;
			for(int trans = 0;  trans < sem.getNumberOfTransitions(); trans++) {
				if (enabled.get(trans)) {
					if (!sem.isTransitionSilent(trans)) {
						line += sem.getTransitionLabel(trans) + "|";
						added = true;
					} else {
						hasTau = true;
					}
					
				}
			}
			if (hasTau) {
				line += "tau|";
				added= true;
			}
			if (added) {
				line = line.substring(0, line.length()-1);
			}
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
	
	
//	helper functions to handle progress to UI or logging
	
	protected void log(String message) {
		System.out.println("[SLPNED-discovery-one-shot] "+message);
	}
	
	protected void ProgSetVal(int val) {
		
	}
	
	protected void ProgIncr() {
		
	}
	
	protected void ProgSetMax(int max) {
		
	}
	
	protected int ProgGetMax() {
		return 0;
	}
	
	public SLPNEDDiscoverer clone() {
		SLPNEDDiscoveryOneShot miner = new SLPNEDDiscoveryOneShot();
		miner.configure(rounding, batchsize,
				timeScaling, defaultSolveValue,
				form
		);
		return miner;
	}

}
