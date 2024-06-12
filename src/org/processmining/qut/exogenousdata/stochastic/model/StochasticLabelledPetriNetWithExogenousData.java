package org.processmining.qut.exogenousdata.stochastic.model;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.IntStream;

import org.apache.commons.lang.StringEscapeUtils;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.basicstochasticminer.solver.Function;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.qut.exogenousdata.ab.jobs.Tuple;
import org.processmining.qut.exogenousdata.data.ExogenousDataset;
import org.processmining.qut.exogenousdata.stochastic.equalities.EqualitiesFactory.SLPNEDVarType;
import org.processmining.qut.exogenousdata.stochastic.equalities.EqualitiesFactory.SLPNEDVariable;
import org.processmining.qut.exogenousdata.stochastic.equalities.EqualitiesFactory.SLPNEDVariablePower;
import org.processmining.stochasticlabelledpetrinets.StochasticLabelledPetriNet;

import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;

public class StochasticLabelledPetriNetWithExogenousData implements StochasticLabelledPetriNet {
	
	private AcceptingPetriNet net;
	private Map<Transition, Integer> transitions;
	private Map<Place, Integer> places;
	private Map<String, Integer> datasets;
	private Map<Integer, int[]> input_transitions;
	private Map<Integer, int[]> output_transitions;
	private Map<Integer, int[]> input_places;
	private Map<Integer, int[]> output_places;
	private Map<Integer, Double> base_weights;
	private Map<Integer, Double[]> adjustments;
	private Map<Integer, Double[]> not_adjustments;
	private int[] initial_marking;
	private List<String> adjustment_names;
	
	public StochasticLabelledPetriNetWithExogenousData() {
//		setup variables
		prepareVars();
	}
	
	public StochasticLabelledPetriNetWithExogenousData(
			AcceptingPetriNet net,
			Map<Function, Double> solvedVariables,
			Collection<ExogenousDataset> datasets
	) throws Exception {
//		setup variables
		prepareVars(net, solvedVariables, datasets);
//		stardardise the ordering of datasets
		int indexer = 0;
		for(ExogenousDataset dataset : datasets) {
			String dname = dataset.getName();
			this.datasets.put(dname, indexer);
			this.adjustment_names.add(dname);
			indexer++;
		}
		System.out.println(this.datasets.toString());
//		initialise the weights
		int numPowers = indexer;
		indexer = 0;
		for(Transition trans : net.getNet().getTransitions()) {
			this.transitions.put(trans, indexer);
			this.base_weights.put(indexer, 1.0);
			this.adjustments.put(indexer, new Double[numPowers]);
			this.not_adjustments.put(indexer, new Double[numPowers]);
			indexer++;
		}
//		load in variables 
		for(Function var: solvedVariables.keySet()) {
			int varIndex =-1;
			int adjIndex =-1;
			SLPNEDVarType type;
			if (var instanceof SLPNEDVariable) {
				SLPNEDVariable slpnedVar = (SLPNEDVariable) var;
				varIndex = this.transitions.get(slpnedVar.getTransition());
				type = slpnedVar.getType();
				if (type != SLPNEDVarType.BASE) {
					adjIndex = this.datasets.get(slpnedVar.getDataset());
				}
			} else if (var instanceof SLPNEDVariablePower) {
				SLPNEDVariablePower slpnedVar = (SLPNEDVariablePower) var;
				varIndex = this.transitions.get(slpnedVar.getTransition());
				type = slpnedVar.getType();
				adjIndex = this.datasets.get(slpnedVar.getDataset());
			} else {
				throw new Exception("variable not of known type for constructing slpned.");
			}
			if (type == SLPNEDVarType.BASE) {
				this.base_weights.put(varIndex, solvedVariables.get(var));
			} else if (type == SLPNEDVarType.EXOADJ) {
				if (solvedVariables.get(var) != null) {
					this.adjustments.get(varIndex)[adjIndex] = solvedVariables.get(var);
				}
			} else if (type == SLPNEDVarType.NOTEXOADJ) {
				this.not_adjustments.get(varIndex)[adjIndex] = solvedVariables.get(var);
			}
		}
		indexer = 0;
		for(Place place : net.getNet().getPlaces()) {
			this.places.put(place, indexer);
			this.initial_marking[indexer] = net.getInitialMarking().contains(place) ? 1: 0;
			indexer++;
		}
//		find input/output for transitions
		for(Transition trans: net.getNet().getTransitions()) {
			Integer tranid = transitions.get(trans);
			Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> inedges = net.getNet().getInEdges(trans);
			Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> outedges = net.getNet().getOutEdges(trans);
			int[] inplaces = new int[this.initial_marking.length];
			int[] outplaces = new int[this.initial_marking.length];
			for( PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : inedges) {
				inplaces[this.places.get(edge.getSource())] = 1;
			}
			indexer = 0;
			for( PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : outedges) {
				outplaces[this.places.get(edge.getTarget())] = 1;
			}
			this.input_transitions.put(tranid, inplaces);
			this.output_transitions.put(tranid, outplaces);
		}
		
//		set up initial places
	}
	
