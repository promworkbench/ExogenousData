package org.processmining.qut.exogenousdata.stochastic.model;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Map;

import org.processmining.stochasticlabelledpetrinets.StochasticLabelledPetriNetSemantics;

public class SLPNEDSemantics implements StochasticLabelledPetriNetSemantics {
	
	private StochasticLabelledPetriNetWithExogenousData net;
	private int[] markedPlaces;
	
	public SLPNEDSemantics(StochasticLabelledPetriNetWithExogenousData slpned) {
		this.net = slpned;
		this.markedPlaces = new int[net.getNumberOfPlaces()];
		setInitialState();
	}

	public int getNumberOfTransitions() {
		return net.getNumberOfTransitions();
	}
	
	public int getNumberOfExogenousFactors() {
		return net.getNumberOfExogenousFactors();
	}
	
	public Map<String, Integer> getFactorTranslation() {
		return net.getDatasetIndexes();
	}
	
	public String getExogenousLabel(int factor) {
		return net.getExogenousLabel(factor);
	}

	public void setInitialState() {
		for (int place = 0; place < net.getNumberOfPlaces(); place++) {
			markedPlaces[place] = (byte) net.isInInitialMarking(place);
		}
	}

	public void executeTransition(int transition) {
//		System.out.println("firing trans ::"+ getTransitionLabel(transition) +" or "+transition);
//		System.out.println("current marking ::"+ Arrays.toString(markedPlaces));
//		if (!getEnabledTransitions().get(transition)) {
//			System.out.print(
//					"Tried to fired transition that is not enabled ("
//					+transition
//					+") in state ("
//					+markedPlaces.toString()
//					+").");
//			return;
//		}
		// remove tokens
		for (int place : net.getInputPlaces(transition)) {
			markedPlaces[place] -= 1;
		}
		// add tokens
		for (int place : net.getOutputPlaces(transition)) {
			markedPlaces[place] += 1;
		}
//		System.out.println("new marking ::"+ Arrays.toString(markedPlaces));

	}

	public BitSet getEnabledTransitions() {
//		System.out.println("current marking ::"+ Arrays.toString(markedPlaces));
		BitSet enabled = new BitSet(getNumberOfTransitions());
		enabled.clear();
		for (int t=0; t < getNumberOfTransitions(); t++) {
			boolean canFire = true;
			for( int iplace : net.getInputPlaces(t))
				if (markedPlaces[iplace] < 1) {
					canFire = false;
					break;
				}
			if (canFire) {
				enabled.set(t);
			}
		}
//		System.out.println("returning enabled :: " + enabled.toString());
		return enabled;
	}

	public boolean isFinalState() {
//		System.out.println("checking in final.");
		if (getEnabledTransitions().cardinality() == 0) {
//			System.out.println("in final");
			return true;
		} else {
//			System.out.println("not in final");
			return false;
		}
	}

	public byte[] getState() {
		byte[] state = new byte[markedPlaces.length];
		for (int p=0; p < markedPlaces.length; p++) {
			state[p] = (byte) (markedPlaces[p] > 0 ? 1: 0);
		}
//		System.out.println("returning state of : " +Arrays.toString(state));
		return state;
	}

	public void setState(byte[] state) {
		for( int s=0; s < state.length; s++) {
			this.markedPlaces[s] = state[s] == 1 ? 1 : 0;
		}
	}

	public boolean isTransitionSilent(int transition) {
		return net.isTransitionSilent(transition);
	}

	public String getTransitionLabel(int transition) {
		return net.getTransitionLabel(transition);
	}

	public double getTransitionWeight(int transition) {
		double [] powers = new double[net.getNumberOfExogenousFactors()];
		int [] known = new int[net.getNumberOfExogenousFactors()];
		Arrays.setAll(known, x -> 0);
		return getTransitionWeight(transition, powers, known);
	}
	
	public double getTransitionWeight(int transition, double[] powers, int[] known) {
		return net.calcWeight(transition, powers, known);
	}

	public double getTotalWeightOfEnabledTransitions() {
		double [] powers = new double[net.getNumberOfExogenousFactors()];
		int [] known = new int[net.getNumberOfExogenousFactors()];
		Arrays.setAll(known, x -> 0);
		Arrays.setAll(powers, x -> 1.0);
		return getTotalWeightOfEnabledTransitions(powers, known);
	}
	
	public double getTotalWeightOfEnabledTransitions(double[] powers, int[] known) {
		BitSet enabled = getEnabledTransitions();
		double weight = 0;
		for( int t=0; t < getNumberOfTransitions(); t++) {
			if (enabled.get(t)) {
				weight += getTransitionWeight(t, powers, known);
			}
		}
		return weight;
	}
	
	public double getProbabilityOfTransition(int transition, 
			double[] powers, int[] known) {
		return (getTransitionWeight(transition, powers, known)) / 
			   (getTotalWeightOfEnabledTransitions(powers, known));
	}
	
	public double getBaseWeight(int transition) {
		return net.getBaseWeight(transition);
	}

	public SLPNEDSemantics clone() {
		SLPNEDSemantics semantics = new SLPNEDSemantics(this.net);
		semantics.setState(getState());
		return semantics;
	}
	
	public SLPNEDSemanticsForduEMSC getduEMSCSemantics() {
		return new SLPNEDSemanticsForduEMSC(this);
	}

}
