package org.processmining.qut.exogenousaware.stochastic.choicedata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XAttributeTimestamp;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XTraceImpl;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.framework.util.ui.widgets.helper.UserCancelledException;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.IllegalTransitionException;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.models.semantics.petrinet.PetrinetSemantics;
import org.processmining.models.semantics.petrinet.impl.PetrinetSemanticsFactory;
import org.processmining.plugins.astar.petrinet.PetrinetReplayerWithoutILP;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayer.PNLogReplayer;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteParam;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;
import org.processmining.pnetreplayer.utils.TransEvClassMappingUtils;
import org.processmining.qut.exogenousaware.data.ExogenousDataset;
import org.processmining.qut.exogenousaware.data.ExogenousUtils;
import org.processmining.qut.exogenousaware.exceptions.LinkNotFoundException;
import org.processmining.qut.exogenousaware.steps.slicing.PastOutcomeSlicer;
import org.processmining.qut.exogenousaware.steps.slicing.Slicer;
import org.processmining.qut.exogenousaware.steps.transform.type.Transformer;
import org.processmining.qut.exogenousaware.steps.transform.type.agg.AbsoluteVarianceTransformer;
import org.processmining.qut.exogenousaware.steps.transform.type.agg.TailingWeightedSubsequencesTransform;

import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import nl.tue.astar.AStarException;

public class ChoiceCollector {
	
//	public static Progressor progress = null;
//	
//	public static Progressor setProgress(Progressor progress) {
//		ChoiceCollector.progress = progress;
//	}
//	
//	public static boolean hasProgressor() {
//		return ChoiceCollector.progress != null;
//	}
//	
//	public static void incrProgress() {
//		if (hasProgressor()) {
//			
//		}
//	}
//	
//	public static void incrProgress(int progress) {
//		if (hasProgressor()) {
//			
//		}
//	}
//	
//	public static void adjustProgressLimit(int limit) {
//		if (hasProgressor()) {
//			
//		}
//	}
//	
//	public static void clearProgress() {
//		if (hasProgressor()) {
//			ChoiceCollector.progress = null;
//		}
//	}
	
	@Builder
	public static class ChoiceCollectorParameters {
		
		@Default @Getter Slicer eventSlicer = PastOutcomeSlicer.builder().build(); 
		@Default @Getter Transformer thetaAggerator = TailingWeightedSubsequencesTransform.builder()
				.aggerator( 
						AbsoluteVarianceTransformer.builder().build()	
				)
				.build();
		@Default boolean useDefaultAggerator = true;
		
		public void adjustAggeratorForPanel(ExogenousDataset dataset) {
			if (useDefaultAggerator) {
				double mean = dataset.getMean();
				double std = dataset.getStd();
				this.thetaAggerator = TailingWeightedSubsequencesTransform.builder()
						.aggerator( 
								AbsoluteVarianceTransformer.builder()
								.mean(mean).std(std).build()	
						)
						.build();
			}
		}
		
		public double computeTheta(XTrace trace, int eventIndex, ExogenousDataset dataset) throws LinkNotFoundException {
			adjustAggeratorForPanel(dataset);
			XTrace simplierTrace = new XTraceImpl(trace.getAttributes());
			simplierTrace.add(trace.get(eventIndex));
			XTrace linkage = dataset.findLinkage(simplierTrace).get(0);
			double theta = thetaAggerator.transform(
						eventSlicer.slice(simplierTrace, linkage, dataset).get(trace.get(eventIndex))
				).getRealValue();
//			System.out.println("Computed Theta :: "+ theta);
			return theta;
				
		}
		
		public double computeProceedingTheta(XTrace trace, XEvent posEvent, ExogenousDataset dataset) throws LinkNotFoundException {
			adjustAggeratorForPanel(dataset);
			XTrace simplierTrace = new XTraceImpl(trace.getAttributes());
			simplierTrace.add(posEvent);
			XTrace linkage = dataset.findLinkage(simplierTrace).get(0);
			double theta = thetaAggerator.transform(
						eventSlicer.slice(simplierTrace, linkage, dataset).get(posEvent)
				).getRealValue();
//			System.out.println("Computed Theta :: "+ theta);
			return theta;
		}
		
