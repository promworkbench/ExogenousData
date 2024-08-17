package org.processmining.qut.exogenousdata.conformance.transitiontree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.deckfour.xes.model.XTrace;
import org.processmining.qut.exogenousdata.utils.EventyUtils;
import org.processmining.qut.exogenousdata.utils.TraceyUtils;

public class CManyMatching implements Matching<String, TTFlowWithGuard> {
	
	private PNWDTransitionTree tree;
	private Map<String, 
		Iterable<List<MatchingStep<String, TTFlowWithGuard>>>
	> history;
	
	public CManyMatching(PNWDTransitionTree tree) {
		super();
		this.tree = tree;
		this.history = new HashMap<String, 
				Iterable<List<MatchingStep<String, TTFlowWithGuard>>>>();
	}
	
	
	public CManyMatching(PNWDTransitionTree tree, 
			Map<XTrace, Iterable<List<MatchingStep<String, TTFlowWithGuard>>>> 
				precompute) {
		this(tree);
		for( Entry<XTrace, Iterable<List<MatchingStep<String, TTFlowWithGuard>>>> 
			entry : precompute.entrySet()) {
			String variant = TraceyUtils.getControlFlowVariant(entry.getKey());
			this.history.put(variant, entry.getValue());
		}
	}

	public List<MatchingStep<String, TTFlowWithGuard>> 
		getPath(XTrace trace) {
		String variant = TraceyUtils.getControlFlowVariant(trace);
		for( List<MatchingStep<String, TTFlowWithGuard>> ret : getAllPaths(trace)) {
			return ret;
		}
		return new ArrayList();	
	}

	public Iterable<List<MatchingStep<String, TTFlowWithGuard>>> 
		getAllPaths(XTrace trace) {
		String variant = TraceyUtils.getControlFlowVariant(trace);
		Iterable<List<MatchingStep<String, TTFlowWithGuard>>> ret;
//		check if we already have a path ready to go
		if (history.containsKey(variant)) {
			ret = history.get(variant);
			return ret;
		} else {
			history.put(variant, new ArrayList<>());
		}
		ret = history.get(variant);
		Set<List<MatchingStep<String, TTFlowWithGuard>>> temp = 
				new HashSet();
//		look at three sets and then work back
//		look at terminals with same length or less
		List<TTNode> maybes = new ArrayList();
		List<TTNode> shorters = new ArrayList();
		int lowestCost = Integer.MAX_VALUE;
		for(TTNode node :tree.getNodes()) {
			if (node.getPath().length() < trace.size()) {
				shorters.add(node);
			}
			if (node.getPath().length() == trace.size()) {
				maybes.add(node);
				if (node.isTerminal()) {
					List<MatchingStep<String, TTFlowWithGuard>> maybePath =
							buildSequence(trace, node.getPath());
					int cost = computeCost(
							maybePath, trace
					);
					if (cost < lowestCost) {
						lowestCost = cost;
						temp.clear();
						temp.add(maybePath);
					} else if (cost == lowestCost) {
						temp.add(maybePath);
					}
				}
			}
			
		}
		if (lowestCost == 0) {
//			no point checking non terminals, if lowest cost is already optimal.
			history.put(variant, temp);
			return temp;
		}
//		look at nodes with the same length
		for(TTNode node: maybes) {
			List<MatchingStep<String, TTFlowWithGuard>> maybePath =
					buildSequence(trace, node.getPath());
			if (temp.contains(maybePath)) {
				continue;
			}
			int cost = computeCost(
					maybePath, trace
			);
			if (cost < lowestCost) {
				lowestCost = cost;
				temp.clear();
				temp.add(maybePath);
			} else if (cost == lowestCost) {
				temp.add(maybePath);
			}
		}
//		iterative look back from all nodes, adding skips (logmoves)
		for( TTNode shrt : shorters) {
			for(int n=0; n < trace.size(); n++) {
				Boolean[] mask = new Boolean[trace.size()];
				Arrays.fill(mask, false);
				mask[n] = Boolean.TRUE;
				List<MatchingStep<String, TTFlowWithGuard>> maybePath =
						buildSequence(trace, shrt.getPath(),
						Arrays.asList(mask)
						);
				if (temp.contains(maybePath)) {
					continue;
				}
				int cost = computeCost(
						maybePath, trace
				);
//				System.out.println(""
//						+ maybePath
//						+ "|"
//						+ cost
//				);
				if (cost < lowestCost) {
					lowestCost = cost;
					temp.clear();
					temp.add(maybePath);
				} else if (cost == lowestCost) {
					temp.add(maybePath);
				}
			}
		}
		ret = temp;
		history.put(variant, ret);
		return ret;
	}
	
	private List<MatchingStep<String, TTFlowWithGuard>> buildSequence(
			XTrace trace,
			TransitionTreePath<TTFlowWithGuard> path
			){
		List<MatchingStep<String, TTFlowWithGuard>> ret = new ArrayList();
		int n = 0;
		for(TTFlowWithGuard step : path.getSteps()) {
			ret.add(
					new CManyMatchingStep(
							EventyUtils.getConcept(trace.get(n)),
							step
					)
			);
			n += 1;
			if (n >= trace.size()) {
				break;
			}
		}
		return ret;
	}
	
	private List<MatchingStep<String, TTFlowWithGuard>> buildSequence(
			XTrace trace,
			TransitionTreePath<TTFlowWithGuard> path,
			Iterable<Boolean> mask
			){
		List<MatchingStep<String, TTFlowWithGuard>> ret = new ArrayList();
		
		int n = 0;
		Iterator<TTFlowWithGuard> steps = path.getSteps().iterator();
		
		for(boolean skip : mask) {
			if (skip) {
				ret.add(
						new CManyMatchingStep(
								EventyUtils.getConcept(trace.get(n))
						)
				);
			} else {
				if (steps.hasNext()) {
					ret.add(
							new CManyMatchingStep(
								EventyUtils.getConcept(trace.get(n)),
								steps.next()
							)
					);
				} else {
					break;
				}
				
			}
			n++;
		}
		return ret;
	}

}
