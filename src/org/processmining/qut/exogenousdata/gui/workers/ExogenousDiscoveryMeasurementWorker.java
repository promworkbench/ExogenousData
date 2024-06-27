package org.processmining.qut.exogenousdata.gui.workers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingWorker;

import org.deckfour.xes.model.XLog;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.qut.exogenousdata.gui.panels.ExogenousDiscoveryProgresser;
import org.processmining.qut.exogenousdata.gui.panels.ExogenousDiscoveryProgresser.ProgressState;
import org.processmining.qut.exogenousdata.gui.panels.ExogenousDiscoveryProgresser.ProgressType;
import org.processmining.qut.exogenousdata.measures.PetriNetMeasure;
import org.processmining.qut.exogenousdata.measures.datapetrinets.DeterminismMeasure;
import org.processmining.qut.exogenousdata.stats.models.ModelStatistics;
import org.processmining.qut.exogenousdata.stats.models.ProcessModelStatistics;

import lombok.Builder;
import lombok.NonNull;


@Builder
public class ExogenousDiscoveryMeasurementWorker extends SwingWorker<Map<String, Double>, Integer> {

	@NonNull XLog endogenousLog;
	@NonNull Object model;
	@NonNull ModelStatistics statistics;
	@NonNull PNRepResult alignment;
	@NonNull Map<String,String> variableMap;
	@NonNull ExogenousDiscoveryProgresser progresser;
	
	private final List<? extends PetriNetMeasure> measureTypes = 
			new ArrayList() {{
		add(DeterminismMeasure.class);
	}};
	
	protected Map<String, Double> doInBackground() throws Exception {
		Map<String,Double> measures = new HashMap();
		
		
		if (statistics instanceof ProcessModelStatistics) {
			((ProcessModelStatistics) statistics ).clearMeasures();
		}
		
//		setup measure state
		int total = 0;
		for(Object moment : statistics.getDecisionMoments()) {
			total += statistics.getDecisionOutcomes(moment).size();
		}
		ProgressState state = progresser.getState(ProgressType.Measurements);
		state.setTotal(total * measureTypes.size());
		state.setProgress(0);
//		measure decision recall
		double measure = DeterminismMeasure.builder()
				.progressInc(1)
				.progresser(progresser)
				.variableMapping(variableMap)
				.build()
				.measure(endogenousLog, model, statistics, alignment);
		measures.put(DeterminismMeasure.NAME, measure);
		
//		double recall = DecisionRecall.builder()
//				.progressInc(1)
//				.progresser(progresser)
//				.variableMapping(variableMap)
//				.build()
//				.measure(endogenousLog, model, statistics, alignment);
//		measures.put(DecisionRecall.NAME, recall);
//		
//		double precision = DecisionPrecision.builder()
//				.progressInc(1)
//				.progresser(progresser)
//				.variableMapping(variableMap)
//				.build()
//				.measure(endogenousLog, model, statistics, alignment);
//		measures.put(DecisionPrecision.NAME, precision);
		
		return measures;
	}

}
