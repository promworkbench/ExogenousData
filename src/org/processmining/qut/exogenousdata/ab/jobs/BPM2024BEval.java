package org.processmining.qut.exogenousdata.ab.jobs;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;

import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetFactory;
import org.processmining.framework.connections.Connection;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.connections.ConnectionID;
import org.processmining.framework.connections.ConnectionManager;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.PluginContextID;
import org.processmining.framework.plugin.PluginDescriptor;
import org.processmining.framework.plugin.PluginExecutionResult;
import org.processmining.framework.plugin.PluginManager;
import org.processmining.framework.plugin.PluginParameterBinding;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.framework.plugin.ProMFuture;
import org.processmining.framework.plugin.Progress;
import org.processmining.framework.plugin.RecursiveCallException;
import org.processmining.framework.plugin.events.Logger.MessageLevel;
import org.processmining.framework.plugin.events.ProgressEventListener.ListenerList;
import org.processmining.framework.plugin.impl.FieldSetException;
import org.processmining.framework.providedobjects.ProvidedObjectManager;
import org.processmining.framework.util.Pair;
import org.processmining.qut.exogenousdata.data.ExogenousDataset;
import org.processmining.qut.exogenousdata.stochastic.conformance.eduEMSC;
import org.processmining.qut.exogenousdata.stochastic.discovery.SLPNEDDiscoveryOneShot;
import org.processmining.qut.exogenousdata.stochastic.discovery.SLPNEDDiscoveryTwoShot;
import org.processmining.qut.exogenousdata.stochastic.model.StochasticLabelledPetriNetWithExogenousData;
import org.processmining.qut.exogenousdata.stochastic.model.StochasticLabelledPetriNetWithExogenousData.WeightForm;

import lombok.Builder;

public class BPM2024BEval {
	
	final static String dataFolder = "F:\\OneDrive - Queensland University of Technology\\phd\\mypapers\\2023\\B\\data\\eval_data\\data\\";
	
	final static String mimicFolder = dataFolder + "mimic\\";
	final static String smartFolder = dataFolder + "smart\\";
	final static String roadFolder = dataFolder + "roadfines\\";
	
	final static String mimicOut = mimicFolder + "out\\";
	final static String smartOut = smartFolder + "out\\";
	final static String roadOut = roadFolder + "out\\";
	
	final static String mimicModels = mimicFolder + "models\\";
	final static String smartModels = smartFolder + "models\\";
	final static String roadModels = roadFolder + "models\\";
	
	final static String mimicDiscLog = mimicFolder + "mimic_s_sampled.xes";
	final static String mimicConfLog = mimicFolder + "mimic_sampled.xes";
	final static String smartDiscLog = smartFolder + "wf101_sampled.xes";
	final static String smartConfLog = smartFolder + "wf101.xes";
	final static String roadDiscLog = roadFolder + "roadfines_s_sampled.xes";
	final static String roadConfLog = roadFolder + "roadfines_sampled.xes";
	
	final static List<String> mimicDatasets = new ArrayList() {{
		add(
				mimicFolder + "mimic_exogenous_BP1.xes"
		);
	}};
	final static List<String> smartDatasets = new ArrayList() {{
		add(
				smartFolder + "xlog_1.xes"
		);
		add(
				smartFolder + "xlog_2.xes"
		);
		add(
				smartFolder + "xlog_3.xes"
		);
	}};
	final static List<String> roadDatasets = new ArrayList() {{
		add( 
				roadFolder + "exogenous_dataset_unpaid_fines.xes"
		);
		add( 
				roadFolder + "exogenous_dataset_unresolved_fines.xes"
		);
	}};
	
	final static String imfNet = "imf_default.apnml";
	final static String dfmNet = "dfm_default.apnml";
	final static String poimNet = "poim_default.pnml";
	final static String rfNorm = "normative_model.pnml";

	@Builder
	public static class Configuration {
		
		public String discLog;
		public String confLog;
		public List<String> datasets;
		public String model;
		public String out;
		public String models;
		public String name;
	}
	
