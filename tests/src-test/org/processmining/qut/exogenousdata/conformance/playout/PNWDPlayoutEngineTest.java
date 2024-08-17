package org.processmining.qut.exogenousdata.conformance.playout;

import static org.junit.Assert.fail;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PetriNetWithData;
import org.processmining.qut.exogenousdata.utils.LoadyUtils;

public class PNWDPlayoutEngineTest {
	
	Path modelPath = Paths.get(
			"tests","src-test","resource", "axioms", "axiom 3",  "ax3_model_3.pnml");
	Path testingModelPath = Paths.get( 
			"tests", "src-test", "resource", "testing_dpn_with_rework.pnml");

	@Test
	public void justRunTest() {
		PetriNetWithData model = LoadyUtils.loadDPNFromFile(modelPath.toFile());
		if (model == null) {
			fail("Unable to load in testing pnml file for DPN.");
		}
		for(PlayoutTraceWithGuards playout : 
			new PNWDPlayoutEngine().generateTraces(model, 6)) {
//			System.out.println(playout);
			if (playout == null) {
				fail("All playouts should be initialised.");
			}
		}
	}
	
	@Test
	public void playoutLengthTest() {
		PetriNetWithData model = LoadyUtils.loadDPNFromFile(modelPath.toFile());
		if (model == null) {
			fail("Unable to load in testing pnml file for DPN.");
		}
		for(int n=0; n < 10; n++) {
			System.out.println("testing length :: n="+n);
			for(PlayoutTraceWithGuards playout : 
				new PNWDPlayoutEngine().generateTraces(model, n)) {
				int len = 0;
				for( PlayoutStep step : playout.getSteps()) {
					len += 1;
				}
				if (len > n+1) {
					System.out.println("Longer than expected :: " + playout);
					fail("Generated a playout longer than n="+n);
				}
			}
		}

	}
	
	@Test
	public void playoutReworkTest() {
		PetriNetWithData model = LoadyUtils.loadDPNFromFile(testingModelPath.toFile());
		if (model == null) {
			fail("Unable to load in testing pnml file for DPN.");
		}
		for(int n=0; n < 6; n++) {
			System.out.println("testing length :: n="+n);
			for(PlayoutTraceWithGuards playout : 
				new PNWDPlayoutEngine().generateTraces(model, n)) {
				int len = 0;
				for( PlayoutStep step : playout.getSteps()) {
					len += 1;
				}
				if (len > n+1) {
					System.out.println("Longer than expected :: " + playout);
					fail("Generated a playout longer than n="+n);
				}
//				System.out.println(playout);
			}
		}

	}
	
	

}
