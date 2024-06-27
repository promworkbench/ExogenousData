package org.processmining.qut.exogenousdata.measures;

import org.deckfour.xes.model.XLog;
import org.processmining.qut.exogenousdata.stats.models.ModelStatistics;

public interface PetriNetMeasure {
	
	public String getName();
	
	public double measure(XLog log, Object model, ModelStatistics statistics, Object Alignment);
}
