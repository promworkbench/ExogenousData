package org.processmining.qut.exogenousaware.stochastic.discovery;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.framework.util.ui.widgets.helper.UserCancelledException;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.qut.exogenousaware.data.ExogenousAnnotatedLog;
import org.processmining.qut.exogenousaware.stochastic.choicedata.ChoiceCollector;
import org.processmining.qut.exogenousaware.stochastic.choicedata.ChoiceDataPoint;
import org.processmining.qut.exogenousaware.stochastic.model.StochasticLabelledPetriNetWithExogenousData;

import nl.tue.astar.AStarException;

public class SLPNEDDiscovery {
	
	public static StochasticLabelledPetriNetWithExogenousData discover(ExogenousAnnotatedLog xlog, AcceptingPetriNet net) throws AStarException, UserCancelledException
	{
//		collect choice data
		Map<Set<Transition>, List<ChoiceDataPoint>> choicepoints = ChoiceCollector.collect(xlog, net, 
				ChoiceCollector.ChoiceCollectorParameters.builder().build());
		
		return
				new StochasticLabelledPetriNetWithExogenousData(
						net
				);
	}

}
