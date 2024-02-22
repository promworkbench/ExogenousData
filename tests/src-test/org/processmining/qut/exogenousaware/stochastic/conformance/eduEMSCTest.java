package org.processmining.qut.exogenousaware.stochastic.conformance;

import static org.junit.Assert.fail;

import org.deckfour.xes.classification.XEventNameClassifier;
import org.junit.Test;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.qut.exogenousaware.data.ExogenousDataset;
import org.processmining.qut.exogenousaware.stochastic.discovery.SLPNEDDiscoveryTest;

import lpsolve.LpSolveException;

public class eduEMSCTest {

	@Test
	public void testeduEMSC() {
		ProMCanceller canceller = new ProMCanceller() {
			public boolean isCancelled() {
				return false;
			}
		};
		
		try {
			double measure = eduEMSC.measureLogModel(
					SLPNEDDiscoveryTest.endogLog,
					SLPNEDDiscoveryTest.xlog.getExogenousDatasets().toArray(new ExogenousDataset[3]),
					new XEventNameClassifier(),
					SLPNEDDiscoveryTest.getNet(),
					true,
					canceller
			);
		} catch (LpSolveException e) {
			// TODO Auto-generated catch block
//			e.getCause().printStackTrace();
			e.printStackTrace();
			fail("Not yet implemented");
		} catch (Exception e) {
			// TODO Auto-generated catch bloc
			System.out.println(e.getMessage());
			e.printStackTrace();
			fail("Not yet implemented");
		}
		
	}

}
