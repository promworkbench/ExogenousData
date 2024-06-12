package org.processmining.qut.exogenousdata.ab.jobs;

public class Tuple<S1, S2> {
	
	private S1 left;
	private S2 right;
	
	public Tuple(S1 left, S2 right) {
		this.left = left;
		this.right = right;
	}
	
	public S1 getLeft() {
		return this.left;
	}
	
	public S2 getRight() {
		return this.right;
	}

}
