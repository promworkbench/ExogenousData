package org.processmining.qut.exogenousdata.ab.jobs;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.qut.exogenousdata.ab.jobs.bases.Configuration;
import org.processmining.qut.exogenousdata.ab.jobs.bases.RMMTesting;
import org.processmining.qut.exogenousdata.ab.jobs.bases.TestingResult;
import org.processmining.qut.exogenousdata.ab.jobs.bases.TestingResultMilliReal;
import org.processmining.qut.exogenousdata.data.ExogenousDataset;
import org.processmining.qut.exogenousdata.exceptions.CannotConvertException;
import org.processmining.qut.exogenousdata.steps.slicing.data.SubSeries.Scaling;
import org.processmining.qut.exogenousdata.stochastic.conformance.eduEMSC;
import org.processmining.qut.exogenousdata.stochastic.discovery.SLPNEDDiscoverer;
import org.processmining.qut.exogenousdata.stochastic.discovery.SLPNEDDiscoveryOneShot;
import org.processmining.qut.exogenousdata.stochastic.discovery.SLPNEDDiscoveryTwoShot;
import org.processmining.qut.exogenousdata.stochastic.model.StochasticLabelledPetriNetWithExogenousData;
import org.processmining.qut.exogenousdata.stochastic.model.StochasticLabelledPetriNetWithExogenousData.WeightForm;
import org.processmining.qut.exogenousdata.utils.LoadyUtils;
import org.processmining.qut.exogenousdata.utils.LoggyUtils;