	static List<Configuration> configs = new ArrayList() {{
//		add(
//			Configuration.builder()
//			.discLog(mimicDiscLog)
//			.confLog(mimicConfLog)
//			.datasets(mimicDatasets)
//			.model(imfNet)
//			.out(mimicOut)
//			.models(mimicModels)
//			.name("mimic_imf")
//			.build()
//		);
//		add(
//				Configuration.builder()
//				.discLog(mimicDiscLog)
//				.confLog(mimicConfLog)
//				.datasets(mimicDatasets)
//				.model(dfmNet)
//				.out(mimicOut)
//				.models(mimicModels)
//				.name("mimic_dfm")
//				.build()
//			);
//		add(
//				Configuration.builder()
//				.discLog(mimicDiscLog)
//				.confLog(mimicConfLog)
//				.datasets(mimicDatasets)
//				.model(poimNet)
//				.out(mimicOut)
//				.models(mimicModels)
//				.name("mimic_poim")
//				.build()
//			);
		add(
				Configuration.builder()
				.discLog(smartDiscLog)
				.confLog(smartConfLog)
				.datasets(smartDatasets)
				.model(imfNet)
				.out(smartOut)
				.models(smartModels)
				.name("smart_imf")
				.build()
			);
		add(
				Configuration.builder()
				.discLog(smartDiscLog)
				.confLog(smartConfLog)
				.datasets(smartDatasets)
				.model(dfmNet)
				.out(smartOut)
				.models(smartModels)
				.name("smart_dfm")
				.build()
			);
		add(
				Configuration.builder()
				.discLog(smartDiscLog)
				.confLog(smartConfLog)
				.datasets(smartDatasets)
				.model(poimNet)
				.out(smartOut)
				.models(smartModels)
				.name("smart_poim")
				.build()
			);
		add(
				Configuration.builder()
				.discLog(roadDiscLog)
				.confLog(roadConfLog)
				.datasets(roadDatasets)
				.model(rfNorm)
				.out(roadOut)
				.models(roadModels)
				.name("roadfines_norm")
				.build()
			);
		add(
				Configuration.builder()
				.discLog(roadDiscLog)
				.confLog(roadConfLog)
				.datasets(roadDatasets)
				.model(imfNet)
				.out(roadOut)
				.models(roadModels)
				.name("roadfines_imf")
				.build()
			);
		add(
				Configuration.builder()
				.discLog(roadDiscLog)
				.confLog(roadConfLog)
				.datasets(roadDatasets)
				.model(dfmNet)
				.out(roadOut)
				.models(roadModels)
				.name("roadfines_dfm")
				.build()
			);
		add(
				Configuration.builder()
				.discLog(roadDiscLog)
				.confLog(roadConfLog)
				.datasets(roadDatasets)
				.model(poimNet)
				.out(roadOut)
				.models(roadModels)
				.name("roadfines_poim")
				.build()
			);
	}};
	