	private void prepareVars() {
//		setup variables
		this.transitions = new HashMap();
		this.places = new HashMap();
		this.input_transitions = new HashMap();
		this.output_transitions = new HashMap();
		this.input_places = new HashMap();
		this.output_places = new HashMap();
		this.base_weights = new HashMap();
		this.adjustments = new HashMap();
		this.not_adjustments = new HashMap();
		this.adjustment_names = new ArrayList();
		this.datasets = new HashMap();
		this.initial_marking = new int[0];
	}
	
	private void prepareVars(AcceptingPetriNet net,
			Map<Function, Double> solvedVariables,
			Collection<ExogenousDataset> datasets) {
//		setup variables
		this.net = net;
		this.transitions = new HashMap();
		this.places = new HashMap();
		this.input_transitions = new HashMap();
		this.output_transitions = new HashMap();
		this.input_places = new HashMap();
		this.output_places = new HashMap();
		this.base_weights = new HashMap();
		this.adjustments = new HashMap();
		this.not_adjustments = new HashMap();
		this.adjustment_names = new ArrayList();
		this.datasets = new HashMap();
		this.initial_marking = new int[net.getNet().getPlaces().size()];
	}
	
	public Map<String, Integer> getDatasetIndexes() {
		return this.datasets;
	}
	
	public String getName() {
		return net.getNet().getLabel();
	}
	
	public double getBaseWeight(int transition) {
		return base_weights.get(transition);
	}
	
	public Map<String, Tuple<Double,Double>> getAdjustments(int transition){
		Map<String, Tuple<Double,Double>> ret = new HashMap();
		for(String adjuster : adjustment_names) {
			int index = datasets.get(adjuster);
			double xadjust = adjustments.get(transition)[index];
			double not_xadjust = not_adjustments.get(transition)[index];
			ret.put(adjuster, new Tuple(xadjust, not_xadjust));
		}
		return ret;
	}
	
	public double calcBaseWeight(int transition) {
		return getBaseWeight(transition);
	}
	
	public double calcWeight(int transition) {
		int[] known = new int[getNumberOfExogenousFactors()];
		double[] powers = new double[getNumberOfExogenousFactors()];
		return calcWeight(transition, powers, known);
	}
	
	public double calcWeight(int transition, double[] powers) {
		int[] known = new int[getNumberOfExogenousFactors()];
		Arrays.setAll(known, i -> 1);
		return calcWeight(transition, powers, known);
	}
	
	public double calcWeight(int transition, double[] powers, int[] known) {
		double weight = calcBaseWeight(transition);
		Map<String, Tuple<Double, Double>> adjusters = getAdjustments(transition);
		for(String adjuster : adjustment_names) {
			int x = datasets.get(adjuster);
			Tuple<Double, Double> adjustments = adjusters.get(adjuster);
			if (known[x] == 1) {
				weight *= Math.pow(adjustments.getLeft(),powers[x]);
			} else {
				weight *= adjustments.getRight();
			}
		}
		return weight;
	}
	
	public int getNumberOfExogenousFactors() {
		return this.adjustment_names.size();
	}
	
	public String getExogenousLabel(int factor) {
		for(String adjuster : adjustment_names) {
			int x = datasets.get(adjuster);
			if (x == factor) {
				return adjuster;
			}
		}
		return "unknown";
	}

	public int getNumberOfTransitions() {
		return this.transitions.values().size();
	}

	public int getNumberOfPlaces() {
		return this.initial_marking.length;
	}
	
	public int findTransitionId(Transition trans) {
		return transitions.get(trans);
	}

	public String getTransitionLabel(int transition) {
		for( Entry<Transition, Integer> entry : transitions.entrySet()) {
			if (entry.getValue() == transition) {
				return entry.getKey().getLabel();
			}
		}
		throw new ValueException("transition id ("+transition+") not known.");
	}

	public boolean isTransitionSilent(int transition) {
		for( Entry<Transition, Integer> entry : transitions.entrySet()) {
			if (entry.getValue() == transition) {
				return entry.getKey().isInvisible();
			}
		}
		throw new ValueException("transition id ("+transition+") not known.");
	}

	public int isInInitialMarking(int place) {
		if (place < initial_marking.length) {
			return initial_marking[place];
		}
		throw new ValueException("place id ("+place+") not known.");
	}

	public int[] getInputPlaces(int transition) {
		int [] iplaces = this.input_transitions.get(transition);
		return 	IntStream.rangeClosed(0, iplaces.length-1)
				.filter(i -> iplaces[i] > 0)
				.toArray();
	}

	public int[] getOutputPlaces(int transition) {
		int[] oplaces = this.output_transitions.get(transition);
		return 	IntStream.rangeClosed(0, oplaces.length-1)
				.filter(i -> oplaces[i] > 0)
				.toArray();
	}

	public int[] getInputTransitions(int place) {
		// TODO Auto-generated method stub
		return null;
	}

	public int[] getOutputTransitions(int place) {
		// TODO Auto-generated method stub
		return null;
	}

