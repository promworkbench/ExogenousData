package org.processmining.qut.exogenousaware.stochastic.discovery;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.junit.Test;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetImpl;
import org.processmining.log.utils.XLogBuilder;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.qut.exogenousaware.data.ExogenousAnnotatedLog;
import org.processmining.qut.exogenousaware.data.ExogenousDataset;
import org.processmining.qut.exogenousaware.data.ExogenousDatasetAttributes;
import org.processmining.qut.exogenousaware.data.ExogenousDatasetLinkType;
import org.processmining.qut.exogenousaware.data.ExogenousDatasetType;
import org.processmining.qut.exogenousaware.steps.determination.Determination;
import org.processmining.qut.exogenousaware.steps.linking.AttributeLinker;
import org.processmining.qut.exogenousaware.steps.slicing.PastOutcomeSlicer;
import org.processmining.qut.exogenousaware.steps.transform.type.StochasticTransformer;

public class SLPNEDDiscoveryTest {
	
	static ExogenousDataset Y = ExogenousDataset.builder()
			.dataType(ExogenousDatasetType.NUMERICAL)
			.linker( 
				AttributeLinker.builder()
					.attributeName("exogenous:link")
					.build()	
			)
			.linkType(ExogenousDatasetLinkType.TRACE_ATTRIBUTE_MATCH)
			.source( 
					XLogBuilder.newInstance()
					.startLog("Y")
					.addTrace("xseries_02")
					.addAttribute("exogenous:link", "2")
						.addEvent("exogenous_datapoint_001")
							.addAttribute("time:timestamp", new Date(2024, 2, 8, 7, 00, 00))
							.addAttribute(ExogenousDatasetAttributes.EXOEVENTVALUE, 5.0)
						.addEvent("exogenous_datapoint_002")
							.addAttribute("time:timestamp", new Date(2024, 2, 8, 9, 00, 00))
							.addAttribute(ExogenousDatasetAttributes.EXOEVENTVALUE, 10.0)
						.addEvent("exogenous_datapoint_003")
							.addAttribute("time:timestamp", new Date(2024, 2, 8, 10, 15, 00))
							.addAttribute(ExogenousDatasetAttributes.EXOEVENTVALUE, 15.0)
						.addEvent("exogenous_datapoint_004")
							.addAttribute("time:timestamp", new Date(2024, 2, 8, 11, 32, 00))
							.addAttribute(ExogenousDatasetAttributes.EXOEVENTVALUE, 20.0)
						.addEvent("exogenous_datapoint_005")
							.addAttribute("time:timestamp", new Date(2024, 2, 8, 15, 00, 00))
							.addAttribute(ExogenousDatasetAttributes.EXOEVENTVALUE, 25.0)
						.addEvent("exogenous_datapoint_006")
							.addAttribute("time:timestamp", new Date(2024, 2, 8, 17, 32, 00))
							.addAttribute(ExogenousDatasetAttributes.EXOEVENTVALUE, 30.0)
					.addTrace("xseries_03")
					.addAttribute("exogenous:link", "3")
						.addEvent("exogenous_datapoint_001")
							.addAttribute("time:timestamp", new Date(2024, 2, 8, 7, 00, 00))
							.addAttribute(ExogenousDatasetAttributes.EXOEVENTVALUE, 3.0)
						.addEvent("exogenous_datapoint_002")
							.addAttribute("time:timestamp", new Date(2024, 2, 8, 9, 00, 00))
							.addAttribute(ExogenousDatasetAttributes.EXOEVENTVALUE, 6.0)
						.addEvent("exogenous_datapoint_003")
							.addAttribute("time:timestamp", new Date(2024, 2, 8, 10, 15, 00))
							.addAttribute(ExogenousDatasetAttributes.EXOEVENTVALUE, 9.0)
						.addEvent("exogenous_datapoint_004")
							.addAttribute("time:timestamp", new Date(2024, 2, 8, 11, 32, 00))
							.addAttribute(ExogenousDatasetAttributes.EXOEVENTVALUE, 12.0)
						.addEvent("exogenous_datapoint_005")
							.addAttribute("time:timestamp", new Date(2024, 2, 8, 15, 00, 00))
							.addAttribute(ExogenousDatasetAttributes.EXOEVENTVALUE, 15.0)
						.addEvent("exogenous_datapoint_006")
							.addAttribute("time:timestamp", new Date(2024, 2, 8, 17, 32, 00))
							.addAttribute(ExogenousDatasetAttributes.EXOEVENTVALUE, 18.0)
					.addTrace("xseries_05")
					.addAttribute("exogenous:link", "5")
						.addEvent("exogenous_datapoint_001")
							.addAttribute("time:timestamp", new Date(2024, 2, 8, 11, 00, 00))
							.addAttribute(ExogenousDatasetAttributes.EXOEVENTVALUE, 5.0)
						.addEvent("exogenous_datapoint_002")
							.addAttribute("time:timestamp", new Date(2024, 2, 8, 13, 00, 00))
							.addAttribute(ExogenousDatasetAttributes.EXOEVENTVALUE, 10.0)
						.addEvent("exogenous_datapoint_003")
							.addAttribute("time:timestamp", new Date(2024, 2, 8, 14, 15, 00))
							.addAttribute(ExogenousDatasetAttributes.EXOEVENTVALUE, 15.0)
						.addEvent("exogenous_datapoint_004")
							.addAttribute("time:timestamp", new Date(2024, 2, 8, 14, 32, 00))
							.addAttribute(ExogenousDatasetAttributes.EXOEVENTVALUE, 20.0)
						.addEvent("exogenous_datapoint_005")
							.addAttribute("time:timestamp", new Date(2024, 2, 8, 16, 05, 00))
							.addAttribute(ExogenousDatasetAttributes.EXOEVENTVALUE, 25.0)
						.addEvent("exogenous_datapoint_006")
							.addAttribute("time:timestamp", new Date(2024, 2, 8, 17, 39, 00))
							.addAttribute(ExogenousDatasetAttributes.EXOEVENTVALUE, 30.0)
					.build()
			)
			.build();
	
