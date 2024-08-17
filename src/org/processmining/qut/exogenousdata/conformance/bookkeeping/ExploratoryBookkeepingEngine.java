package org.processmining.qut.exogenousdata.conformance.bookkeeping;

import java.util.HashMap;
import java.util.Map;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.qut.exogenousdata.conformance.guards.Guard.GuardOutcomes;
import org.processmining.qut.exogenousdata.conformance.transitiontree.Matching;
import org.processmining.qut.exogenousdata.conformance.transitiontree.MatchingStep;
import org.processmining.qut.exogenousdata.conformance.transitiontree.TTFlowWithGuard;
import org.processmining.qut.exogenousdata.conformance.transitiontree.TTNode;
import org.processmining.qut.exogenousdata.conformance.transitiontree.TransitionTree;
import org.processmining.qut.exogenousdata.utils.EventyUtils;

public class ExploratoryBookkeepingEngine 
	implements BookkeepingEngine<TTNode, TTFlowWithGuard> {
	
	protected UIPluginContext context;
	protected double k = 0.9;
	protected double c = 1;
	
	public ExploratoryBookkeepingEngine(UIPluginContext context) {
		super();
		this.context = context;
	}

	public ExploratoryBookkeepingEngine() {
		super();
	}

	public double computeBookkeeping(
			TransitionTree<TTNode, TTFlowWithGuard> tree,
			Matching<String, TTFlowWithGuard> matcher, 
			XLog log) {
		double botSum = 0.0;
		double topSum = 0.0;
		boolean optimal = true;
		for(XTrace trace: log) {
			double weight = weightForPaths(trace, matcher);
			double totalWeight = 0;
			int paths = 0;
			for( Iterable<MatchingStep<String, TTFlowWithGuard>> path :
				matcher.getAllPaths(trace)) {
				paths++;
			}
//			get paths for trace
			for( Iterable<MatchingStep<String, TTFlowWithGuard>> path :
				matcher.getAllPaths(trace)) {
				Map<String,Object> dataState = new HashMap<>();
				int stepIdx = -1;
//				walk a path for the trace
				for( MatchingStep<String, TTFlowWithGuard> step : path) {
//					update the datastate as we go
					if (stepIdx >= 0) {
						dataState = EventyUtils.updateAndExtractDataState(
								dataState, 
								trace.get(stepIdx)
						);
					}
//					check the traversed flow
//					check for tree component and see if it matches
					if (!step.isSkip()) {
						topSum += weight * ( 
								step.getModel().get()
								.evaluateGuard(dataState) 
								== GuardOutcomes.TRUE
								?			
								1 : 0
						);
						TTNode src = step.getModel().get().src();
						for(TTFlowWithGuard flow : src.getOutgoingFlows()) {
							if (flow.evaluateGuard(dataState) 
									== GuardOutcomes.TRUE) {
								botSum += (1.0/paths);
							}
						}
					}
					stepIdx++;
				}
				incProgress();
				totalWeight += weight;
			}
			if (Math.abs(1.0 - totalWeight) > 1e-6) {
				optimal = false;
			}
		}
//		add optimal to top if 
		if (optimal) {
			topSum += c;
		}
		botSum += c;
		return topSum / botSum;
	}

	public double computeBookkeeping(
			TTFlowWithGuard flow, 
			Matching<String, TTFlowWithGuard> matcher, 
			XLog log) {
		double botSum = 0.0;
		double topSum = 0.0;
		for(XTrace trace: log) {
			double weight = weightForPaths(trace, matcher);
			double totalWeight = 0;
			int paths = 0;
			for( Iterable<MatchingStep<String, TTFlowWithGuard>> path :
				matcher.getAllPaths(trace)) {
				paths++;
			}
//			get paths for trace
			for( Iterable<MatchingStep<String, TTFlowWithGuard>> path :
				matcher.getAllPaths(trace)) {
				Map<String,Object> dataState = new HashMap<>();
				int stepIdx = -1;
//				walk a path for the trace
				for( MatchingStep<String, TTFlowWithGuard> step : path) {
//					update the datastate as we go
					if (stepIdx >= 0) {
						dataState = EventyUtils.updateAndExtractDataState(
								dataState, 
								trace.get(stepIdx)
						);
					}
//					check the traversed flow
//					check for tree component and see if it matches
					if (!step.isSkip()) {
//						check against the given flow
						if (!flow.equals(step.getModel().get())) {
							stepIdx++;
							continue;
						}
//						only continue if matching flow
						topSum += weight * ( 
								step.getModel().get()
								.evaluateGuard(dataState) 
								== GuardOutcomes.TRUE
								?			
								1 : 0
						);
						TTNode src = step.getModel().get().src();
						for(TTFlowWithGuard srcflow : src.getOutgoingFlows()) {
							botSum += (1/paths) * ( 
									srcflow
									.evaluateGuard(dataState) 
									== GuardOutcomes.TRUE
									?			
									1 : 0
							);
						}
					}
					stepIdx++;
				}
				incProgress();
				totalWeight += weight;
			}
		}
		if (botSum == 0.0) {
			return 0.0;
		}
		return topSum / botSum;
	}
	
	public double weightForPaths(XTrace trace,
			Matching<String, TTFlowWithGuard> matcher) {
		int len = 0;
		double cost = matcher.computeCost(matcher.getPath(trace), trace);
		for(Iterable<MatchingStep<String, TTFlowWithGuard>> path : 
			matcher.getAllPaths(trace)) {
			len++;
		}
		double ret = (1.0/len) * Math.pow(k, cost);
		return ret;
	}
	
//	helper functions for ProM
	private void sendMessageToProgressor(String message) {
		if (context == null) {
			return;
		}
		synchronized (context) {
			context.log(message);
		}
	}
	
	private void incProgress() {
		if (context == null) {
			return;
		}
		synchronized (context) {
			context.getProgress().inc();
		}
	}

}
