package org.processmining.qut.exogenousaware.stochastic.model;

import java.util.BitSet;

import org.processmining.stochasticlabelledpetrinets.StochasticLabelledPetriNetSemantics;

public class SLPNEDSemantics implements StochasticLabelledPetriNetSemantics {
	
	private StochasticLabelledPetriNetWithExogenousData slpned;
	
	public SLPNEDSemantics(StochasticLabelledPetriNetWithExogenousData slpned) {
		this.slpned = slpned;
	}

	public int getNumberOfTransitions() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setInitialState() {
		// TODO Auto-generated method stub

	}

	public void executeTransition(int transition) {
		// TODO Auto-generated method stub

	}

	public BitSet getEnabledTransitions() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isFinalState() {
		// TODO Auto-generated method stub
		return false;
	}

	public byte[] getState() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setState(byte[] state) {
		// TODO Auto-generated method stub

	}

	public boolean isTransitionSilent(int transition) {
		// TODO Auto-generated method stub
		return false;
	}

	public String getTransitionLabel(int transition) {
		// TODO Auto-generated method stub
		return null;
	}

	public double getTransitionWeight(int transition) {
		// TODO Auto-generated method stub
		return 0;
	}

	public double getTotalWeightOfEnabledTransitions() {
		// TODO Auto-generated method stub
		return 0;
	}

	public StochasticLabelledPetriNetSemantics clone() {
		// TODO Auto-generated method stub
		return null;
	}

}
