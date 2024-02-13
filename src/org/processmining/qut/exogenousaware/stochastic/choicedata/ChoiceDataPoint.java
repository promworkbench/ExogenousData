package org.processmining.qut.exogenousaware.stochastic.choicedata;

import java.util.Set;

import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Builder
public class ChoiceDataPoint {

	@NonNull @Getter Set<Transition> enabled;
	@NonNull @Getter Transition fired;
	@NonNull @Getter ChoiceExogenousPoint[] powers;
	
	public String toString() {
		String ret = "(";
		for (Transition trans : enabled) {
			ret += trans.getLabel();
		}
		ret += ", <";
		for(ChoiceExogenousPoint power : powers) {
			ret += power.toString()+",";
		}
		ret += ">, ";
		ret += fired.getLabel();
		ret += ")";
		return ret;
	}
}
