package org.processmining.qut.exogenousdata.conformance.playout;

import static org.junit.Assert.fail;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.processmining.datapetrinets.io.DPNIOException;
import org.processmining.datapetrinets.io.DataPetriNetImporter;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PetriNetWithData;

public class PNWDPlayoutEngineTest {
	
	Path modelPath = Paths.get(
			"tests","src-test","resource", "ax3_model_1.pnml");

	@Test
	public void justRunTest() {
		PetriNetWithData model = null;
		try {
			model = (PetriNetWithData)
					new DataPetriNetImporter()
					.importFromStream(new FileInputStream(modelPath.toFile()))
					.getDPN();
		} catch (FileNotFoundException | DPNIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		}
		for(PlayoutTraceWithGuards playout : 
			new PNWDPlayoutEngine().generateTraces(model, 6)) {
			System.out.println(playout);
		}
	}
	
	@Test
	public void playoutLengthTest() {
		PetriNetWithData model = null;
		try {
			model = (PetriNetWithData)
					new DataPetriNetImporter()
					.importFromStream(new FileInputStream(modelPath.toFile()))
					.getDPN();
		} catch (FileNotFoundException | DPNIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		}
		for(int n=0; n < 10; n++) {
			System.out.println("testing length :: n="+n);
			for(PlayoutTraceWithGuards playout : 
				new PNWDPlayoutEngine().generateTraces(model, n)) {
				int len = 0;
				for( PlayoutStep step : playout.getSteps()) {
					len += 1;
				}
				if (len > n) {
					System.out.println("Longer than expected :: " + playout);
					fail("Generated a playout longer than n="+n);
				}
			}
		}

	}

}
