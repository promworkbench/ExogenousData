package org.processmining.qut.exogenousdata.stochastic.conformance;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.framework.plugin.Progress;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.qut.exogenousdata.ab.jobs.Tuple;
import org.processmining.qut.exogenousdata.data.ExogenousDataset;
import org.processmining.qut.exogenousdata.steps.slicing.data.SubSeries.Scaling;
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
	public static boolean parallel = true;
	
	public static double defaultRoudning = 1e-2;
	public static Scaling timeScaling = Scaling.hour;
	
	private static UIPluginContext CONTEXT = null;
	private static Progress progressor = null;
	
	public static void setContext(UIPluginContext context) {
		CONTEXT = context;
		progressor = null;
	}
	
	public static void clearContext() {
		CONTEXT = null;
		progressor = null;
	}

	public static double measureLogModel(
			XLog log, ExogenousDataset[] datasets, XEventClassifier classifier,
			StochasticLabelledPetriNetWithExogenousData model, 
			boolean debug, ProMCanceller canceller)
	{
		return measureLogModel(
				log, datasets, classifier,
				model, 
				debug, canceller,
				defaultRoudning, timeScaling);
	}
	
	public static double measureLogModel(
			XLog log, ExogenousDataset[] datasets, XEventClassifier classifier,
			StochasticLabelledPetriNetWithExogenousData model, 
			boolean debug, ProMCanceller canceller,
			double rounding, Scaling timeScaler)
	{
		if (CONTEXT != null) {
			progressor = CONTEXT.getProgress();
			progressor.setCaption("Computing eduEMSC");
			progressor.setValue(0);
			progressor.setMaximum(4);
		}
		
		//		handle log so we have appropriate transformed values
		eduEMSC.debug = debug;

		SLPNEDSemantics semantics = model.getDefaultSemantics();
		double ret = compute(log, classifier, 
				new ExogenousDataStateLogAdapter(
						semantics, datasets,
						rounding, timeScaler), 
				semantics, canceller);
		clearContext();
		return ret;
		
	}
	
	

	public static double compute(XLog log, XEventClassifier classifier, DataStateLogAdapter logAdapter,
			SLPNEDSemantics semantics, ProMCanceller canceller) {

		MathContext mc = new MathContext(10, RoundingMode.HALF_UP);

		//gather data
		int logSize = log.size();
		int maximumTraceLength = getMaximumTraceLength(log);
		TObjectIntMap<String[]> activitySequences;
		TObjectIntMap<DataState[]> dataSequences;
		TObjectIntMap<Pair<String[], DataState[]>> activityDataSequences;
		progMessage("Computing all pairs of activity and data sequences...");
		setProgMax(getProgMax()+logSize);
		Object[] plog = processLogForSequences(log, classifier,
				logAdapter, maximumTraceLength);
//		activitySequences = getActivitySequences(log, classifier);
//		progIncr();
//		progMessage("Computing all data sequences...");
//		dataSequences = getDataSequences(log, logAdapter, maximumTraceLength);
//		progIncr();
//		progMessage("Computing all pairs of data and activity sequences...");
//		activityDataSequences = getActivityDataSequences(log, classifier,
//				logAdapter, maximumTraceLength);
		activitySequences = (TObjectIntMap<String[]>) plog[0];
		dataSequences = (TObjectIntMap<DataState[]>) plog[1];
		activityDataSequences = (TObjectIntMap<Pair<String[], DataState[]>>) plog[2];
		progIncr();
		BigDecimal sum = BigDecimal.ZERO;

		List<Tuple<String[], Integer>> work = new ArrayList();
		for (TObjectIntIterator<String[]> itAs = activitySequences.iterator(); itAs.hasNext();) {
			itAs.advance();
			work.add(new Tuple(itAs.key(), itAs.value()));
		}
		setProgMax(getProgMax()+work.size());
		progMessage("Starting computation of probability differences ("
				+work.size()
				+")..."
		);
		if (parallel) {
			sum = work.parallelStream()
					.map(t -> {
						try {
							BigDecimal ret = getProbDifference(
									t.getLeft(), 
									t.getRight(), 
									logSize, mc, semantics, canceller, dataSequences,
									activityDataSequences);
							progIncr();
							return ret;
						} catch (LpSolveException e) {
							// TODO Auto-generated catch block
							System.out.println("[eduEMSC] Failed to compute "
									+ "probability difference.");
							progMessage("error occured in computation");
							e.printStackTrace();
						}
						return BigDecimal.ZERO;
					})
					.reduce(sum, BigDecimal::add);
		} else {
			sum = work.stream()
					.map(t -> {
						try {
							BigDecimal ret = getProbDifference(
									t.getLeft(), 
									t.getRight(), 
									logSize, mc, semantics, canceller, dataSequences,
									activityDataSequences);
							progIncr();
							return ret;
						} catch (LpSolveException e) {
							// TODO Auto-generated catch block
							System.out.println("[eduEMSC] Failed to compute "
									+ "probability difference.");
							progMessage("error occured in computation");
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
		progIncr();
		double ret = BigDecimal.ONE.subtract(sum).doubleValue();
		progMessage("computed eduEMSC was ::" + ret);
		return ret;
	}
	
	private static Object[] processLogForSequences(XLog log,
			XEventClassifier classifier, DataStateLogAdapter logAdapter, 
			int maximumTraceLength) {
		Object[] ret = new Object[3];
//		create custom hash maps
		TObjectIntMap<Pair<String[], DataState[]>> actData = new TObjectIntCustomHashMap<>(
			new HashingStrategy<Pair<String[], DataState[]>>() {
	
				private static final long serialVersionUID = 1L;
	
				public int computeHashCode(Pair<String[], DataState[]> object) {
					return Arrays.hashCode(object.getA()) + Arrays.hashCode(object.getB());
				}
	
				public boolean equals(Pair<String[], DataState[]> o1, Pair<String[], DataState[]> o2) {
					return Arrays.equals(o1.getA(), o2.getA()) && Arrays.equals(o1.getB(), o2.getB());
				}
		}, 0);
		TObjectIntMap<String[]> activities = new TObjectIntCustomHashMap<>(new HashingStrategy<String[]>() {
			private static final long serialVersionUID = 1L;

			public int computeHashCode(String[] object) {
				return Arrays.hashCode(object);
			}

			public boolean equals(String[] o1, String[] o2) {
				return Arrays.equals(o1, o2);
			}
		});
		TObjectIntMap<DataState[]> data = new TObjectIntCustomHashMap<>(new HashingStrategy<DataState[]>() {
			private static final long serialVersionUID = 1L;

			public int computeHashCode(DataState[] object) {
				return Arrays.hashCode(object);
			}

			public boolean equals(DataState[] o1, DataState[] o2) {
				return Arrays.equals(o1, o2);
			}
		});
//		expand on log to find components in ONE pass
		for(Object[] processed : log.parallelStream()
				.map(t -> {
					Object[] process = new Object[2];
//					extract activity
					process[0]  = TraceProbablility.getActivitySequence(t, classifier);
//					extract data
					process[1]  = TraceProbablility.getDataSequence(
							t, ((ExogenousDataStateLogAdapter) logAdapter).clone(), 
							maximumTraceLength
					);
					progIncr();
					return process;
				})
				.collect(ArrayList<Object[]>::new, ArrayList::add, ArrayList::addAll)
			) {
			activities.adjustOrPutValue((String[])processed[0], 1, 1);
			data.adjustOrPutValue((DataState[])processed[1], 1, 1);
			Pair<String[], DataState[]> pair = Pair.of((String[])processed[0],
					(DataState[])processed[1]);
			actData.adjustOrPutValue(pair, 1, 1);
			
		}
//		store and return
		ret[0] = activities;
		ret[1] = data;
		ret[2] = actData;
		return ret;
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
		System.out.println("[eduEMSC] processing activity sequences...");
		log.stream()
			.map(trace -> TraceProbablility.getActivitySequence(trace, classifier))
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
		System.out.println("[eduEMSC] processing traces ("
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
		for(DataState[] dataTrace : stream.collect(
				ArrayList<DataState[]>::new, ArrayList::add, ArrayList::addAll)) {
			dataSequences.adjustOrPutValue(dataTrace, 1, 1);
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
	
	
//	progressor helper functions
	static protected int getProgMax() {
		if (progressor != null) {
			return progressor.getMaximum();
		}
		return 0;
	}
	
	static protected void setProgMax(int max) {
		if (progressor != null) {
			progressor.setMaximum(max);
		}
	}
	
	static protected void setProgVal(int val) {
		if (progressor != null) {
			progressor.setValue(val);
		}
	}
	
	static protected void progMessage(String msg) {
		if (CONTEXT != null) {
			synchronized (CONTEXT) {
				CONTEXT.log(msg);
			}
		}
	}
	
	static protected void progIncr() {
		if (progressor != null) {
			synchronized (progressor) {
				progressor.inc();
			}
		}
	}

}
