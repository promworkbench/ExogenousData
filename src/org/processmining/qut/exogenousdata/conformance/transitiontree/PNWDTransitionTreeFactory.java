package org.processmining.qut.exogenousdata.conformance.transitiontree;

import java.util.ArrayList;
import java.util.List;

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
		return new TTNode(new ArrayList() {{ addAll(path); }}, null, new ArrayList());
	}
	
	public static TTFlowWithGuard constructFlow(TTNode src, TTNode tgt, PlayoutStepWithGuard step) {
		TTFlowWithGuard flow = new TTFlowWithGuard(step.getLabel(), step.getGuard(), src, tgt);
		src.addFlowToOutedges(flow);
		tgt.setSuccFlow(flow);
		return flow;
	}
	
}
