package org.processmining.qut.exogenousdata.conformance.transitiontree;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.processmining.qut.exogenousdata.conformance.guards.Guard;
import org.processmining.qut.exogenousdata.conformance.guards.NaiveWeakening;
import org.processmining.qut.exogenousdata.conformance.guards.Weakening;
import org.processmining.qut.exogenousdata.conformance.playout.PlayoutStepWithGuard;

public class PNWDTransitionTreeFactory {

	private PNWDTransitionTreeFactory() {};
	
	/**
	 * Constructs a root node.
	 * @return
	 */
	public static TTNode constructRootNode() {
		return new TTNode();
	}
	
	/**
	 * Constructs a node based on a sequence of flows
	 * @param path
	 * @return
	 */
	public static TTNode constructNode(List<TTFlowWithGuard> path) {
		return new TTNode(
				new ArrayList() {{ addAll(path); }}, null, new ArrayList());
	}
	
	public static TTFlowWithGuard constructFlow(TTNode src, TTNode tgt, 
			PlayoutStepWithGuard step) {
		TTFlowWithGuard flow = new TTFlowWithGuard(
				step.getLabel(), step.getGuard(), src, tgt);
		src.addFlowToOutedges(flow);
		tgt.setSuccFlow(flow);
		return flow;
	}
	
	public static TTFlowWithGuard combineFlows(List<TTFlowWithGuard> flows) {
		TTNode src = null;
		TTNode tgt = null;
		String label = null;
//		check that all flows are the same
		for(TTFlowWithGuard flow: flows) {
			if (src == null) {
				src = flow.src();
				tgt = flow.tgt();
				label = flow.label();
			} else {
				assert src.equals(flow.src()) && tgt.equals(flow.tgt()) 
				&& label.equals(flow.label());
			}
		}
		Weakening<Guard> weaker = new NaiveWeakening(flows.stream()
				.map(f -> f.guard())
				.collect(Collectors.toList()));
		return new TTFlowWithGuard(label, weaker, src, tgt);
	}
	
}
