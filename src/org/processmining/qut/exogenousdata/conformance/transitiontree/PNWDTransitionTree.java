package org.processmining.qut.exogenousdata.conformance.transitiontree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.StopWatch;
import org.deckfour.xes.model.XTrace;
import org.processmining.qut.exogenousdata.conformance.playout.PlayoutStepWithGuard;
import org.processmining.qut.exogenousdata.conformance.playout.PlayoutTraceWithGuards;

public class PNWDTransitionTree implements 
	TransitionTree<TTNode, TTFlowWithGuard> {
	
	private Set<TTFlowWithGuard> flows;
	private Set<TTNode> nodes;
	private TTNode root;
	
	public PNWDTransitionTree(Iterable<PlayoutTraceWithGuards> playouts) {
		super();
		flows = new HashSet<>();
		nodes = new HashSet<>();
		root = PNWDTransitionTreeFactory.constructRootNode();
		nodes.add(root);
		constructTree(playouts);
	}


	protected void constructTree(Iterable<PlayoutTraceWithGuards> playouts) {
//		go through playouts and generate nodes and flows for the tree.
		Map<String,TTNode> prevNodes = new HashMap<>(); 
		prevNodes.put(root.getVariant().toString(), root);
		StopWatch watch = new StopWatch();
		watch.start();
		int steper = 1;		
		for(PlayoutTraceWithGuards playout : playouts) {
			TTNode last = root;
			List<TTFlowWithGuard> path = new ArrayList();
			List<String> variant = new ArrayList();
			int len=0;
			for( PlayoutStepWithGuard step : playout.getSteps()) {
				if (step.isHalt()) {
					last.setTerminal(true);
					break;
				}
				TTNode next = PNWDTransitionTreeFactory.constructNode(path);
				variant.add(step.getLabel());
				TTFlowWithGuard flow;
				if (prevNodes.containsKey(variant.toString())) {
					next = prevNodes.get(variant.toString());
					flow = PNWDTransitionTreeFactory.constructFlow(last, next, step);
				} else {
					flow = PNWDTransitionTreeFactory.constructFlow(last, next, step);
					next.addToPath(flow);
					prevNodes.put(variant.toString(), next);
					nodes.add(next);
				}
				path.add(flow);
				flows.add(flow);
				last = next;
				len++;
			}
			steper++;
			if (steper % 5000 == 0) {
				steper = 1;
				System.out.println("[PNWDTransitionTree] Handled 5000 playouts "
						+ "with len of "+ len);
			}
			
		}
		watch.split();
		System.out.println(
				"[PNWDTransitionTree] "
				+ "constructed tree in "
				+ watch.formatSplitTime());
		applyFlowReduction();
		watch.split();
		System.out.println(
				"[PNWDTransitionTree] "
				+ "flow reduction completed in "
				+ watch.formatSplitTime());
		watch.stop();
	}
	
	public void applyFlowReduction() {
		Set<TTFlowWithGuard> rflows = new HashSet();
		Set<TTNode> rNodes = new HashSet();
		for(TTNode src : nodes) {
			Map<TTNode, Set<TTFlowWithGuard>> maps = 
					new HashMap<TTNode, Set<TTFlowWithGuard>>();
			for(TTFlowWithGuard flow : src.getOutgoingFlows()) {
				if (maps.containsKey(flow.tgt())) {
					maps.get(flow.tgt()).add(flow);
				} else {
					maps.put(flow.tgt(), new HashSet());
					maps.get(flow.tgt()).add(flow);
				}
			}
			src.clearOutEdges();
			for( Entry<TTNode, Set<TTFlowWithGuard>> entry : maps.entrySet()) {
				TTFlowWithGuard flow = null;
				List<TTFlowWithGuard> outs = new ArrayList<>();
				outs.addAll(entry.getValue());
				if (entry.getValue().size() == 1) {
					flow = outs.get(0);
				} else {
					flow = PNWDTransitionTreeFactory.combineFlows(outs);
					for(TTFlowWithGuard oFlow : entry.getValue()) {
						flow.tgt().swapFlowInpath(oFlow, flow);
					}
				}
				src.addFlowToOutedges(flow);
				flow.tgt().setSuccFlow(flow);
				rflows.add(flow);
			}
		}
		flows = rflows;
	}


	public Set<TTFlowWithGuard> getFlows() {
		// TODO Auto-generated method stub
		return flows;
	}

	public Set<TTNode> getNodes() {
		// TODO Auto-generated method stub
		return nodes;
	}

	public TTNode getRoot() {
		// TODO Auto-generated method stub
		return root;
	}

	public TTNode hasNodeFor(XTrace trace) {
		// TODO Auto-generated method stub
		return null;
	}


	public String toString() {
		return "PNWDTransitionTree [\n\troot=" 
				+ root 
				+ ",\n\tnodes=" 
				+ nodes.stream()
					.sorted((a,b) -> -1 * a.getVariant().toString().compareTo(b.getVariant().toString()))
					.collect(Collectors.toList())
				+ ",\n\tflows=" 
				+ flows.stream()
					.sorted((a,b) -> -1 * a.src().getVariant().toString().compareTo(b.src().getVariant().toString()))
					.collect(Collectors.toList())
				+ "\n]";
	}
	
	

}
