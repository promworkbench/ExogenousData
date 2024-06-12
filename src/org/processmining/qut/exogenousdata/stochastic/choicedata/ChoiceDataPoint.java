package org.processmining.qut.exogenousdata.stochastic.choicedata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(powers);
		result = prime * result + Objects.hash(enabled);
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ChoiceDataPoint other = (ChoiceDataPoint) obj;
		return Objects.equals(enabled, other.enabled) && Arrays.equals(powers, other.powers);
	}
	
	
}
