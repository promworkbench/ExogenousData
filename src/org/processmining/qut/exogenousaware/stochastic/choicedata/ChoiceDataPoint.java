package org.processmining.qut.exogenousaware.stochastic.choicedata;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NonNull;

@Builder
public class ChoiceDataPoint {

	@NonNull @Getter Set<Transition> enabled;
	@NonNull @Getter Transition fired;
	@NonNull @Getter ChoiceExogenousPoint[] powers;
	@Default @Getter List<Transition> firingSeq = new ArrayList();
	
	public String toString() {
		String ret = "(";
//		ret += "m0 -> ";
//		for(Transition fired : firingSeq) {
//			ret += fired.getLabel() + " -> ";
//		}
//		ret = ret.substring(0, ret.length()-4);
//		ret += ", ";
		for (Transition trans : enabled) {
			ret += trans.getLabel()+"|";
		}
		ret = ret.substring(0, ret.length()-1);
		ret += ", <";
		for(ChoiceExogenousPoint power : powers) {
			ret += power.toString()+",";
		}
		ret = ret.substring(0, ret.length()-1);
		ret += ">, ";
		ret += fired.getLabel();
		ret += ")";
		return ret;
	}
	
	public int hashCode() {
		return enabled.hashCode() + powers.hashCode();
	}
}