		public double computeTheta(XTrace trace, int leftIndex, int rightIndex, ExogenousDataset dataset, Double sojourn) throws LinkNotFoundException {
			adjustAggeratorForPanel(dataset);
			XTrace simplierTrace = new XTraceImpl(trace.getAttributes());
			Date newDate = new Date(
					ExogenousUtils.getEventTimeMillis(trace.get(leftIndex))
					+Double.doubleToLongBits(sojourn)
			);
			XEvent newEvent = (XEvent) trace.get(rightIndex).clone();
			((XAttributeTimestamp) newEvent.getAttributes().get("time:timestamp")).setValue(newDate);
			simplierTrace.add(newEvent);
			XTrace linkage = dataset.findLinkage(simplierTrace).get(0);
			double theta = thetaAggerator.transform(
						eventSlicer.slice(simplierTrace, linkage, dataset).get(newEvent)
				).getRealValue();
//			System.out.println("Computed Theta :: "+ theta);
			return theta;
				
		}
		
	}

	public static Iterator<ChoiceDataPoint> collect(
			XLog xlog, 
			List<ExogenousDataset> datasets,
			AcceptingPetriNet net, 
			ChoiceCollectorParameters ccparameters) throws AStarException, UserCancelledException {
		
//		compute an alignment between the given log and net.
		XEventClassifier classifer = xlog.getClassifiers().get(0);
		
		XLogInfo logInfo = XLogInfoFactory.createLogInfo(
				(XLog) xlog, classifer
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
				xlog,
				mapping,
				new PetrinetReplayerWithoutILP(),
				parameters
				);
//		traverse each alignment to compute sojourn times between synchronized transitions
		SojournStatistics sojournStats = new SojournStatistics(net.getNet().getTransitions());
		for(SyncReplayResult alignment : alignedTraces) {
			for(int traceIdx : alignment.getTraceIndex()) {
				XTrace currTrace = xlog.get(traceIdx);
				System.out.println("looking at an alignment for a trace...");
				System.out.println("trace ::  " + currTrace
						.stream()
						.map( (x) -> {return classifer.getClassIdentity(x);})
						.reduce("", (ls,nx) -> {return ls + nx;})
				);
				System.out.println("moves :: "+alignment.getStepTypes());
				sojournStats.processAlignment(currTrace, alignment);
			}
		}
		System.out.println("collected sojourn times...");
		int maxProcess = xlog.size();
//		traverse each alignment and collect choice data in a lazy manner
		return new Iterator<ChoiceDataPoint>() {
			
			private int curr = 0;
			private Iterator<SyncReplayResult> alignments = alignedTraces.iterator();
			private SyncReplayResult currResult = null;
			private ChoiceAlignmentHandler currHandler = null;
			private Iterator<Integer> currTraces = null;
			private int currTraceIndex = -1;
			private Iterator<ChoiceDataPoint> currPoints = null;
			private long timebetween = -1;

			public boolean hasNext() {
				return curr < maxProcess && alignments.hasNext();
			}

			public ChoiceDataPoint next() {
				if (currTraces != null && !currTraces.hasNext()) {
					currResult = null;
				}
				if (!alignments.hasNext()) {
					return null;
				}
				// get result and handler if needed
				if (currResult == null) {
					currResult = alignments.next();
					currHandler = new ChoiceAlignmentHandler(currResult, net, ccparameters);
					currTraces = currResult.getTraceIndex().iterator();
				}
				// check for intermediate result, if so return them
				if (currPoints != null && currPoints.hasNext()) {
					return currPoints.next();
				}
				// begin generating points
				currTraceIndex = currTraces.next();
				currPoints = 
						Arrays.stream(
								currHandler.generateChoiceData(
										xlog.get(currTraceIndex),
										sojournStats,
										datasets))
						.iterator()
						;
				// print some progress out
				curr += 1;
				if (curr % 10 == 0) {
					System.out.println("processed traces into choice data :: "+curr+"/"+maxProcess);
					if (timebetween < 0) {
						timebetween = System.currentTimeMillis();
					} else {
						timebetween = System.currentTimeMillis() - timebetween;
						double seconds = Double.longBitsToDouble(timebetween) / (1000.0* 60.0);
						double intervals = (maxProcess - curr) * 1.0;
						intervals = intervals / 10.0;
						intervals = seconds * intervals;
						System.out.println(
							String.format("likely time left itering choice data :: %.1f minutes", intervals)
						);
					}
				}
				if (currPoints.hasNext()) {
					return currPoints.next();
				} else {
					return next();
				}
				
			}
			
		};
	}
//		for(SyncReplayResult alignment : alignedTraces) {
//			System.out.println("looking at an alignment for a trace...");
//			System.out.println("moves :: "+alignment.getStepTypes());
//			ChoiceAlignmentHandler handler = new ChoiceAlignmentHandler(alignment, net, ccparameters);
////			for each trace with this control flow alignment
//			for(int traceIdx : alignment.getTraceIndex()) {
//				XTrace currTrace = xlog.get(traceIdx);
////				System.out.println("trace ::  " + currTrace
////						.stream()
////						.map( (x) -> {return classifer.getClassIdentity(x);})
////						.reduce("", (ls,nx) -> {return ls + nx;})
////				);
//				for( ChoiceDataPoint point : handler.generateChoiceData(currTrace, sojournStats, datasets)) {
//					if (storage.containsKey(point)) {
//						storage.get(point).add(point);
//					} else {
//						List<ChoiceDataPoint> lister = new ArrayList();
//						lister.add(point);
//						storage.put(point, lister);
//					}
//				}
//				
//				
//			}
//		}
//		
//		return storage;
//	}
	
