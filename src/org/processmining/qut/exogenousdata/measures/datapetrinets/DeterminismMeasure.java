package org.processmining.qut.exogenousdata.measures.datapetrinets;

import java.util.Map;

import org.deckfour.xes.model.XLog;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PetriNetWithData;
import org.processmining.qut.exogenousdata.conformance.Determinism;
import org.processmining.qut.exogenousdata.gui.panels.ExogenousDiscoveryProgresser;
import org.processmining.qut.exogenousdata.gui.panels.ExogenousDiscoveryProgresser.ProgressState;
import org.processmining.qut.exogenousdata.gui.panels.ExogenousDiscoveryProgresser.ProgressType;
import org.processmining.qut.exogenousdata.measures.PetriNetMeasure;
import org.processmining.qut.exogenousdata.stats.models.ModelStatistics;
import org.processmining.qut.exogenousdata.stats.models.ProcessModelStatistics;

import lombok.Builder;
import lombok.NonNull;

@Builder
public class DeterminismMeasure implements PetriNetMeasure {
	
	@NonNull Integer progressInc;
	@NonNull private ExogenousDiscoveryProgresser progresser;
	@NonNull Map<String,String> variableMapping;
	
	public static String NAME = "determinism";
	
	public double measure(
			XLog log, 
			Object model, 
			ModelStatistics statistics, 
			Object Alignment) {
		ProgressState state = progresser.getState(ProgressType.Measurements);
		state.setTotal(state.getTotal()+1);
//		check for the right type of modelstatistics
		ProcessModelStatistics statisticResult;
		if (statistics instanceof ProcessModelStatistics) {
			statisticResult = (ProcessModelStatistics) statistics;
		} else {
			throw new IllegalArgumentException(
				"Unknown statistics result of :: " + 
				statistics.getClass().toGenericString()
			);
		}
		
//		check for the right type of model 
		PetriNetWithData modeller;
		if (model instanceof PetriNetWithData) {
			modeller = (PetriNetWithData) model;
		} else {
			throw new IllegalArgumentException(
					"Unsupported type of model :: " + 
					model.getClass().getSimpleName()
			);
		}
		
//		for each decision moment compute determinism locally
		for( Place moment : statisticResult.getDecisionMoments()) {
			statisticResult.addMeasureToDecisionMoment(
					moment, NAME, 
					Determinism.computeDeterminismForPlace(modeller, moment)
			);
			state.increment(progressInc * 
					statisticResult.getDecisionOutcomes(moment).size()
			);
		}
//		return the global state
		double ret = Determinism.ComputeDeterminism(modeller);
		state.increment(progressInc);
		return ret;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return this.NAME;
	}

}
