package org.processmining.qut.exogenousdata.stochastic.model;

import java.util.BitSet;

import org.processmining.qut.exogenousdata.stochastic.conformance.ExogenousDataState;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNet.VariableType;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNetSemantics;
import org.processmining.stochasticlabelleddatapetrinet.datastate.DataState;

public class SLPNEDSemanticsForduEMSC implements StochasticLabelledDataPetriNetSemantics{
	
	private SLPNEDSemantics realSemenatics;
	private DataState state;
	
	public SLPNEDSemanticsForduEMSC(SLPNEDSemantics semantics) {
		this.realSemenatics = semantics.clone();
	}

	public void setInitialState(DataState initialDataState) {
		realSemenatics.setInitialState();
		state = initialDataState.deepCopy();
	}

	public void executeTransition(int transition, DataState dataEffect) {
		realSemenatics.executeTransition(transition);
		state.update(dataEffect);
	}

	public BitSet getEnabledTransitions() {
		return realSemenatics.getEnabledTransitions();
	}

	public boolean isFinalState() {
		return realSemenatics.isFinalState();
	}

	public byte[] getState() {
		return realSemenatics.getState();
	}

	public void setState(byte[] state) {
		realSemenatics.setState(state);
	}

	public DataState getDataState() {
		return state;
	}

	public void setDataState(DataState dataState) {
		state = dataState.deepCopy();
		
	}

	public DataState newDataState() {
		return state.deepCopy();
	}

	public double getTransitionWeight(int transition) {
//		double weight = 0.0;
		ExogenousDataState xstate = null;
		if (state instanceof ExogenousDataState) {
			xstate = (ExogenousDataState) state;
		}
		double [] powers = xstate.getPowers(realSemenatics.getFactorTranslation());
		int [] known = xstate.getKnowns(realSemenatics.getFactorTranslation());
//		System.out.println("calling weight :: "+weight);
		return realSemenatics.getTransitionWeight(transition, powers, known);
	}

	public double getTotalWeightOfEnabledTransitions() {
		ExogenousDataState xstate = null;
		if (state instanceof ExogenousDataState) {
			xstate = (ExogenousDataState) state;
		}
		double [] powers = xstate.getPowers(realSemenatics.getFactorTranslation());
		int [] known = xstate.getKnowns(realSemenatics.getFactorTranslation());
//		System.out.println("calling total weight :: "+totalWeight);
		return realSemenatics.getTotalWeightOfEnabledTransitions(powers, known);
	}

	public SLPNEDSemanticsForduEMSC clone() {
		SLPNEDSemanticsForduEMSC cloner = new SLPNEDSemanticsForduEMSC(this.realSemenatics);
		cloner.setInitialState(this.state.deepCopy());
		cloner.setState(this.getState());
		return cloner;
	}

	public boolean isTransitionSilent(int transition) {
		return realSemenatics.isTransitionSilent(transition);
	}

	public String getTransitionLabel(int transition) {
		return realSemenatics.getTransitionLabel(transition);
	}

	public int getNumberOfVariables() {
		return realSemenatics.getNumberOfExogenousFactors() * 2;
	}

	public String getVariableLabel(int variable) {
		String label = realSemenatics.getExogenousLabel(variable);
		if (variable % 2 == 0) {
			return label + "-known";
		} else {
			return label + "-unknown";
		}
	}

	public VariableType getVariableType(int variable) {
		return VariableType.CONTINUOUS;
	}

}
