package org.processmining.qut.exogenousdata.stochastic.conformance;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.qut.exogenousdata.ab.jobs.Tuple;
import org.processmining.qut.exogenousdata.data.ExogenousDataset;
import org.processmining.qut.exogenousdata.stochastic.model.SLPNEDSemantics;
import org.processmining.qut.exogenousdata.stochastic.model.StochasticLabelledPetriNetWithExogenousData;
import org.processmining.stochasticlabelleddatapetrinet.datastate.DataState;
import org.processmining.stochasticlabelleddatapetrinet.duemsc.duEMSC;
import org.processmining.stochasticlabelleddatapetrinet.logadapter.DataStateLogAdapter;
import org.processmining.stochasticlabelleddatapetrinet.probability.TraceProbablility;

import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.custom_hash.TObjectIntCustomHashMap;
import gnu.trove.strategy.HashingStrategy;
import lpsolve.LpSolveException;

public class eduEMSC extends duEMSC {

	public static boolean debug;
	public static boolean parallel = false;

	public static double measureLogModel(XLog log, ExogenousDataset[] datasets, XEventClassifier classifier,
			StochasticLabelledPetriNetWithExogenousData model, boolean debug, ProMCanceller canceller)
			throws LpSolveException {

		//		handle log so we have appropriate transformed values
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
		TObjectIntMap<Pair<String[], DataState[]>> activityDataSequences = getActivityDataSequences(log, classifier,
				logAdapter, maximumTraceLength);

		BigDecimal sum = BigDecimal.ZERO;

		int jobs = activitySequences.size();
		int curr = 1;

		List<Tuple<String[], Integer>> work = new ArrayList();
		for (TObjectIntIterator<String[]> itAs = activitySequences.iterator(); itAs.hasNext();) {
			itAs.advance();
			work.add(new Tuple(itAs.key(), itAs.value()));
		}
		if (parallel) {
			sum = work.parallelStream()
					.map(t -> {
						try {
							return getProbDifference(
									t.getLeft(), 
									t.getRight(), 
									logSize, mc, semantics, canceller, dataSequences,
									activityDataSequences);
						} catch (LpSolveException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return BigDecimal.ZERO;
					})
					.reduce(sum, BigDecimal::add);
		} else {
			sum = work.stream()
					.map(t -> {
						try {
							return getProbDifference(
									t.getLeft(), 
									t.getRight(), 
									logSize, mc, semantics, canceller, dataSequences,
									activityDataSequences);
						} catch (LpSolveException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return BigDecimal.ZERO;
					})
					.reduce(sum, BigDecimal::add);
		}
		

		//		for (TObjectIntIterator<String[]> itAs = activitySequences.iterator(); itAs.hasNext();) {
		//			itAs.advance();
		//
		//			String[] activitySequence = itAs.key();
		//			int activitySequenceWeight = itAs.value();
		//			BigDecimal activitySequenceProbabilityLog = BigDecimal.valueOf(activitySequenceWeight)
		//					.divide(BigDecimal.valueOf(logSize), mc);
		//
		//			BigDecimal activitySequenceProbabilityModel = queryModelForTrace(semantics, canceller, mc, activitySequence,
		//					dataSequences, logSize);
		//
		//			BigDecimal difference = activitySequenceProbabilityLog.subtract(activitySequenceProbabilityModel)
		//					.max(BigDecimal.ZERO);
		//			
		//			if (eduEMSC.debug) {
		//				System.out.println("("+curr+"/" + jobs + ") log: " 
		//						+ activitySequenceProbabilityLog + " model: " + activitySequenceProbabilityModel
		//						+ " difference: " + difference + " trace " + Arrays.toString(activitySequence));
		//			}
		//
		//			curr += 1;
		//			sum = sum.add(difference);
		//		}

		return BigDecimal.ONE.subtract(sum).doubleValue();
	}

	private static TObjectIntMap<Pair<String[], DataState[]>> getActivityDataSequences(XLog log,
			XEventClassifier classifier, DataStateLogAdapter logAdapter, int maximumTraceLength) {
		TObjectIntMap<Pair<String[], DataState[]>> result = new TObjectIntCustomHashMap<>(
				new HashingStrategy<Pair<String[], DataState[]>>() {

					private static final long serialVersionUID = 1L;

					public int computeHashCode(Pair<String[], DataState[]> object) {
						return Arrays.hashCode(object.getA()) + Arrays.hashCode(object.getB());
					}

					public boolean equals(Pair<String[], DataState[]> o1, Pair<String[], DataState[]> o2) {
						return Arrays.equals(o1.getA(), o2.getA()) && Arrays.equals(o1.getB(), o2.getB());
					}
				}, 0);

		log.stream()//
				.parallel()//
				.map(trace -> {
					Pair<String[], DataState[]> pair = Pair.of(TraceProbablility.getActivitySequence(trace, classifier),
						TraceProbablility.getDataSequence(trace, logAdapter, maximumTraceLength));
					return pair;
				}
				)//
				.sequential()//
				.forEach(pair -> result.adjustOrPutValue(pair, 1, 1));
		
		for (TObjectIntIterator<Pair<String[], DataState[]>> itAs = result.iterator(); itAs.hasNext();) {
			itAs.advance();
			Pair<String[], DataState[]> pair = itAs.key();
			System.out.println("[eduEMSC] show act seq of "+ Arrays.toString(pair.getA()) 
			+ " with data "+ Arrays.toString(pair.getB()) 
			+ " with freq of " + itAs.value());
		}
		
		return result;
	}

	public static BigDecimal getProbDifference(
			String[] activitySequence, int activitySequenceWeight, int logSize,
			MathContext mc, SLPNEDSemantics semantics, ProMCanceller canceller,
			TObjectIntMap<DataState[]> dataSequences, 
			TObjectIntMap<Pair<String[], DataState[]>> activityDataSequences)
			throws LpSolveException {

		BigDecimal sum = BigDecimal.ZERO;

		for (TObjectIntIterator<DataState[]> itDs = dataSequences.iterator(); itDs.hasNext();) {
			itDs.advance();

			DataState[] dataSequence = itDs.key();
			int dataSequenceWeight = itDs.value();

			BigDecimal p_L_A_D = BigDecimal.valueOf(activityDataSequences.get(Pair.of(activitySequence, dataSequence)))
					.divide(BigDecimal.valueOf(logSize), mc);
//			don't need to compute model prob if A | D is zero already.
			if (p_L_A_D == BigDecimal.ZERO) {
				continue;
			}
//			if A | D is greater than zero then check model for difference
			BigDecimal p_M_A_c_D = BigDecimal.valueOf(TraceProbablility
					.getTraceProbability(semantics.getduEMSCSemantics(), activitySequence, dataSequence, canceller));
//			substract from the log, the prob of model given A | D
//			at most reduce log prob to zero (does not matter if log has more prob)
			BigDecimal p_L_D = BigDecimal.valueOf(dataSequenceWeight).divide(BigDecimal.valueOf(logSize), mc);
			BigDecimal intermed = p_L_A_D.subtract(p_M_A_c_D.multiply(p_L_D)).max(BigDecimal.ZERO);
			sum = sum.add(intermed);
		}

//		BigDecimal activitySequenceProbabilityLog = BigDecimal.valueOf(activitySequenceWeight)
//				.divide(BigDecimal.valueOf(logSize), mc);
//
//		BigDecimal activitySequenceProbabilityModel = queryModelForTrace(semantics, canceller, mc, activitySequence,
//				dataSequences, logSize);
//
//		BigDecimal difference = activitySequenceProbabilityLog.subtract(activitySequenceProbabilityModel)
//				.max(BigDecimal.ZERO);

		if (eduEMSC.debug) {
			System.out.println("[eduEMSC] for given trace: "+ Arrays.toString(activitySequence));
			System.out.println("[eduEMSC] (concurrent) log, sum of probs over D given A: " + sum);
		}

		return sum;
	}

	public static TObjectIntMap<String[]> getActivitySequences(XLog log, XEventClassifier classifier) {
		TObjectIntMap<String[]> activitySequences = new TObjectIntCustomHashMap<>(new HashingStrategy<String[]>() {
			private static final long serialVersionUID = 1L;

			public int computeHashCode(String[] object) {
				return Arrays.hashCode(object);
			}

			public boolean equals(String[] o1, String[] o2) {
				return Arrays.equals(o1, o2);
			}
		});
		int processed = 0;
		System.out.println("[eduEMSC] processing activity sequences...");
		log.stream().parallel().map(trace -> TraceProbablility.getActivitySequence(trace, classifier)).sequential()
				.forEach(activityTrace -> activitySequences.adjustOrPutValue(activityTrace, 1, 1));
		System.out.println("[eduEMSC] finished activity sequences...");
		//		for (XTrace trace : log) {
		//			String[] activityTrace = TraceProbablility.getActivitySequence(trace, classifier);
		//			activitySequences.adjustOrPutValue(activityTrace, 1, 1);
		//			processed +=1;
		//			if (processed % 25 == 0) {
		//				System.out.println(
		//						"[eduEMSC] handled activity sequences :: " + processed + "/" + log.size()
		//				);
		//			}
		//		}
		return activitySequences;
	}

	public static TObjectIntMap<DataState[]> getDataSequences(XLog log, DataStateLogAdapter logAdapter,
			int maxTraceLength) {
		int processed = 0;
////		build a list of states
//		Map<String, Integer> counts = new HashMap<>();
//		List<DataState[]> states = log.stream()
//			.parallel()
//			.map(trace -> TraceProbablility.getDataSequence(trace, logAdapter, maxTraceLength))
//			.collect(Collectors.toList());
////		build binned versions of values
//		Map<String, Double> maxs = new HashMap();
//		Map<String, Double> mins = new HashMap();
//		states.stream()
//			.forEach(ds -> {
//				for(DataState d: ds) {
//					if (d instanceof ExogenousDataState) {
//						ExogenousDataState e = (ExogenousDataState) d;
//						for(Factor f :e.getState().values()){
//							if (f.isKnown() == 0) {
//								continue;
//							}
//							String name =  f.toString();
//							if (maxs.containsKey(name)) {
//								if (maxs.get(name) < f.getValue()) {
//									maxs.put(name, f.getValue());
//								}
//							} else {
//								maxs.put(name, f.getValue());
//							}
//						}
//						
//					}
//				}
//		});
//		System.out.println(maxs.toString());
////		add mean of each bin to map with value
		
//		hash based on the binned values
		TObjectIntMap<DataState[]> dataSequences = new TObjectIntCustomHashMap<>(new HashingStrategy<DataState[]>() {
			private static final long serialVersionUID = 1L;

			public int computeHashCode(DataState[] object) {
				return Arrays.hashCode(object);
			}

			public boolean equals(DataState[] o1, DataState[] o2) {
				return Arrays.equals(o1, o2);
			}
		});
		System.out.println("[eduEMSC] processing ("
				+log.size()
				+") for data sequences..."
		);
		Stream<DataState[]> stream;
		if (parallel) {
			stream = log.stream()
					.parallel()
					.map(trace -> TraceProbablility.getDataSequence(
							trace, 
							((ExogenousDataStateLogAdapter)logAdapter).clone(), 
							maxTraceLength)
					);
		} else {
			stream = log.stream()
					.map(trace -> TraceProbablility.getDataSequence(
							trace, 
							((ExogenousDataStateLogAdapter)logAdapter).clone(), 
							maxTraceLength)
					);
		}
			
		Iterator<DataState[]> iter = stream.iterator();
		if (iter.hasNext()) {
			for(DataState[] dataTrace = iter.next();iter.hasNext();dataTrace = iter.next()) {
				dataSequences.adjustOrPutValue(dataTrace, 1, 1);
			}
		}
		System.out.println("[eduEMSC] finished collecting data ("+ dataSequences.size() +") sequences...");
		//		for (XTrace trace : log) {
		//			DataState[] dataTrace = TraceProbablility.getDataSequence(trace, logAdapter, maxTraceLength);
		//			dataSequences.adjustOrPutValue(dataTrace, 1, 1);
		//			processed +=1;
		//			if (processed % 25 == 0) {
		//				System.out.println(
		//						"[eduEMSC] handled data sequences :: " + processed + "/" + log.size()
		//				);
		//			}
		//		}
		return dataSequences;
	}

	private static BigDecimal queryModelForTrace(SLPNEDSemantics semantics, ProMCanceller canceller, MathContext mc,
			String[] activitySequence, TObjectIntMap<DataState[]> dataSequences, int logSize) throws LpSolveException {

		BigDecimal sum = BigDecimal.ZERO;

		for (TObjectIntIterator<DataState[]> itDs = dataSequences.iterator(); itDs.hasNext();) {
			itDs.advance();

			DataState[] dataSequence = itDs.key();
			int dataSequenceWeight = itDs.value();
			BigDecimal dataSequenceProbabilityLog = BigDecimal.valueOf(dataSequenceWeight)
					.divide(BigDecimal.valueOf(logSize), mc);

			//get the model probability
			BigDecimal probabilityConditionalModel = BigDecimal.valueOf(TraceProbablility
					.getTraceProbability(semantics.getduEMSCSemantics(), activitySequence, dataSequence, canceller));
			//			System.out.println("computed a conditional model prob.");
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