public class BPM2024BStrats implements RMMTesting<AcceptingPetriNet, 
	StochasticLabelledPetriNetWithExogenousData> {

	private Path dataDir = Paths.get(
//			"F:\\OneDrive - Queensland University of Technology\\phd\\mypapers\\2023\\B\\data\\eval_data\\data\\mimic"
			"F:\\OneDrive - Queensland University of Technology\\phd\\mypapers\\2023\\B\\data\\eval_data\\data\\roadfines"
			); 
	
	private List<Path> logs = new ArrayList() {{
//		add(Paths.get(dataDir.toString(), "mimic_sampled.xes"));
		add(Paths.get(dataDir.toString(), "roadfines_sampled.xes"));
	}};
	private List<Path> controlflows = new ArrayList() {{
//		add(Paths.get(dataDir.toString(), "models", "imf_default.apnml"));
		add(Paths.get(dataDir.toString(), "models", "road_fines_normative_model_with_marking.pnml"));
	}};
	private List<Integer> sampleSizes = new ArrayList() {{
		for(int i = 1000; i <= 25000; i+=1000) {
			add(i);
		}
//		for(int i = 100; i < 4000; i+=100) {
//			add(i);
//		}
	}};
	private List<Path> xdatasets = new ArrayList() {{
		add(Paths.get(dataDir.toString(), "exogenous_dataset_unpaid_fines.xes"));
		add(Paths.get(dataDir.toString(), "exogenous_dataset_unresolved_fines.xes"));
//		add(Paths.get(dataDir.toString(), "mimic_exogenous_BP1.xes"));
	}};
//	setup for mimic
//	Scaling ts = Scaling.hour;
//	double rounding = 1e-2;
//	setup for road fines
	Scaling ts = Scaling.monthly;
	double rounding = 1e-2;
	
	
	private List<SLPNEDDiscoverer> miners = new ArrayList() {{
		add(new SLPNEDDiscoveryOneShot());
		add(new SLPNEDDiscoveryTwoShot());
//		add(new SLPNEDDiscoveryBatchedOneShot());
//		add(new SLPNEDDiscoveryBatchedTwoShot());
	}};
	
	private List<WeightForm> forms = new ArrayList() {{
		add(WeightForm.INDIVADD);
	}};
	
	public List<
		Configuration<AcceptingPetriNet, 
					  StochasticLabelledPetriNetWithExogenousData,
					  Long, Double>> getConfigurations() {
		List<Configuration<AcceptingPetriNet, 
					  StochasticLabelledPetriNetWithExogenousData,
					  Long, Double>> ret = new ArrayList<>();
		for(Path log: logs) {
			for(Path model: controlflows) {
				for(int sample : sampleSizes) {
					for(SLPNEDDiscoverer miner : miners) {
							for (WeightForm form : forms) {
								miner.configure(rounding, 100, ts, 2.0, form);
								ret.add( new BPM2024BStratConfig(
										log, model, xdatasets, miner.clone(), sample));
							}
							
						}
				}
			}
		}
		return ret;
	}

	public FileInputStream getDumpFile() {
		// TODO Auto-generated method stub
		return null;
	}

	public StochasticLabelledPetriNetWithExogenousData _run(
			Configuration<AcceptingPetriNet, 
			  StochasticLabelledPetriNetWithExogenousData,
			  Long, Double> config) {
		List<ExogenousDataset> datasets = new ArrayList();
		List<Path> dataFiles = (List<Path>) config.other("datasets");
		SLPNEDDiscoverer miner = (SLPNEDDiscoverer) config.other("miner");
		for(Path file : dataFiles) {
			try {
				datasets.add( 
					ExogenousDataset.builder()
					.source(
							LoadyUtils.loadLogFromFile(file.toFile())
					)
					.build()
					.setup()
				);
			} catch (CannotConvertException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			return miner.discover(
					config.trainingLog(), 
					datasets,
					config.controlflow());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public double _measure(
			Configuration<AcceptingPetriNet, 
			  StochasticLabelledPetriNetWithExogenousData,
			  Long, Double> config) {
		XLog log = config.testingLog();
		List<ExogenousDataset> datasets = new ArrayList();
		List<Path> dataFiles = (List<Path>) config.other("datasets");
		for(Path file : dataFiles) {
			try {
				datasets.add( 
					ExogenousDataset.builder()
					.source(
							LoadyUtils.loadLogFromFile(file.toFile())
					)
					.build()
					.setup()
				);
			} catch (CannotConvertException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		ProMCanceller canceller = new ProMCanceller() {
			public boolean isCancelled() {
				return false;
			}
		};
		return eduEMSC.measureLogModel(
				log, datasets.toArray(new ExogenousDataset[0]), 
				new XEventNameClassifier(), config.result(), true, canceller,
				rounding, ts
		);
	}
	
	class BPM2024BStratConfig implements Configuration<
	  AcceptingPetriNet, 
	  StochasticLabelledPetriNetWithExogenousData,
	  Long, Double> {
		
		private Path log;
		private Path model;
		private List<Path> xdatasets;
		private SLPNEDDiscoverer miner;
		private int samples;
		private Map<String, Object> others;
		
		TestingResult<Long, Double> tester = null;
		StochasticLabelledPetriNetWithExogenousData res = null;
		
		public BPM2024BStratConfig(
				Path log, Path model, 
				List<Path> datasets,
				SLPNEDDiscoverer miner,
				int samples) {
			super();
			this.log = log;
			this.model = model;
			this.samples = samples;
			this.others = new HashMap();
			this.others.put("datasets", datasets);
			this.others.put("miner", miner);
		}

		public XLog trainingLog() {
			return LoggyUtils.sampleFromLog(
					LoadyUtils.loadLogFromFile(this.log.toFile()), 
					samples);
		}

		public XLog testingLog() {
			return LoadyUtils.loadLogFromFile(this.log.toFile());
		}

		public AcceptingPetriNet controlflow() {
			return LoadyUtils.loadAcceptingNetFromFile(model.toFile());
		}

		public StochasticLabelledPetriNetWithExogenousData result() {
			return this.res;
		}

		public void setResult(StochasticLabelledPetriNetWithExogenousData result) {
			this.res = result;
		}

		public Object other(String name) {
			Object other = null;
			if (others.containsKey(name)) {
				other = others.get(name);
			}
			return other;
		}

		public void addOther(String name, Object other) {
			this.others.put(name, other);
		}

		public TestingResult<Long, Double> makeTesting() {
			this.tester = new TestingResultMilliReal();
			return this.tester;
		}

		public void setTestingResult(TestingResult<Long, Double> tester) {
			this.tester = tester;
		}

		public TestingResult<Long, Double> getTestingResult() {
			return this.tester;
		}

		public String toString() {
			return "[Config] { 'samples':"
					+ samples
					+ ", 'type':'"
					+ others.get("miner").getClass().toString()
					+ "' }";
		}		
		
		
	}
}