	/*
	 * Collects sojourn times between two synchronised moves, 
	 * towards the right synchronised move.
	 */
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
		
		public void processAlignment(XTrace trace, SyncReplayResult alignment) {
			int currIdx = -1;
			int transitionIdx = -1;
			StepTypes last_step = null;
			int lastsync = -1;
			int lasttrans = -1;
			for( StepTypes step: alignment.getStepTypes()) {
//				setup for the first entry
				if (last_step == null) {
					last_step = step;
				}
//				every other entry
				else {
					if (step == StepTypes.LMGOOD) {
						if (lastsync >= 0) {
							System.out.println("found pair at ("+lasttrans+","+(transitionIdx+1)+")");
							Object trans = alignment.getNodeInstance().get(transitionIdx+1);
							System.out.println(trans.getClass());
							if (trans instanceof Transition) {
								Double sojourn = 
									(extractTime(trace, currIdx+1).getTime() 
									- 
									extractTime(trace,lastsync).getTime())/ (1000.0 * 60.0);
								addTime((Transition) trans, sojourn);
							}
						}
					}
				}
//				update trackers
				if (step == StepTypes.LMGOOD || step == StepTypes.L) {
					currIdx +=1;
					if (step == StepTypes.LMGOOD) {
						lastsync = currIdx;
					}
				}
				if (step == StepTypes.LMGOOD || step == StepTypes.MINVI || step == StepTypes.MREAL) {
					transitionIdx += 1;
					if (step == StepTypes.LMGOOD) {
						lasttrans = transitionIdx;
					}
				}
			}
		}
		
		public void addTime(Transition trans, double time) {
			System.out.println(trans.toString() + " added time of "+ time);
			if (transitions.contains(trans)) {
				this.times.get(trans).add(time);
			}
		}
		
		private Date extractTime(XTrace trace, int idx) {
			XAttributeTimestamp time = (XAttributeTimestamp) trace.get(idx).getAttributes().get("time:timestamp");
			return time.getValue();
		}
		
		public Double[] getSojourns(Transition trans) {
			return this.times.get(trans).toArray(new Double[0]);
		}
		
