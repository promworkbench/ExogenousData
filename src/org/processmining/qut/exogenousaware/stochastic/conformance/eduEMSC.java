package org.processmining.qut.exogenousaware.stochastic.conformance;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.qut.exogenousaware.data.ExogenousDataset;
import org.processmining.qut.exogenousaware.stochastic.model.SLPNEDSemantics;
import org.processmining.qut.exogenousaware.stochastic.model.StochasticLabelledPetriNetWithExogenousData;
import org.processmining.stochasticlabelleddatapetrinet.datastate.DataState;
import org.processmining.stochasticlabelleddatapetrinet.duemsc.duEMSC;
import org.processmining.stochasticlabelleddatapetrinet.logadapter.DataStateLogAdapter;
import org.processmining.stochasticlabelleddatapetrinet.probability.TraceProbablility;

import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.TObjectIntMap;
import lpsolve.LpSolveException;

public class eduEMSC extends duEMSC {
	
	public static boolean debug;
	
	public static double measureLogModel(
			XLog log, ExogenousDataset[] datasets, XEventClassifier classifier, 
			StochasticLabelledPetriNetWithExogenousData model, boolean debug,
			ProMCanceller canceller) throws LpSolveException {
		
//		handle log so we have approperiate transformed values
		eduEMSC.debug = debug;

		SLPNEDSemantics semantics = model.getDefaultSemantics();

		return compute(log, classifier, new ExogenousDataStateLogAdapter(semantics, datasets), semantics, canceller);
	}
	
	public static double compute(XLog log, XEventClassifier classifier, DataStateLogAdapter logAdapter,
			SLPNEDSemantics semantics, ProMCanceller canceller) throws LpSolveException {

		MathContext mc = new MathContext(10, RoundingMode.HALF_UP);

		//gather data
		int logSize = log.size();
		int maximumTraceLength = getMaximumTraceLength(log);
		TObjectIntMap<String[]> activitySequences = getActivitySequences(log, classifier);
		TObjectIntMap<DataState[]> dataSequences = getDataSequences(log, logAdapter, maximumTraceLength);

		BigDecimal sum = BigDecimal.ZERO;

		for (TObjectIntIterator<String[]> itAs = activitySequences.iterator(); itAs.hasNext();) {
			itAs.advance();

			String[] activitySequence = itAs.key();
			int activitySequenceWeight = itAs.value();
			BigDecimal activitySequenceProbabilityLog = BigDecimal.valueOf(activitySequenceWeight)
					.divide(BigDecimal.valueOf(logSize), mc);

			BigDecimal activitySequenceProbabilityModel = queryModelForTrace(semantics, canceller, mc, activitySequence,
					dataSequences, logSize);

			BigDecimal difference = activitySequenceProbabilityLog.subtract(activitySequenceProbabilityModel)
					.max(BigDecimal.ZERO);
			
			if (eduEMSC.debug) {
				System.out.println("log: " + activitySequenceProbabilityLog + " model: " + activitySequenceProbabilityModel
						+ " difference: " + difference + " trace " + Arrays.toString(activitySequence));
			}


			sum = sum.add(difference);
		}

		return BigDecimal.ONE.subtract(sum).doubleValue();
	}
	
	private static BigDecimal queryModelForTrace(SLPNEDSemantics semantics,
			ProMCanceller canceller, MathContext mc, String[] activitySequence,
			TObjectIntMap<DataState[]> dataSequences, int logSize) throws LpSolveException {
		
		BigDecimal sum = BigDecimal.ZERO;
		
		for (TObjectIntIterator<DataState[]> itDs = dataSequences.iterator(); itDs.hasNext();) {
			itDs.advance();

			DataState[] dataSequence = itDs.key();
			int dataSequenceWeight = itDs.value();
			BigDecimal dataSequenceProbabilityLog = BigDecimal.valueOf(dataSequenceWeight)
					.divide(BigDecimal.valueOf(logSize), mc);

			//get the model probability
			BigDecimal probabilityConditionalModel = BigDecimal.valueOf(
					TraceProbablility.getTraceProbability(semantics.getduEMSCSemantics(), activitySequence, dataSequence, canceller));

			sum = sum.add(probabilityConditionalModel.multiply(dataSequenceProbabilityLog));

			//			System.out.println("    trace+data done");
		}

		//		System.out.println("  trace done");

		//		TObjectIntIterator<DataState[]> it = dataSequences.iterator();
		//		it.advance();
		//		CrossProductResultDot result2 = new CrossProductResultDot();
		//		FollowerSemanticsDataImpl systemB2 = new FollowerSemanticsDataImpl(activitySequence, it.key());
		//		CrossProductSLDPN.traverse(semantics, systemB2, result2, canceller);
		//		System.out.println(result2.toDot());

		return sum;
	}

}