	static ExogenousDataset Z = ExogenousDataset.builder()
			.dataType(ExogenousDatasetType.NUMERICAL)
			.linker( 
				AttributeLinker.builder()
					.attributeName("exogenous:link:z")
					.build()	
			)
			.linkType(ExogenousDatasetLinkType.TRACE_ATTRIBUTE_MATCH)
			.source( 
					XLogBuilder.newInstance()
					.startLog("Z")
					.addTrace("xseries_01")
					.addAttribute("exogenous:link:z", "1")
						.addEvent("exogenous_datapoint_001")
							.addAttribute("time:timestamp", new Date(2024, 2, 6, 6, 00, 00))
							.addAttribute(ExogenousDatasetAttributes.EXOEVENTVALUE, 10.0)
						.addEvent("exogenous_datapoint_002")
							.addAttribute("time:timestamp", new Date(2024, 2, 7, 6, 00, 00))
							.addAttribute(ExogenousDatasetAttributes.EXOEVENTVALUE, 10.0)
						.addEvent("exogenous_datapoint_003")
							.addAttribute("time:timestamp", new Date(2024, 2, 8, 6, 00, 00))
							.addAttribute(ExogenousDatasetAttributes.EXOEVENTVALUE, 10.0)
					.build()
			)
			.build();
	