	public SLPNEDSemantics getDefaultSemantics() {
		return new SLPNEDSemantics(this);
	}
	
//	export functions
	public void exportNet(File outstream) throws IOException {
			PrintWriter w = null;
			try {
				w = new PrintWriter(outstream);
				
				w.println("# adjusters");
				w.println(adjustment_names.size());
				
				w.println("# adjusters");
				for(String adjuster : adjustment_names) {
					w.println(adjuster.replace("#", "&#35;"));
				}
				
				w.println("# number of places");
				w.println(getNumberOfPlaces());

				w.println("# initial marking");
				for (int place = 0; place < getNumberOfPlaces(); place++) {
					w.println(isInInitialMarking(place));
				}

				w.println("# number of transitions");
				w.println(getNumberOfTransitions());
				for (int transition = 0; transition < getNumberOfTransitions(); transition++) {
					w.println("# transition " + transition);
					if (isTransitionSilent(transition)) {
						w.println("silent");
					} else {
						w.println("label " + StringEscapeUtils.escapeJava(getTransitionLabel(transition).replace("#", "&#35;")));
					}
					w.println("# base weight ");
					w.println(getBaseWeight(transition));
					
					Map<String, Tuple<Double,Double>> adjusters = getAdjustments(transition);
					w.println("# adjustments for exogenous data");
					for(String adjuster : adjustment_names) {
						w.println(adjusters.get(adjuster).getLeft());
					}
					
					w.println("# adjustments without exogenous data");
					for(String adjuster : adjustment_names) {
						w.println(adjusters.get(adjuster).getRight());
					}

					w.println("# number of input places");
					w.println(getInputPlaces(transition).length);
					for (int place : getInputPlaces(transition)) {
						w.println(place);
					}

					w.println("# number of output places");
					w.println(getOutputPlaces(transition).length);
					for (int place : getOutputPlaces(transition)) {
						w.println(place);
					}
				}
			} finally {
				if (w != null) {
					w.close();
				}
			}
		}

//	import functions
	public void addPlace() {
		int newPlaceId = 1;
		if (places.values().size() > 0) {
			newPlaceId = places.values().stream().reduce(Integer::max).get()+1;
		}
		Place place = new Place("p"+newPlaceId, null);
		places.put(place, newPlaceId);
//		extend initial and final marking
		int[] old_initial = initial_marking;
		initial_marking = new int[newPlaceId];
		for(int i = 0; i < old_initial.length; i++) {
			initial_marking[i] = old_initial[i];
		}
	}
	
	public void addPlaceToInitial(int place) {
		initial_marking[place] = 1;
	}
	
	public void addTransition(double bweight, Double[] adjusters, Double[] notadjusters) {
		int newTransId = 0;
		if (transitions.values().size() > 0) {
			newTransId = transitions.values().stream().reduce(Integer::max).get()+1;
		}
		String label = "tau "+newTransId;
		addTransition(label, bweight, adjusters, notadjusters);
	}
	
	public void addTransition(String label, double bweight, Double[] adjusters, Double[] notadjusters) {
		int newTransId = 0;
		if (transitions.values().size() > 0) {
			newTransId = transitions.values().stream().reduce(Integer::max).get()+1;
		}
//		make new transition
		Transition trans = new Transition(label, null);
		trans.setInvisible(label.startsWith("tau"));
//		introduce transition
		this.transitions.put(trans, newTransId);
		this.base_weights.put(newTransId, bweight);
		this.adjustments.put(newTransId, adjusters);
		this.not_adjustments.put(newTransId, notadjusters);
		this.input_transitions.put(newTransId, new int[initial_marking.length]);
		this.output_transitions.put(newTransId, new int[initial_marking.length]);
	}
	
	public void addPlaceTransitionArc(int place ,int transition) {
		int[] iplaces = this.input_transitions.get(transition);
		iplaces[place] += 1;
	}
	
	public void addTransitionPlaceArc(int transition, int place) {
		int[] oplaces = this.output_transitions.get(transition);
		oplaces[place] += 1;
	}
	
	public void setBaseWeight(Transition transition, double weight) {
		setBaseWeight(this.transitions.get(transition), weight);
	}
	
	public void setBaseWeight(int transition, double weight) {
		this.base_weights.put(transition, weight);
	}
	
	public void addExogenousFactor(String name) {
		if (!adjustment_names.contains(name)) {
			adjustment_names.add(name);
			datasets.put(name, adjustment_names.size()-1);
//			adjust the lengths for adjusters
			for(int trans : transitions.values()) {
				Double[] old_adjustments = this.adjustments.get(trans);
				Double[] new_adjustments = new Double[old_adjustments.length+1];
				Double[] old_not_adjustments = this.not_adjustments.get(trans);
				Double[] new_not_adjustments = new Double[old_not_adjustments.length+1];
				for(int i=0; i < old_adjustments.length; i++) {
					new_adjustments[i] = old_adjustments[i];
					new_not_adjustments[i] = old_not_adjustments[i];
				}
				new_adjustments[old_adjustments.length] = 1.0;
				new_not_adjustments[old_not_adjustments.length] = 1.0;
				this.adjustments.replace(trans, new_adjustments);
				this.not_adjustments.replace(trans, new_not_adjustments);
			}
		}
	}
}
