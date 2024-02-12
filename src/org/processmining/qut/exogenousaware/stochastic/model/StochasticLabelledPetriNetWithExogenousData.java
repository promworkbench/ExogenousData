package org.processmining.qut.exogenousaware.stochastic.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.stochasticlabelledpetrinets.StochasticLabelledPetriNet;

import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;

public class StochasticLabelledPetriNetWithExogenousData implements StochasticLabelledPetriNet {
	
	private AcceptingPetriNet net;
	private Map<Transition, Integer> transitions;
	private Map<Place, Integer> places;
	private Map<Integer, int[]> input_transitions;
	private Map<Integer, int[]> output_transitions;
	private Map<Integer, int[]> input_places;
	private Map<Integer, int[]> output_places;
	private Map<Integer, Double> base_weights;
	private Map<Integer, Double[]> adjustments;
	private Map<Integer, Double[]> not_adjustments;
	private List<String> adjustment_names;
	private int noAdjuster;
	private Map<String, Integer> adjusters;
	
	public StochasticLabelledPetriNetWithExogenousData(AcceptingPetriNet net) {
//		setup variables
		this.net = net;
		this.noAdjuster = 0;
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
//		initialise the weights
		int indexer = 1;
		for(Transition trans : net.getNet().getTransitions()) {
			this.transitions.put(trans, indexer);
			this.base_weights.put(indexer, 1.0);
			this.adjustments.put(indexer, new Double[0]);
			this.not_adjustments.put(indexer, new Double[0]);
			indexer++;
		}
		indexer = 1;
		for(Place place : net.getNet().getPlaces()) {
			this.places.put(place, indexer);
			indexer++;
		}
//		find input/output for transitions
		for(Transition trans: net.getNet().getTransitions()) {
			Integer tranid = transitions.get(trans);
			Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> inedges = net.getNet().getInEdges(trans);
			Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> outedges = net.getNet().getOutEdges(trans);
			int[] inplaces = new int[inedges.size()];
			int[] outplaces = new int[outedges.size()];
			indexer = 0;
			for( PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : inedges) {
				inplaces[indexer] = this.places.get(edge.getSource());
				indexer++;
			}
			indexer = 0;
			for( PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : outedges) {
				outplaces[indexer] = this.places.get(edge.getTarget());
				indexer++;
			}
			this.input_transitions.put(tranid, inplaces);
			this.output_transitions.put(tranid, outplaces);
		}
		
		
	}
	
	public void setBaseWeight(Transition transition, double weight) {
		setBaseWeight(this.transitions.get(transition), weight);
	}
	
	public void setBaseWeight(int transition, double weight) {
		this.base_weights.put(transition, weight);
	}
	
	public void addExogenousFactor(String name) {
		if (adjustment_names.contains(name)) {
			adjustment_names.add(name);
			adjusters.put(name, noAdjuster);
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

	public int getNumberOfTransitions() {
		return this.net.getNet().getTransitions().size();
	}

	public int getNumberOfPlaces() {
		return this.net.getNet().getPlaces().size();
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
		for( Entry<Place, Integer> entry : places.entrySet()) {
			if (entry.getValue() == place) {
				return this.net.getInitialMarking() 
					   .contains(entry.getKey()) 
					   ? 1 : 0;
			}
		}
		throw new ValueException("transition id ("+place+") not known.");
	}

	public int[] getInputPlaces(int transition) {
		return this.input_transitions.get(transition);
	}

	public int[] getOutputPlaces(int transition) {
		return this.output_transitions.get(transition);
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

}