	public static String outFile = "C:\\Users\\adam\\Desktop\\testing\\BPM2024B-eval-invmut.stdout";
	public static String outSuffix = "-two-shot-eq-1";
	
	
	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		System.setOut(new PrintStream(
				new BufferedOutputStream(
						new FileOutputStream(outFile,true)), 
				true));
		for (Configuration config : configs) {
			for( int r=0; r<5; r++) {
				System.out.println("Starting run "+(r+1));
				try {
					run(
						config.discLog, config.confLog, config.datasets,
						config.model, config.out, config.models, r
					);
				} catch (Exception e) {
					writeOutError(config, e);
					e.printStackTrace();
				}
			}
			
		}
	}
	
	private static void writeOutError(Configuration config, Exception e) {
		// TODO Auto-generated method stub
		try {
			PrintStream stream = new PrintStream(new File(dataFolder + config.name + ".stderr"));
			
			stream.println("failed to complete run, reason ::");
			stream.println(e);
			for( StackTraceElement el : e.getStackTrace()) {
				stream.println(el.toString());
			}
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public static void run(
			String discLog, String confLog, List<String> sets, String net,
			String outLoc, String modelLoc, int run
		) throws FileNotFoundException, Exception {
//		load the log
		XLog log = new XesXmlParser().parse(new FileInputStream(discLog)).get(0);
//		load datasets
		List<ExogenousDataset> datasets = new ArrayList();
		for(String dataset : sets ) {
			datasets.add( 
					ExogenousDataset.builder()
						.source(
								new XesXmlParser().parse(new FileInputStream(dataset)).get(0)	
						)
						.build()
						.setup()
			);
		}
//		set out location
		SLPNEDDiscoveryOneShot disc = new SLPNEDDiscoveryTwoShot();
//		configuration for multiplicative form (eq.3)
		disc.configure(
				disc.DEFAULT_ROUNDING,
				disc.DEFAULT_BATCH,
				disc.DEFAULT_TIME_SCALE,
				disc.DEFAULT_SOLVING_VALUE,
				WeightForm.INDIVMUT);
//		configuration for additive form (eq.4)
//		disc.configure(
//				disc.DEFAULT_ROUNDING,
//				disc.DEFAULT_BATCH,
//				disc.DEFAULT_TIME_SCALE,
//				disc.DEFAULT_SOLVING_VALUE,
//				WeightForm.INDIVADD);
//		configuration for global additive form (eq.5)
//		disc.configure(
//				disc.DEFAULT_ROUNDING,
//				disc.DEFAULT_BATCH,
//				disc.DEFAULT_TIME_SCALE,
//				disc.DEFAULT_SOLVING_VALUE,
//				WeightForm.GLOBALADD);
		disc.setDumpLoc(outLoc);
//		run the discovery
		StochasticLabelledPetriNetWithExogenousData slpned = 
				disc.discover(log, datasets, loadNet(modelLoc + net));
//		save discovery
		slpned.exportNet(new File(outLoc + net.split("[.]")[0] 
				+ String.format("-%02d", run+1)
				+ outSuffix
				+".slpned"));
//		perform conformance
		log = null;
		System.out.println("loading in conformance log...");
		log = new XesXmlParser().parse(new FileInputStream(confLog)).get(0);
		ProMCanceller canceller = new ProMCanceller() {
			public boolean isCancelled() {
				return false;
			}
		};
		System.out.println("starting eduEMSC...");
		double measure = eduEMSC.measureLogModel(
				log, datasets.toArray(new ExogenousDataset[0]), 
				new XEventNameClassifier(), slpned, true, canceller
		);
		System.out.println("eduEMSC finished...");
		writeOutScore(outLoc + net.split("[.]")[0]
				+ String.format("-%02d", run+1)
				+ outSuffix
				+ ".eduEMSC" , measure);
//		finished run
		System.out.println("computed conformance :: " + measure);
	}
	
	public static void writeOutScore(String out, double score) throws FileNotFoundException {
		PrintStream stream = new PrintStream(new File(out));
		
		stream.println(
				"# eduEMSC score"
		);
		
		stream.println( 
				score
		);
		
		stream.close();
	}
	
	public static AcceptingPetriNet loadNet(String net) throws FileNotFoundException, Exception {
		AcceptingPetriNet aNet = AcceptingPetriNetFactory.createAcceptingPetriNet();
		aNet.importFromStream(new FakeContext(), new FileInputStream(net));
		return aNet;
	}
	
	public static class FakeContext implements PluginContext {

		public PluginManager getPluginManager() {
			// TODO Auto-generated method stub
			return null;
		}

		public ProvidedObjectManager getProvidedObjectManager() {
			// TODO Auto-generated method stub
			return null;
		}

		public ConnectionManager getConnectionManager() {
			// TODO Auto-generated method stub
			return new ConnectionManager() {
				
				public void setEnabled(boolean isEnabled) {
					// TODO Auto-generated method stub
					
				}
				
				public boolean isEnabled() {
					// TODO Auto-generated method stub
					return false;
				}
				
				public <T extends Connection> T getFirstConnection(Class<T> connectionType, PluginContext context,
						Object... objects) throws ConnectionCannotBeObtained {
					// TODO Auto-generated method stub
					return null;
				}
				
				public <T extends Connection> Collection<T> getConnections(Class<T> connectionType, PluginContext context,
						Object... objects) throws ConnectionCannotBeObtained {
					// TODO Auto-generated method stub
					return null;
				}
				
				public org.processmining.framework.plugin.events.ConnectionObjectListener.ListenerList getConnectionListeners() {
					// TODO Auto-generated method stub
					return null;
				}
				
				public Collection<ConnectionID> getConnectionIDs() {
					// TODO Auto-generated method stub
					return null;
				}
				
				public Connection getConnection(ConnectionID id) throws ConnectionCannotBeObtained {
					// TODO Auto-generated method stub
					return null;
				}
				
				public void clear() {
					// TODO Auto-generated method stub
					
				}
				
				public <T extends Connection> T addConnection(T connection) {
					// TODO Auto-generated method stub
					return null;
				}
			};
		}

		public PluginContextID createNewPluginContextID() {
			// TODO Auto-generated method stub
			return null;
		}

		public void invokePlugin(PluginDescriptor plugin, int index, Object... objects) {
			// TODO Auto-generated method stub
			
		}

		public void invokeBinding(PluginParameterBinding binding, Object... objects) {
			// TODO Auto-generated method stub
			
		}

		public Class<? extends PluginContext> getPluginContextType() {
			// TODO Auto-generated method stub
			return null;
		}

		public <T, C extends Connection> Collection<T> tryToFindOrConstructAllObjects(Class<T> type,
				Class<C> connectionType, String role, Object... input) throws ConnectionCannotBeObtained {
			// TODO Auto-generated method stub
			return null;
		}

		public <T, C extends Connection> T tryToFindOrConstructFirstObject(Class<T> type, Class<C> connectionType,
				String role, Object... input) throws ConnectionCannotBeObtained {
			// TODO Auto-generated method stub
			return null;
		}

		public <T, C extends Connection> T tryToFindOrConstructFirstNamedObject(Class<T> type, String name,
				Class<C> connectionType, String role, Object... input) throws ConnectionCannotBeObtained {
			// TODO Auto-generated method stub
			return null;
		}

		public PluginContext createChildContext(String label) {
			// TODO Auto-generated method stub
			return null;
		}

		public Progress getProgress() {
			// TODO Auto-generated method stub
			return null;
		}

		public ListenerList getProgressEventListeners() {
			// TODO Auto-generated method stub
			return null;
		}

		public org.processmining.framework.plugin.events.PluginLifeCycleEventListener.List getPluginLifeCycleEventListeners() {
			// TODO Auto-generated method stub
			return null;
		}

		public PluginContextID getID() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getLabel() {
			// TODO Auto-generated method stub
			return null;
		}

		public Pair<PluginDescriptor, Integer> getPluginDescriptor() {
			// TODO Auto-generated method stub
			return null;
		}

		public PluginContext getParentContext() {
			// TODO Auto-generated method stub
			return null;
		}

		public java.util.List<PluginContext> getChildContexts() {
			// TODO Auto-generated method stub
			return null;
		}

		public PluginExecutionResult getResult() {
			// TODO Auto-generated method stub
			return null;
		}

		public ProMFuture<?> getFutureResult(int i) {
			// TODO Auto-generated method stub
			return null;
		}

		public Executor getExecutor() {
			// TODO Auto-generated method stub
			return null;
		}

		public boolean isDistantChildOf(PluginContext context) {
			// TODO Auto-generated method stub
			return false;
		}

		public void setFuture(PluginExecutionResult resultToBe) {
			// TODO Auto-generated method stub
			
		}

		public void setPluginDescriptor(PluginDescriptor descriptor, int methodIndex)
				throws FieldSetException, RecursiveCallException {
			// TODO Auto-generated method stub
			
		}

		public boolean hasPluginDescriptorInPath(PluginDescriptor descriptor, int methodIndex) {
			// TODO Auto-generated method stub
			return false;
		}

		public void log(String message, MessageLevel level) {
			// TODO Auto-generated method stub
			
		}

		public void log(String message) {
			// TODO Auto-generated method stub
			
		}

		public void log(Throwable exception) {
			// TODO Auto-generated method stub
			
		}

		public org.processmining.framework.plugin.events.Logger.ListenerList getLoggingListeners() {
			// TODO Auto-generated method stub
			return null;
		}

		public PluginContext getRootContext() {
			// TODO Auto-generated method stub
			return null;
		}

		public boolean deleteChild(PluginContext child) {
			// TODO Auto-generated method stub
			return false;
		}

		public <T extends Connection> T addConnection(T c) {
			// TODO Auto-generated method stub
			return null;
		}

		public void clear() {
			// TODO Auto-generated method stub
			
		}
		
	}

}
