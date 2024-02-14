package org.processmining.qut.exogenousaware.stochastic.choicedata;

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
}