	static ExogenousDataset X = ExogenousDataset.builder()
			.dataType(ExogenousDatasetType.NUMERICAL)
			.linker( 
				AttributeLinker.builder()
					.attributeName("exogenous:link")
					.build()	
			)
			.linkType(ExogenousDatasetLinkType.TRACE_ATTRIBUTE_MATCH)
			.source( 
					XLogBuilder.newInstance()
						.startLog("X")
						.addTrace("xseries_01")
						.addAttribute("exogenous:link", "1")
							.addEvent("exogenous_datapoint_001")
								.addAttribute("time:timestamp", new Date(2024, 2, 8, 9, 00, 00))
								.addAttribute(ExogenousDatasetAttributes.EXOEVENTVALUE, 10.0)
							.addEvent("exogenous_datapoint_002")
								.addAttribute("time:timestamp", new Date(2024, 2, 8, 9, 42, 00))
								.addAttribute(ExogenousDatasetAttributes.EXOEVENTVALUE, 9.0)
							.addEvent("exogenous_datapoint_003")
								.addAttribute("time:timestamp", new Date(2024, 2, 8, 10, 12, 00))
								.addAttribute(ExogenousDatasetAttributes.EXOEVENTVALUE, 8.0)
							.addEvent("exogenous_datapoint_004")
								.addAttribute("time:timestamp", new Date(2024, 2, 8, 10, 37, 00))
								.addAttribute(ExogenousDatasetAttributes.EXOEVENTVALUE, 7.0)
							.addEvent("exogenous_datapoint_005")
								.addAttribute("time:timestamp", new Date(2024, 2, 8, 13, 30, 00))
								.addAttribute(ExogenousDatasetAttributes.EXOEVENTVALUE, 6.0)
							.addEvent("exogenous_datapoint_006")
								.addAttribute("time:timestamp", new Date(2024, 2, 8, 14, 25, 00))
								.addAttribute(ExogenousDatasetAttributes.EXOEVENTVALUE, 5.0)
							.addEvent("exogenous_datapoint_007")
								.addAttribute("time:timestamp", new Date(2024, 2, 8, 14, 47, 00))
								.addAttribute(ExogenousDatasetAttributes.EXOEVENTVALUE, 4.0)
							.addEvent("exogenous_datapoint_008")
								.addAttribute("time:timestamp", new Date(2024, 2, 8, 16, 32, 00))
								.addAttribute(ExogenousDatasetAttributes.EXOEVENTVALUE, 3.0)
							.addEvent("exogenous_datapoint_009")
								.addAttribute("time:timestamp", new Date(2024, 2, 8, 17, 12, 00))
								.addAttribute(ExogenousDatasetAttributes.EXOEVENTVALUE, 2.0)
							.addEvent("exogenous_datapoint_010")
								.addAttribute("time:timestamp", new Date(2024, 2, 8, 20, 00, 00))
								.addAttribute(ExogenousDatasetAttributes.EXOEVENTVALUE, 1.0)
						.addTrace("xseries_03")
						.addAttribute("exogenous:link", "3")
							.addEvent("exogenous_datapoint_001")
								.addAttribute("time:timestamp", new Date(2024, 2, 8, 9, 00, 00))
								.addAttribute(ExogenousDatasetAttributes.EXOEVENTVALUE, 25.0)
							.addEvent("exogenous_datapoint_002")
								.addAttribute("time:timestamp", new Date(2024, 2, 8, 9, 15, 00))
								.addAttribute(ExogenousDatasetAttributes.EXOEVENTVALUE, 20.0)
							.addEvent("exogenous_datapoint_003")
								.addAttribute("time:timestamp", new Date(2024, 2, 8, 9, 45, 00))
								.addAttribute(ExogenousDatasetAttributes.EXOEVENTVALUE, 18.0)
							.addEvent("exogenous_datapoint_004")
								.addAttribute("time:timestamp", new Date(2024, 2, 8, 10, 45, 00))
								.addAttribute(ExogenousDatasetAttributes.EXOEVENTVALUE, 18.0)
							.addEvent("exogenous_datapoint_005")
								.addAttribute("time:timestamp", new Date(2024, 2, 8, 11, 7, 00))
								.addAttribute(ExogenousDatasetAttributes.EXOEVENTVALUE, 16.0)
							.addEvent("exogenous_datapoint_006")
								.addAttribute("time:timestamp", new Date(2024, 2, 8, 11, 45, 00))
								.addAttribute(ExogenousDatasetAttributes.EXOEVENTVALUE, 14.0)
							.addEvent("exogenous_datapoint_007")
								.addAttribute("time:timestamp", new Date(2024, 2, 8, 12, 23, 00))
								.addAttribute(ExogenousDatasetAttributes.EXOEVENTVALUE, 14.0)
							.addEvent("exogenous_datapoint_008")
								.addAttribute("time:timestamp", new Date(2024, 2, 8, 13, 17, 00))
								.addAttribute(ExogenousDatasetAttributes.EXOEVENTVALUE, 12.0)
							.addEvent("exogenous_datapoint_009")
								.addAttribute("time:timestamp", new Date(2024, 2, 8, 17, 38, 00))
								.addAttribute(ExogenousDatasetAttributes.EXOEVENTVALUE, 10.0)
							.addEvent("exogenous_datapoint_010")
								.addAttribute("time:timestamp", new Date(2024, 2, 8, 12, 23, 00))
								.addAttribute(ExogenousDatasetAttributes.EXOEVENTVALUE, 5.0)
							.addEvent("exogenous_datapoint_011")
								.addAttribute("time:timestamp", new Date(2024, 2, 8, 13, 17, 00))
								.addAttribute(ExogenousDatasetAttributes.EXOEVENTVALUE, 3.0)
							.addEvent("exogenous_datapoint_012")
								.addAttribute("time:timestamp", new Date(2024, 2, 8, 17, 38, 00))
								.addAttribute(ExogenousDatasetAttributes.EXOEVENTVALUE, 1.0)
						.addTrace("xseries_04")
						.addAttribute("exogenous:link", "4")
							.addEvent("exogenous_datapoint_001")
								.addAttribute("time:timestamp", new Date(2024, 2, 8, 9, 00, 00))
								.addAttribute(ExogenousDatasetAttributes.EXOEVENTVALUE, 16.0)
							.addEvent("exogenous_datapoint_002")
								.addAttribute("time:timestamp", new Date(2024, 2, 8, 9, 15, 00))
								.addAttribute(ExogenousDatasetAttributes.EXOEVENTVALUE, 14.0)
							.addEvent("exogenous_datapoint_003")
								.addAttribute("time:timestamp", new Date(2024, 2, 8, 9, 45, 00))
								.addAttribute(ExogenousDatasetAttributes.EXOEVENTVALUE, 14.0)
							.addEvent("exogenous_datapoint_004")
								.addAttribute("time:timestamp", new Date(2024, 2, 8, 10, 45, 00))
								.addAttribute(ExogenousDatasetAttributes.EXOEVENTVALUE, 14.0)
							.addEvent("exogenous_datapoint_005")
								.addAttribute("time:timestamp", new Date(2024, 2, 8, 12, 7, 00))
								.addAttribute(ExogenousDatasetAttributes.EXOEVENTVALUE, 12.0)
							.addEvent("exogenous_datapoint_006")
								.addAttribute("time:timestamp", new Date(2024, 2, 8, 13, 45, 00))
								.addAttribute(ExogenousDatasetAttributes.EXOEVENTVALUE, 10.0)
							.addEvent("exogenous_datapoint_007")
								.addAttribute("time:timestamp", new Date(2024, 2, 8, 14, 23, 00))
								.addAttribute(ExogenousDatasetAttributes.EXOEVENTVALUE, 10.0)
							.addEvent("exogenous_datapoint_008")
								.addAttribute("time:timestamp", new Date(2024, 2, 8, 14, 17, 00))
								.addAttribute(ExogenousDatasetAttributes.EXOEVENTVALUE, 8.0)
							.addEvent("exogenous_datapoint_009")
								.addAttribute("time:timestamp", new Date(2024, 2, 8, 16, 38, 00))
								.addAttribute(ExogenousDatasetAttributes.EXOEVENTVALUE, 8.0)
							.addEvent("exogenous_datapoint_010")
								.addAttribute("time:timestamp", new Date(2024, 2, 8, 17, 23, 00))
								.addAttribute(ExogenousDatasetAttributes.EXOEVENTVALUE, 6.0)
							.addEvent("exogenous_datapoint_011")
								.addAttribute("time:timestamp", new Date(2024, 2, 8, 19, 17, 00))
								.addAttribute(ExogenousDatasetAttributes.EXOEVENTVALUE, 6.0)
							.addEvent("exogenous_datapoint_012")
								.addAttribute("time:timestamp", new Date(2024, 2, 8, 22, 38, 00))
								.addAttribute(ExogenousDatasetAttributes.EXOEVENTVALUE, 4.0)
						.build()
			)
			.build();
	
