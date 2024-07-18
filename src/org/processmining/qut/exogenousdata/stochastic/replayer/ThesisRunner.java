package org.processmining.qut.exogenousdata.stochastic.replayer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XLog;
import org.processmining.qut.exogenousdata.data.ExogenousAnnotatedLog;
import org.processmining.qut.exogenousdata.data.ExogenousAnnotatedLog.ExogenousAnnotatedLogBuilder;
import org.processmining.qut.exogenousdata.data.ExogenousDataset;
import org.processmining.qut.exogenousdata.exceptions.CannotConvertException;
import org.processmining.qut.exogenousdata.steps.determination.Determination;
import org.processmining.qut.exogenousdata.steps.slicing.PastOutcomeSlicer;
import org.processmining.qut.exogenousdata.steps.transform.type.linear.SlopeTransformer;
import org.processmining.qut.exogenousdata.stochastic.model.StochasticLabelledPetriNetWithExogenousData;
import org.processmining.qut.exogenousdata.stochastic.model.in.SLPNEDImporter;

public class ThesisRunner {
	
	public static final String DATADIR = "C:\\Users\\n7176546\\OneDrive - Queensland University of Technology\\phd\\thesis\\extensions\\stochastic\\models\\test_data\\";
	public static final String LOGFILE = DATADIR+"roadfines_snippet.xes";
	public static final String[] DATASETFILES = new String[] {
			DATADIR+"exogenous_dataset_unresolved_fines.xes",
			DATADIR+"exogenous_dataset_unpaid_fines.xes"
	};
	public static final String MODELFILE = DATADIR+"slpned_over_sample.slpned";
	public static final String InterestedCase = "A21188";
	public static final String InterestedLabel = "Send for Credit Collection";
	public static final int[] firing = new int[] {
			
	};

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		StochasticLabelledPetriNetWithExogenousData net;
		XLog log;
		XLog[] datasets = new XLog[DATASETFILES.length];
		try {
			 net = SLPNEDImporter.read(
					 new FileInputStream(new File(MODELFILE))
			);
		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		try {
			log = new XesXmlParser().parse(new File(LOGFILE)).get(0);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}
		
		for(int i=0; i < DATASETFILES.length; i++) {
			try {
				datasets[i] = 
						new XesXmlParser().parse(new File(DATASETFILES[i])).get(0);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
		}
		
		ExogenousAnnotatedLogBuilder xbuilder = ExogenousAnnotatedLog.builder()
				.endogenousLog(log);
		
		List<Determination> determins = new ArrayList<>();
		for(XLog dataset : datasets) {
			try {
				ExogenousDataset datadum = ExogenousDataset.builder()
						.source(dataset)
						.build()
						.setup();
				determins.add(Determination.builder()
					.linker(datadum.getLinker())
					.slicer(new PastOutcomeSlicer())
					.transformer(new SlopeTransformer())
					.build()
				);
				xbuilder.exogenousDataset(datadum);
			} catch (CannotConvertException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		ExogenousAnnotatedLog xlog = xbuilder
				.determinations(determins)
				.useDefaultConfiguration(true)
				.classifiers(log.getClassifiers())
				.extensions(log.getExtensions())
				.globalEventAttributes(log.getGlobalEventAttributes())
				.globalTraceAttributes(log.getGlobalTraceAttributes())
				.attributes(log.getAttributes())
				.parsed(false)
				.build();
		
		try {
			new SLPNEDReplayer()
				.setInterestedTransitionLabel(InterestedLabel)
				.setInterestCase(InterestedCase)
				.replay(net, xlog);
		} catch (Exception e) {
			System.out.println("Runner failed :: "+e.getCause());
			e.printStackTrace();
		}

	}

}
