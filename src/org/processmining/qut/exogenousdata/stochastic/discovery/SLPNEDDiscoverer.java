package org.processmining.qut.exogenousdata.stochastic.discovery;

import java.util.List;

import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.qut.exogenousdata.data.ExogenousAnnotatedLog;
import org.processmining.qut.exogenousdata.data.ExogenousDataset;
import org.processmining.qut.exogenousdata.steps.slicing.data.SubSeries.Scaling;
import org.processmining.qut.exogenousdata.stochastic.model.StochasticLabelledPetriNetWithExogenousData;
import org.processmining.qut.exogenousdata.stochastic.model.StochasticLabelledPetriNetWithExogenousData.WeightForm;

public interface SLPNEDDiscoverer {
	
	abstract public StochasticLabelledPetriNetWithExogenousData discoverFromLog(
			ExogenousAnnotatedLog xlog, 
			AcceptingPetriNet net) throws Exception;
	
	abstract public StochasticLabelledPetriNetWithExogenousData discover(
			XLog xlog,
			List<ExogenousDataset> datasets,
			AcceptingPetriNet net) throws Exception;
	
	default public void configure(
		double rounding,
		int batchsize,
		Scaling timeScaling,
		double defaultParameterValue,
		WeightForm form
	) {
		
	}
	
	abstract public SLPNEDDiscoverer clone();
}