	@SuppressWarnings("deprecation")
	static XLog endogLog = XLogBuilder.newInstance()
			.startLog("Paper Example")
			.addTrace("trace_01")
			.addAttribute(new XAttributeLiteralImpl("exogenous:link", "1"))
			.addAttribute(new XAttributeLiteralImpl("exogenous:link:z", "1"))
				.addEvent("a")
				.addAttribute("time:timestamp", new Date(2024, 2, 8, 10, 00, 00))
				.addEvent("b")
				.addAttribute("time:timestamp", new Date(2024, 2, 8, 14, 00, 00))
				.addEvent("e")
				.addAttribute("time:timestamp", new Date(2024, 2, 8, 15, 00, 00))
				.addEvent("b")
				.addAttribute("time:timestamp", new Date(2024, 2, 8, 18, 00, 00))
				.addEvent("d")
				.addAttribute("time:timestamp", new Date(2024, 2, 9, 1, 00, 00))
			.addTrace("trace_02")
			.addAttribute(new XAttributeLiteralImpl("exogenous:link", "2"))
			.addAttribute(new XAttributeLiteralImpl("exogenous:link:z", "1"))
				.addEvent("a")
				.addAttribute("time:timestamp", new Date(2024, 2, 8, 10, 00, 00))
				.addEvent("c")
				.addAttribute("time:timestamp", new Date(2024, 2, 8, 12, 00, 00))
				.addEvent("b")
				.addAttribute("time:timestamp", new Date(2024, 2, 8, 18, 00, 00))
			.addTrace("trace_03")
			.addAttribute(new XAttributeLiteralImpl("exogenous:link", "3"))
			.addAttribute(new XAttributeLiteralImpl("exogenous:link:z", "1"))
				.addEvent("a")
				.addAttribute("time:timestamp", new Date(2024, 2, 8, 10, 00, 00))
				.addEvent("b")
				.addAttribute("time:timestamp", new Date(2024, 2, 8, 12, 00, 00))
				.addEvent("c")
				.addAttribute("time:timestamp", new Date(2024, 2, 8, 18, 00, 00))
				.addEvent("c")
				.addAttribute("time:timestamp", new Date(2024, 2, 8, 22, 00, 00))
				.addEvent("d")
				.addAttribute("time:timestamp", new Date(2024, 2, 9, 1, 00, 00))
			.addTrace("trace_04")
			.addAttribute(new XAttributeLiteralImpl("exogenous:link", "4"))
			.addAttribute(new XAttributeLiteralImpl("exogenous:link:z", "1"))
				.addEvent("a")
					.addAttribute("time:timestamp", new Date(2024, 2, 8, 10, 00, 00))
				.addEvent("f")
					.addAttribute("time:timestamp", new Date(2024, 2, 8, 14, 00, 00))
				.addEvent("e")
					.addAttribute("time:timestamp", new Date(2024, 2, 8, 15, 00, 00))
				.addEvent("z")
					.addAttribute("time:timestamp", new Date(2024, 2, 8, 18, 00, 00))
				.addEvent("d")
					.addAttribute("time:timestamp", new Date(2024, 2, 9, 1, 00, 00))
			.addTrace("trace_05")
			.addAttribute(new XAttributeLiteralImpl("exogenous:link", "5"))
			.addAttribute(new XAttributeLiteralImpl("exogenous:link:z", "1"))
				.addEvent("f")
					.addAttribute("time:timestamp", new Date(2024, 2, 8, 14, 00, 00))
				.addEvent("b")
					.addAttribute("time:timestamp", new Date(2024, 2, 8, 15, 00, 00))
				.addEvent("z")
					.addAttribute("time:timestamp", new Date(2024, 2, 8, 18, 00, 00))
				.addEvent("d")
					.addAttribute("time:timestamp", new Date(2024, 2, 9, 1, 00, 00))
			.build();
	
