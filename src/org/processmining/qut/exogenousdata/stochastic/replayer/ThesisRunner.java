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
	
	public static final String DATADIR = "F:\\data\\thesis\\";
	public static final String LOGFILE = DATADIR+"roadfines_snippet.xes";
	public static final String[] DATASETFILES = new String[] {
			DATADIR+"exogenous_dataset_unresolved_fines.xes",
			DATADIR+"exogenous_dataset_unpaid_fines.xes"
	};
	public static final String MODELFILE = DATADIR+"slpned_over_sample.slpned";
	public static final String InterestedCase = "A8690";
	public static final String InterestedLabel = "Send for Credit Collection";
	public static final int[] firing = new int[] {
			12, 10, 16
	};
	
	/*
	 * Mad with power : cmd text sub and replace for table data
	 * grep CHOICEDATA$1 thingo.txt | cut -f2-100000 -d" " | sort | sed s/"Create Fine"/"\\\shtmathCF"/g | sed s/"Insert Fine Notification"/"\\\shtmathIFN"/g | sed s/"Payment"/"\\\shtmathPY?"/g | sed s/"Send Fine"/"\\\shtmathSF"/g | sed s/"Notify Result Appeal to Offender"/"\\\shtmathNRA"/g | sed s/"Receive Result Appeal from Prefecture"/"\\\shtmathRAP"/g | sed s/"Send Appeal to Prefecture"/"\\\shtmathSAP"/g | sed s/"Send for Credit Collection"/"\\\shtmathCC"/g | sed s/"\$fines"/"\\\mathpanelone"/g | sed s/"\#fines"/"\\\mathpaneltwo"/g | sed s/"Insert Date Appeal to Prefecture"/"\\\shtmathIAP"/g | sed s/"Add penalty"/"\\\shtmathAP"/g | sed s/"Appeal to Judge"/"\\\shtmathAtJ"/g | awk '$0="\t"$0"\\\\"'
	 */
	
	/*
	 * Mad with power : cmd text sub and replace for table data
	 * grep VISDATA$1 stdout_from_runner.txt | cut -f2-100000 -d" " | sort | sed s/"Create Fine"/"\\\shtmathCF"/g | sed s/"Insert Fine Notification"/"\\\shtmathIFN"/g | sed s/"Payment"/"\\\shtmathPY?"/g | sed s/"Send Fine"/"\\\shtmathSF"/g | sed s/"Notify Result Appeal to Offender"/"\\\shtmathNRA"/g | sed s/"Receive Result Appeal from Prefecture"/"\\\shtmathRAP"/g | sed s/"Send Appeal to Prefecture"/"\\\shtmathSAP"/g | sed s/"Send for Credit Collection"/"\\\shtmathCC"/g | sed s/"\$fines"/"\\\mathpanelone"/g | sed s/"\#fines"/"\\\mathpaneltwo"/g | sed s/"Insert Date Appeal to Prefecture"/"\\\shtmathIAP"/g | sed s/"Add penalty"/"\\\shtmathAP"/g | sed s/"Appeal to Judge"/"\\\shtmathAtJ"/g | awk '$0="\t"$0"\\\\"'
	 */

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
				.setFiringSeq(firing)
				.replay(net, xlog);
		} catch (Exception e) {
			System.out.println("Runner failed :: "+e.getCause());
			e.printStackTrace();
		}

	}

}
