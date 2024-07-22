package org.processmining.qut.exogenousdata.conformance.transitiontree;

import java.util.ArrayList;
import java.util.List;

public class TTNode implements TransitionTreeNode {
	
	private TTPathWithGuard path;
	private TTFlowWithGuard succ;
	private List<TTFlowWithGuard> outedges;
	private boolean root;
	private boolean terminal = false;
	
	/**
	 * Constructs the node as a root node of the tree, with unknown outedges.
	 */
	public TTNode() {
		super();
		this.path = new TTPathWithGuard(new ArrayList());
		this.succ = null;
		this.outedges = new ArrayList();
		this.root = true;
	}
	
	/**
	 * Constructs the node as a child of the tree.
	 * @param path
	 * @param variant
	 * @param succ
	 * @param outedges
	 */
	public TTNode(List<TTFlowWithGuard> path, TTFlowWithGuard succ,
			List<TTFlowWithGuard> outedges) {
		super();
		this.path = new TTPathWithGuard(path);
		this.succ = succ;
		this.outedges = outedges;
		this.root = false;
	}
	
	public TransitionTreePath<TTFlowWithGuard> getPath() {
		return path;
	}
	
	public void addToPath(TTFlowWithGuard flow){
		path.append(flow);
	}

	public Iterable<String> getVariant() {
		return path.variant();
	}

	public TransitionTreeFlow getSuccFlow() {
		return succ;
	}
	
	public void setSuccFlow(TTFlowWithGuard succ) {
		this.succ = succ;
	}

	public Iterable<TTFlowWithGuard> getOutgoingFlows() {
		return outedges;
	}

	public boolean isTerminal() {
		return terminal;
	}
	
	public void setTerminal(boolean terminal) {
		this.terminal = terminal;
	}

	public boolean isRoot() {
		return root;
	}
	
	public void addFlowToOutedges(TTFlowWithGuard outedge) {
		outedges.add(outedge);
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((path == null) ? 0 : path.uniqueness());
		result = prime * result + ((getVariant() == null) ? 0 : getVariant().hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TTNode other = (TTNode) obj;
		if (getVariant() == null) {
			if (other.getVariant() != null)
				return false;
		} else if (!getVariant().equals(other.getVariant()))
			return false;
		return true;
	}

	public String toString() {
		String sepl = "[";
		String sepr = "]";
		if (isTerminal()) {
			sepl = "<<";
			sepr = ">>";
		}
		if (root) {
			return "TTNode "
					+ sepl
					+"Root"
					+ sepr;
		} else {
			return "TTNode "
					+ sepl
					+ "variant=" + getVariant() 
					+ sepr;
		}
	}

}
