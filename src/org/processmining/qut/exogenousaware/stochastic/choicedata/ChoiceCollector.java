package org.processmining.qut.exogenousaware.stochastic.choicedata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.framework.util.ui.widgets.helper.UserCancelledException;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.astar.petrinet.PetrinetReplayerWithoutILP;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayer.PNLogReplayer;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteParam;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;
import org.processmining.pnetreplayer.utils.TransEvClassMappingUtils;
import org.processmining.qut.exogenousaware.data.ExogenousAnnotatedLog;

import lombok.Builder;
import nl.tue.astar.AStarException;

public class ChoiceCollector {
	
	@Builder
	public static class ChoiceCollectorParameters {
		
	}

	public static Map<Set<Transition>, List<ChoiceDataPoint>> collect(ExogenousAnnotatedLog xlog, AcceptingPetriNet net, ChoiceCollectorParameters ccparameters) throws AStarException, UserCancelledException {
		
//		compute an alignment between the given log and net.
		XEventClassifier classifer = xlog.getEndogenousLog().getClassifiers().get(0);
		
		XLogInfo logInfo = XLogInfoFactory.createLogInfo(
				(XLog) xlog.getEndogenousLog().clone(), classifer
		);
		
		TransEvClassMapping mapping = TransEvClassMappingUtils
				.getInstance()
				.getMapping(
						net.getNet(), 
						new HashSet(logInfo.getEventClasses().getClasses()), 
						classifer
		);
				
		
		CostBasedCompleteParam parameters = new CostBasedCompleteParam(
				logInfo.getEventClasses().getClasses(), 
				mapping.getDummyEventClass(),  
				net.getNet().getTransitions(), 
				1, 1
		);
		
		parameters.setGUIMode(false);
		parameters.setUsePartialOrderedEvents(true);
		parameters.setCreateConn(false);
		parameters.setInitialMarking(net.getInitialMarking());
		parameters.setFinalMarkings((Marking[]) net.getFinalMarkings().toArray(new Marking[1]));
//		parameters.setNumThreads(this.maxConcurrentThreads); ILP problem with an array index when threaded : LPProblemProvider:24
		parameters.setMaxNumOfStates(Integer.MAX_VALUE);
		
//		collect alignments for traces in log 
		PNRepResult alignedTraces = new PNLogReplayer().replayLog(
				null,
				net.getNet(),
				xlog.getEndogenousLog(),
				mapping,
				new PetrinetReplayerWithoutILP(),
				parameters
				);
//		traverse each alignment to compute sojourn times between synchronized transitions
		SojournStatistics sojournStats = new SojournStatistics(net.getNet().getTransitions());
		for(SyncReplayResult alignment : alignedTraces) {
			
		}
//		traverse each alignment and collect choice data.
		for(SyncReplayResult alignment : alignedTraces) {
//			for each trace with this control flow alignment
			for(int traceIdx : alignment.getTraceIndex()) {
				XTrace currTrace = xlog.get(traceIdx);
				System.out.println("looking at an alignment for a trace...");
				System.out.println("trace ::  " + currTrace
						.stream()
						.map( (x) -> {return classifer.getClassIdentity(x);})
						.reduce("", (ls,nx) -> {return ls + nx;})
				);
				System.out.println("moves :: "+alignment.getStepTypes());
			}
		}
		
		return new HashMap();
	}
	
	public static class SojournStatistics {
		
		private List<Transition> transitions;
		private Map<Transition, List<Double>> times;
		
		public SojournStatistics(Collection<Transition> collection) {
			this.transitions = new ArrayList<>();
			this.transitions.addAll(collection);
			this.times = new HashMap<>();
			for(Transition trans : this.transitions) {
				this.times.put(trans, new ArrayList());
			}
		}
		
		public void addTime(Transition trans, double time) {
			if (transitions.contains(trans)) {
				this.times.get(trans).add(time);
			}
		}
		
	}
	
	public class ChoiceAlignmentHandler {
		
		private SyncReplayResult alignment;
		private int generatedPoints;
		
		public ChoiceAlignmentHandler(SyncReplayResult alignment) {
			this.alignment = alignment;
		}
		
		private void prepareThetaOptions() {
			
		}
		
		public ChoiceDataPoint[] generateChoiceData(XTrace xtrace) {
			
			return new ChoiceDataPoint[generatedPoints];
		}
	}
	
	public class PowerBetweenSynchronisation {
		
		private int syncLeft;
		private int target;
		private int syncRight;
		
	}
	
	public class PowerProceedingSynchronisation {
		
		private int syncLeft;
		private int target;
	}
	
	public class PowerPrecedingSynchronisation {
		
		private int syncRight;
		private int target;
	}
}