	static List<Determination> deters = new ArrayList<Determination>() {{
		add(Determination.builder()
			.linker(
					AttributeLinker.builder()
						.attributeName("exogenous:link")
						.build()
			)
			.slicer(
					PastOutcomeSlicer.builder()
						.build()
			)
			.transformer(
					new StochasticTransformer()
			)
			.build());
		add(Determination.builder()
				.linker(
						AttributeLinker.builder()
							.attributeName("exogenous:link:z")
							.build()
				)
				.slicer(
						PastOutcomeSlicer.builder()
							.build()
				)
				.transformer(
						new StochasticTransformer()
				)
				.build());
	}};
	
	static ExogenousAnnotatedLog xlog = ExogenousAnnotatedLog.builder()
			.parsed(false)
			.exogenousDataset(X)
			.exogenousDataset(Y)
			.exogenousDataset(Z)
			.endogenousLog(endogLog)
			.determinations(deters)
			.build();
	
	
	
	public AcceptingPetriNet buildNet() {
		Petrinet pnet = PetrinetFactory.newPetrinet("example_net");
		Place p1 = pnet.addPlace("p1");
		Place p2 = pnet.addPlace("p2");
		Place p3 = pnet.addPlace("p3");
		Place p4 = pnet.addPlace("p4");
		
		Transition t1 = pnet.addTransition("a");
		Transition t2 = pnet.addTransition("b");
		Transition t3 = pnet.addTransition("c");
		Transition t4 = pnet.addTransition("tau");
		t4.setInvisible(true);
		Transition t5 = pnet.addTransition("d");
		
		pnet.addArc(p1, t1);
		pnet.addArc(t1, p2);
		pnet.addArc(p2, t2);
		pnet.addArc(p2, t3);
		pnet.addArc(t2, p3);
		pnet.addArc(t3, p3);
		pnet.addArc(p3, t4);
		pnet.addArc(t4, p2);
		pnet.addArc(p3, t5);
		pnet.addArc(t5, p4);
		
		Marking initialMarking = new Marking(new ArrayList<Place>(){{ add(p1); }} );
		Marking finalMarking = new Marking(new ArrayList<Place>() {{ add(p4); }} );
		return new AcceptingPetriNetImpl(pnet, initialMarking , finalMarking);
	}

	@Test
	public void test() {
		try {
			xlog.getEndogenousLog().getClassifiers().add(
					new XEventNameClassifier()
			);
			SLPNEDDiscovery.discover(xlog, buildNet());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			org.junit.Assert.fail();
		}
	}

}
