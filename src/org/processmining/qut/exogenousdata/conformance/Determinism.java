package org.processmining.qut.exogenousdata.conformance;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import org.processmining.datapetrinets.expression.GuardExpression;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PNWDTransition;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PetriNetWithData;

import com.google.gwt.dev.util.collect.HashSet;

/*
 * Computes the determinism of a Petri with guards, which may be a Petri net 
 * with data.
 * Determinism computes the portion of transitions that are in the postset of
 * places with at least two outgoing arcs and have a non-trivial guard 
 * associated with them.
 */
public class Determinism {
	
	private Determinism() {};
	
	/**
	* Determinism computes the portion of transitions that are in the postset of
	* places with at least two outgoing arcs and have a non-trivial guard 
	* associated with them.
	*/
	public static double ComputeDeterminism(PetriNetWithData net) {
		double ret = 1.0;
		Set<PNWDTransition> transitions = new HashSet<>();
//		go get all the transitions in the postsets of places with many outgoing
//		arcs
		for( Place place: net.getPlaces()) {
			Collection<PetrinetEdge<
				? extends PetrinetNode,
				? extends PetrinetNode>
			> out = net.getOutEdges(place);
			if (out.size()> 1) {
				for(PetrinetEdge<
						? extends PetrinetNode,
						? extends PetrinetNode
					> edge: out) {
					PetrinetNode node = edge.getTarget();
					if (node instanceof PNWDTransition) {
						PNWDTransition trans = (PNWDTransition) node;
						transitions.add(trans);
					}
				}
			}
		}
//		check the guards of transitions
		Set<PNWDTransition> noguards = new HashSet<>();
		for(PNWDTransition trans: transitions) {
			if (isGuardTrivial(trans)) {
				noguards.add(trans);
			} 
		}
//		compute the portion
		ret = transitions.size() - noguards.size();
		ret = ret / transitions.size();
		return ret;
	}
	
	/**
	* Computes local Determinism for the given place.
	* Determinism the portion of transitions that are in the postset of
	* places with at least two outgoing arcs and have a non-trivial guard 
	* associated with them.
	*/
	public static double computeDeterminismForPlace(
			PetriNetWithData net, Place place
			) {
		double ret = 1.0;
		
		if (net.getOutEdges(place).size() > 1) {
			Collection<PetrinetEdge<
			? extends PetrinetNode,
			? extends PetrinetNode>
			> out = net.getOutEdges(place);
			double totalTrans = out.size();
			double nonTrival = 0.0;
			for(PetrinetEdge<
					? extends PetrinetNode,
					? extends PetrinetNode
				> edge: out) {
				PetrinetNode node = edge.getTarget();
				if (node instanceof PNWDTransition) {
					PNWDTransition trans = (PNWDTransition) node;
					if (!isGuardTrivial(trans)) {
						nonTrival += 1;
					}
				}
			}
			ret = nonTrival / totalTrans;
		}
		
		return ret;
	}
	
	/**
	 * Checks if the given transition has a somewhat trivial guard expression.
	 * A trivial guard is one with no variables and evaluates to true.
	 * @param trans The transition to consider.
	 * @return Whether the guard is trivial.
	 */
	public static boolean isGuardTrivial(PNWDTransition trans) {
		boolean ret = false;
		if (trans.hasGuardExpression()) {
			GuardExpression exp = trans.getGuardExpression();
			if (exp.getNormalVariables().size() == 0) {
				ret = exp.isTrue(new HashMap());
			}
		} else {
			ret = true;
		}
		return ret;
	}

}
