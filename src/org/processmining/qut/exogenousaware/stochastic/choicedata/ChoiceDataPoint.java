package org.processmining.qut.exogenousaware.stochastic.choicedata;

import java.util.Set;

import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

import lombok.Builder;

@Builder
public class ChoiceDataPoint {

	Set<Transition> enabled;
	Transition fired;
	ChoiceExogenousPoint[] powers;
	
}
