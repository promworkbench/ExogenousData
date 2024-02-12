package org.processmining.qut.exogenousaware.stochastic.choicedata;

import lombok.Builder;

@Builder
public class ChoiceExogenousPoint {

	boolean known = true;
	double value = 0.0;
}
