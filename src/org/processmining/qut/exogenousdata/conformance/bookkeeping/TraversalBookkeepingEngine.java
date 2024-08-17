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

public class TraversalBookkeepingEngine implements 
	BookkeepingEngine<TTNode, TTFlowWithGuard> {
	
	public TraversalBookkeepingEngine() {
		super();
	}
	
	public TraversalBookkeepingEngine(UIPluginContext context) {
		super();
		this.context = context;
	}
	
	protected UIPluginContext context;
	protected double k = 0.9;

	public double computeBookkeeping(
			TransitionTree<TTNode, TTFlowWithGuard> tree,
			Matching<String, TTFlowWithGuard> matcher, 
			XLog log) {
		double retTop = 0.0;
		double retBottom = 0.0;
		for (XTrace trace : log) {
			double weight = weightForPaths(trace, matcher);
			int pathsForTrace = 0;
			for( Iterable<MatchingStep<String, TTFlowWithGuard>> path :
				matcher.getAllPaths(trace)) {
				Map<String,Object> dataState = new HashMap<>();
				int stepIdx = 0;
				for( MatchingStep<String, TTFlowWithGuard> step : path) {
//					update the datastate as we go
					dataState = EventyUtils.updateAndExtractDataState(
							dataState, 
							trace.get(stepIdx)
					);
//					check for tree component and see if it matches
					if (!step.isSkip()) {
							retTop += weight * ( 
									step.getModel().get()
									.evaluateGuard(dataState) 
									== GuardOutcomes.TRUE
									?			
									1 : 0
							);
					}
					stepIdx++;
				}
				pathsForTrace++;
			}
//			update bottom of equation
			for(int i=0; i <pathsForTrace; i++) {
				retBottom += (1/pathsForTrace) * trace.size();
			}
			incProgress();
		}
		return retTop / retBottom;
	}

	public double computeBookkeeping(
			TTFlowWithGuard flow,
			Matching<String, TTFlowWithGuard> matcher, 
			XLog log) {
		double ret = 0.0;
		for (XTrace trace : log) {
			double weight = weightForPaths(trace, matcher);
			for( Iterable<MatchingStep<String, TTFlowWithGuard>> path :
				matcher.getAllPaths(trace)) {
				boolean found = false;
				Map<String,Object> dataState = new HashMap<>();
				int stepIdx = 0;
				for( MatchingStep<String, TTFlowWithGuard> step : path) {
//					update the datastate as we go
					dataState = EventyUtils.updateAndExtractDataState(
							dataState, 
							trace.get(stepIdx)
					);
//					check for tree component and see if it matches
					if (!step.isSkip()) {
						if (step.getModel().get().equals(flow)) {
							ret += weight * ( 
									flow.evaluateGuard(dataState) 
									== GuardOutcomes.TRUE
									?			
									1 : 0
							);
							found = true;
						}
					}
					if (found) {
						break;
					}
					stepIdx++;
				}
			}
		}
		return ret;
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
