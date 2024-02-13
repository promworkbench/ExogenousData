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
import org.processmining.framework.util.ui.widgets.helper.UserCancelledException;
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

import nl.tue.astar.AStarException;

public class SLPNEDDiscoveryTest {
	
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
						.startLog("Exogenous Dataset X")
						.addTrace("xseries_01_a")
						.addAttribute("exogenous:link", "2")
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
						.build()
			)
			.build();
	
	@SuppressWarnings("deprecation")
	static XLog endogLog = XLogBuilder.newInstance()
			.startLog("Paper Example")
			.addTrace("trace_01")
			.addAttribute(new XAttributeLiteralImpl("exogenous:link", "1"))
				.addEvent("a")
				.addAttribute("time:timestamp", new Date(2024, 2, 8, 10, 00, 00))
				.addEvent("c")
				.addAttribute("time:timestamp", new Date(2024, 2, 8, 12, 00, 00))
				.addEvent("b")
				.addAttribute("time:timestamp", new Date(2024, 2, 8, 18, 00, 00))
			.addTrace("trace_02")
			.addAttribute(new XAttributeLiteralImpl("exogenous:link", "2"))
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
			.addTrace("trace_03")
			.addAttribute(new XAttributeLiteralImpl("exogenous:link", "3"))
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
			.addTrace("trace_04")
			.addAttribute(new XAttributeLiteralImpl("exogenous:link", "4"))
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
	}};
	
	static ExogenousAnnotatedLog xlog = ExogenousAnnotatedLog.builder()
			.parsed(false)
			.exogenousDataset(X)
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
		} catch (AStarException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UserCancelledException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