		public Double[] getSojournsLessThan(Transition trans, double lessThan) {
			List<Double> keepers = new ArrayList();
			for(Double pos: this.times.get(trans)) {
				if (pos <= lessThan) {
					keepers.add(pos);
				}
			}
			return keepers.toArray(new Double[0]);
		}
		
	}
	
	/*
	 * 
	 */
	public static class ChoiceAlignmentHandler {
		
		private ChoiceCollectorParameters params;
		private AcceptingPetriNet net;
		private SyncReplayResult alignment;
		private List<Integer> generators;
		private PowerHandler[] transformers;
		
		public ChoiceAlignmentHandler(SyncReplayResult alignment, AcceptingPetriNet net, ChoiceCollectorParameters params) {
			this.params = params;
			this.net = net;
			this.alignment = alignment;
			this.transformers = new PowerHandler[alignment.getStepTypes().size()];
			this.generators = new ArrayList();
			prepareThetaOptions();
		}
		
		private void prepareThetaOptions() {
//			determine how to handle producing a theta component in the alignment
			List<StepTypes> steps = alignment.getStepTypes();
			String ret = "";
			for(int i=0; i < steps.size(); i++) {
				transformers[i] = determineHanlder(
					steps.subList(0, i),
					steps.get(i),
					steps.subList(i+1, steps.size())
				);
				ret += transformers[i].toString() + "->";
				if (steps.get(i) == StepTypes.LMGOOD || steps.get(i) == StepTypes.MINVI || steps.get(i) == StepTypes.MREAL) {
					generators.add(i);
				}
			}
			ret = ret.substring(0, ret.length()-2);
			System.out.println("theta operations :: "+ ret);
		}
		
		public ChoiceDataPoint[] generateChoiceData(
				XTrace xtrace, 
				SojournStatistics sojourns,
				List<ExogenousDataset> datasets) {
			ChoiceDataPoint[] ret = new ChoiceDataPoint[generators.size()];
			int powers = datasets.size();
			ChoiceExogenousPoint[][] thetas = new ChoiceExogenousPoint[transformers.length][powers];
//			first pass 
			for( int i =0; i < transformers.length; i++) {
				if (transformers[i] instanceof SilientModelPower) {
					continue;
				} else {
					thetas[i] = transformers[i].handle(thetas, datasets, xtrace, this.params, sojourns);
				}
			}
//			second pass (for silent moves)
			for( int i =0; i < transformers.length; i++) {
				if (transformers[i] instanceof SilientModelPower) {
					thetas[i] = transformers[i].handle(thetas, datasets, xtrace, this.params, sojourns);
				}
			}
//			scrape thetas from generators
			int genIndex = 0;
			for(int gen : generators) {
				ChoiceDataPoint point = ChoiceDataPoint.builder()
						.enabled(findEnabled(gen))
						.fired(findFired(gen))
						.powers(thetas[gen])
						.firingSeq(findFiringSeq(gen))
						.build();
//				System.out.println(point.toString());
				ret[genIndex] = point;
				genIndex++;
			}
			return ret;
		}
		
		public PowerHandler determineHanlder(
				List<StepTypes> left_rem, StepTypes curr, List<StepTypes> right_rem) {
			System.out.println("left_rem : "+ left_rem);
			System.out.println("curr :"+curr);
			System.out.println("right_rem :"+ right_rem);
			if (curr == StepTypes.L) {
				System.out.println("determined log power");
				return new LogPower();
			} else if (curr == StepTypes.MINVI) {
				System.out.println("determined silent power");
				int nexterVis = nextVis(right_rem);
				if (nexterVis < 0) {
					return new SilientModelPower(-1, false);
				} else {
					return new SilientModelPower(left_rem.size()+ 1+ nexterVis, true);
				}
			} else if (curr == StepTypes.LMGOOD) {
				System.out.println("determined sync power");
				return new PowerSynchronistation(findEventIndex(left_rem.size()+1));
			} else {
				if (left_rem.contains(StepTypes.LMGOOD)) {
					if (right_rem.contains(StepTypes.LMGOOD)) {
						System.out.println("determined betweensync power");
						return new PowerBetweenSynchronisation(
							(Transition) alignment.getNodeInstance().get(left_rem.size()),
							findEventIndex(lastSync(left_rem)+1), 
							findEventIndex(left_rem.size()+1+nextSync(right_rem))								
						);
					} else {
						System.out.println("determined preceding power");
						int leftIndex = lastSync(left_rem);
						return new PowerPrecedingSynchronisation(
								findEventIndex(leftIndex),
								(Transition) alignment.getNodeInstance().get(left_rem.size())
						);
					}
				} else if (right_rem.contains(StepTypes.LMGOOD)) {
					System.out.println("determined proceding power");
					int rightIndex = left_rem.size() + 1 + nextSync(right_rem);
					return new PowerProceedingSynchronisation(
							findEventIndex(rightIndex),
							(Transition) alignment.getNodeInstance().get(rightIndex)
					);
				} else {
					System.out.println("determined no power");
					return new NoPower();
				}
			}
		}
		
		public int findTransitionIndex(int stepIndex) {
			int transIndex = -1;
			for(StepTypes step : alignment.getStepTypes().subList(0, stepIndex+1)) {
				if (step == StepTypes.MINVI || step == StepTypes.LMGOOD || step == StepTypes.MREAL) {
					transIndex++;
				}
			}
			return transIndex;
		}
		
		public int findEventIndex(int stepIndex) {
			int eventIndex = -1;
			for(StepTypes step : alignment.getStepTypes().subList(0, stepIndex)) {
				if (step == StepTypes.L || step == StepTypes.LMGOOD) {
					eventIndex++;
				}
			}
			return eventIndex;
		}
		
		public int lastSync(List<StepTypes> steps) {
			int ret = -1;
			for(int i = steps.size()-1; i >= 0; i--) {
				if (steps.get(i) == StepTypes.LMGOOD) {
					ret = i;
					break;
				}
			}
			return ret;
		}
		
		public int nextSync(List<StepTypes> steps) {
			int ret = -1;
			for(int i = 0; i < steps.size(); i++) {
				if (steps.get(i) == StepTypes.LMGOOD) {
					ret = i;
					break;
				}
			}
			return ret;
		}
		
		public int lastVis(List<StepTypes> steps) {
			int ret = -1;
			for(int i = 0; i < steps.size(); i++) {
				if (steps.get(i) == StepTypes.LMGOOD || steps.get(i) == StepTypes.MREAL) {
					ret = i;
					break;
				}
			}
			return ret;
		}
		
		public int nextVis(List<StepTypes> steps) {
			int ret = -1;
			for(int i = 0; i < steps.size(); i++) {
				if (steps.get(i) == StepTypes.LMGOOD || steps.get(i) == StepTypes.MREAL) {
					ret = i;
					break;
				}
			}
			return ret;
		}
		
		public List<Transition> findFiringSeq(int stepIndex){
			List<Transition> firing = new ArrayList();
			int nodeIndex = 0;
			if (stepIndex > 0) {
				for(StepTypes step : alignment.getStepTypes().subList(0, stepIndex)) {
					if (step == StepTypes.LMGOOD || step == StepTypes.MINVI || step == StepTypes.MREAL) {
						Object node = alignment.getNodeInstance().get(nodeIndex);
						if (node instanceof Transition) {
							firing.add( (Transition) node);
						}
					}
					nodeIndex++;
				}
			}
			return firing;
		}
		
		public Set<Transition> findEnabled(int stepIndex){
			PetrinetSemantics curr = PetrinetSemanticsFactory.regularEfficientPetrinetSemantics(net.getNet());
			curr.setCurrentState(net.getInitialMarking());
			for( Transition fired : findFiringSeq(stepIndex)) {
				try {
					curr.executeExecutableTransition(fired);
				} catch (IllegalTransitionException e) {
					// opps the alignment translation is not sound
					e.printStackTrace();
					
				}
			}
			return new HashSet() {{ addAll(curr.getExecutableTransitions()); }};
		}
		
		public Transition findFired(int stepIndex) {
			int transIndex = findTransitionIndex(stepIndex);
			Object node = this.alignment.getNodeInstance().get(stepIndex);
			if (node instanceof Transition) {
				return (Transition) node;
			}
			throw new ValueException("Expected a transition here.");
		}
	}
	
	public interface PowerHandler {
		
		/*
		 * Computes the theta for this step of the alignment sequence
		 */
		public ChoiceExogenousPoint[]handle(
				ChoiceExogenousPoint[][]  thetas, 
				List<ExogenousDataset> datasets,
				XTrace trace,
				ChoiceCollectorParameters params,
				SojournStatistics sojourns,
				Object ...args);
	}
	
	public static class NoPower implements PowerHandler {

		public ChoiceExogenousPoint[] handle(
				ChoiceExogenousPoint[][] thetas, 
				List<ExogenousDataset> datasets,
				XTrace trace,
				ChoiceCollectorParameters params,
				SojournStatistics sojourns,
				Object ...args) {
//			just build an empty power.
			List<String> xnames = datasets.stream().map( d -> d.getName()).collect(Collectors.toList());
			return xnames.stream()
					.map((name) -> {return ChoiceExogenousPoint.builder().name(name).build();})
					.collect(Collectors.toList())
					.toArray(new ChoiceExogenousPoint[0]);
		}
		
		public String toString() {
			return "NoPower";
		}
		
		
	}
	
	public static class SilientModelPower implements PowerHandler {
		
		private int left_target;
		private boolean foundVis;
		
		public SilientModelPower(int target, boolean foundVis) {
			this.left_target = target;
			this.foundVis = foundVis;
		}

		public ChoiceExogenousPoint[] handle(
				ChoiceExogenousPoint[][] thetas, 
				List<ExogenousDataset> datasets,
				XTrace trace,
				ChoiceCollectorParameters params,
				SojournStatistics sojourns,
				Object ...args) {
//			look at the target and copy its power
//			System.out.println("cloning target :: " + left_target);
			ChoiceExogenousPoint[] ret;
			if (!foundVis) {
//				System.out.println("No next visible step found, reverting to no power.");
				NoPower handler = new NoPower();
				ret = handler.handle(thetas, datasets, trace, params, sojourns, args);
			} else {
				ret = thetas[left_target].clone();
			}
			return ret;
		}
		
		public String toString() {
			return "SilentPower";
		}
		
	}
	
	public static class LogPower implements PowerHandler {

		public ChoiceExogenousPoint[] handle(
				ChoiceExogenousPoint[][] thetas, 
				List<ExogenousDataset> datasets,
				XTrace trace,
				ChoiceCollectorParameters params,
				SojournStatistics sojourns,
				Object ...args) {
//			make a skipped power
			List<String> xnames = datasets.stream().map( d -> d.getName()).collect(Collectors.toList());
			return xnames.stream()
					.map((name) -> {return ChoiceExogenousPoint.builder().name(name).skipped(true).build();})
					.collect(Collectors.toList())
					.toArray(new ChoiceExogenousPoint[0]);
		}
		
		public String toString() {
			return "skipped";
		}
		
		
	}
	
	public static class PowerSynchronistation implements PowerHandler {

		private int eventTarget;
		
		public PowerSynchronistation(int target) {
			this.eventTarget = target;
		}
		
		public ChoiceExogenousPoint[] handle(
				ChoiceExogenousPoint[][] thetas, 
				List<ExogenousDataset> datasets,
				XTrace trace,
				ChoiceCollectorParameters params,
				SojournStatistics sojourns,
				Object ...args) {
//			loop through powers and build ceps 
			ChoiceExogenousPoint[] powers = new ChoiceExogenousPoint[datasets.size()];
			int cepIndex = 0;
			for(ExogenousDataset dataset : datasets) {
//				does the target exist in the attributes
				if (dataset.checkLink(trace)) {
						try {
							double theta = params.computeTheta(trace, eventTarget, dataset);
							powers[cepIndex] = ChoiceExogenousPoint.builder()
									.known(true)
									.value(theta)
									.name(dataset.getName())
									.build();
						} catch (LinkNotFoundException e) {
//							already checked, shouldn't happen
						}
						
				} else {
					powers[cepIndex] = ChoiceExogenousPoint.builder()
							.name(dataset.getName())
							.build();
				}
				cepIndex++;
			}
			return powers;
		}
		
		public String toString() {
			return "SyncPower";
		}
		
	}
	
	public static class PowerBetweenSynchronisation implements PowerHandler{
		
		private Transition target;
		private int leftEventIndex;
		private int rightEventIndex;
		
		public PowerBetweenSynchronisation(Transition target, int leftEvent, int rightEvent) {
			this.target = target;
			this.leftEventIndex = leftEvent;
			this.rightEventIndex = rightEvent;
		}
		
		public ChoiceExogenousPoint[] handle(
				ChoiceExogenousPoint[][] thetas, 
				List<ExogenousDataset> datasets,
				XTrace trace,
				ChoiceCollectorParameters params,
				SojournStatistics sojourns,
				Object ...args) {
//			work out upper limit for sojourns
			double duration = findDuration(trace);
			Double[] targetTimes = sojourns.getSojournsLessThan(target, duration);
//			loop through powers and build ceps 
			ChoiceExogenousPoint[] powers = new ChoiceExogenousPoint[datasets.size()];
			int cepIndex = 0;
			for(ExogenousDataset dataset : datasets) {
				if (dataset.checkLink(trace)) {
					double avgTheta = 0.0;
					for(double time : targetTimes) {
						try {
							avgTheta += params.computeTheta(trace, leftEventIndex, rightEventIndex, dataset, time);
						} catch (LinkNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					avgTheta = avgTheta / targetTimes.length;
					powers[cepIndex] = ChoiceExogenousPoint.builder()
							.known(true)
							.value(avgTheta)
							.name(dataset.getName())
							.build();
				} else {
					powers[cepIndex] = ChoiceExogenousPoint.builder()
							.name(dataset.getName())
							.build();
				}
				cepIndex++;
			}
			
			return powers;
		}
		
		public String toString() {
			return "BetweenSync";
		}
		
		private double findDuration(XTrace trace) {
			XEvent left = trace.get(leftEventIndex);
			XEvent right = trace.get(rightEventIndex);
			return ExogenousUtils.getEventTimeMillis(right) - ExogenousUtils.getEventTimeMillis(left) / 1000.0;
		}
		
	}
	
	public static class PowerProceedingSynchronisation implements PowerHandler{
		
		private int eventRight;
		private Transition target;
		
		public PowerProceedingSynchronisation(int eventRight, Transition target) {
			this.eventRight = eventRight;
			this.target = target;
		}
		
		public ChoiceExogenousPoint[] handle(
				ChoiceExogenousPoint[][] thetas, 
				List<ExogenousDataset> datasets,
				XTrace trace,
				ChoiceCollectorParameters params,
				SojournStatistics sojourns,
				Object ...args) {
			// get sojourns of right
			Double[] times = sojourns.getSojourns(target);
//			get event
			XEvent ev = trace.get(eventRight);
			ChoiceExogenousPoint[] powers = new ChoiceExogenousPoint[datasets.size()];
//			for all times, compute theta
			int cepIndex = 0;
			for(ExogenousDataset dataset : datasets) {
				if (dataset.checkLink(trace)) {
					double avgTheta = 0;
					for(Double time : times) {
						Date newDate = new Date(
								ExogenousUtils.getEventTimeMillis(ev)
								-Double.doubleToLongBits(time)
						);
						XEvent newEvent = (XEvent) ev.clone();
						((XAttributeTimestamp) newEvent.getAttributes().get("time:timestamp")).setValue(newDate);
						try {
							avgTheta += params.computeProceedingTheta(trace, newEvent, dataset);
						} catch (LinkNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					avgTheta = avgTheta / times.length;
					powers[cepIndex] = ChoiceExogenousPoint.builder()
							.known(true)
							.value(avgTheta)
							.name(dataset.getName())
							.build();
				} else {
					powers[cepIndex] = ChoiceExogenousPoint.builder()
							.name(dataset.getName())
							.build();
				}
				cepIndex++;
			}
			return powers;
		}
		
		public String toString() {
			return "ProceddingSync";
		}
	}
	
	public static class PowerPrecedingSynchronisation implements PowerHandler{
		
		private int eventLeft;
		private Transition target;
		
		public PowerPrecedingSynchronisation(int eventLeft, Transition target) {
			this.eventLeft = eventLeft;
			this.target = target;
		}
		
		public ChoiceExogenousPoint[] handle(
				ChoiceExogenousPoint[][] thetas, 
				List<ExogenousDataset> datasets,
				XTrace trace,
				ChoiceCollectorParameters params,
				SojournStatistics sojourns,
				Object ...args) {
//			grab the target event
			XEvent leftev = trace.get(eventLeft);
//			grab sojourns for target
			Double[] times = sojourns.getSojourns(target);
//			build powers
			ChoiceExogenousPoint[] powers = new ChoiceExogenousPoint[datasets.size()];
//			loop through datasets and construct powers
			int cepIndex = 0;
			for(ExogenousDataset dataset : datasets) {
//				check for linkage
				if (dataset.checkLink(trace)) {
//					compute an average of the sojourns
					double avgTheta = 0;
					for(Double time : times) {
//						bake a new event
						Date newDate = new Date(
								ExogenousUtils.getEventTimeMillis(leftev)
								+ Double.doubleToLongBits(time)
						);
						XEvent newEvent = (XEvent) leftev.clone();
						((XAttributeTimestamp) newEvent.getAttributes().get("time:timestamp")).setValue(newDate);
						try {
							avgTheta += params.computeProceedingTheta(trace, newEvent, dataset);
						} catch (LinkNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					avgTheta = avgTheta / times.length;
					powers[cepIndex] = ChoiceExogenousPoint.builder()
							.known(true)
							.value(avgTheta)
							.name(dataset.getName())
							.build();
				} else {
					powers[cepIndex] = ChoiceExogenousPoint.builder()
							.name(dataset.getName())
							.build();
				}
				cepIndex += 1;
			}
			
			return powers;
		}
		
		public String toString() {
			return "PrecedingSync";
		}
	}
}
