package org.processmining.qut.exogenousdata.stochastic.choicedata;

import java.util.Objects;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NonNull;

@Builder
public class ChoiceExogenousPoint {

	@NonNull @Getter private String name;
	@Default @Getter private boolean known = false;
	@Default @Getter private double value = 0.0;
	@Default @Getter private boolean skipped = false;
	
	public ChoiceExogenousPoint copy() {
		return ChoiceExogenousPoint.builder()
				.name(this.name)
				.value(this.value)
				.known(this.known)
				.skipped(this.skipped)
				.build();
	}
	
	public String toString() {
		if (skipped) {
			return ">>";
		} else if (! known) {
			return new String("?");
		} else {
			return String.format("%.4f", value);
		}
	}
	
	protected Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return this.copy();
				
	}

	public int hashCode() {
		return Objects.hash(known, name, skipped, value);
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ChoiceExogenousPoint other = (ChoiceExogenousPoint) obj;
		return known == other.known && Objects.equals(name, other.name) && skipped == other.skipped
				&& Double.doubleToLongBits(value) == Double.doubleToLongBits(other.value);
	}

	
	
}
