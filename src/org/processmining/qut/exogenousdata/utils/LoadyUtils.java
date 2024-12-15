package org.processmining.qut.exogenousdata.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetFactory;
import org.processmining.datapetrinets.io.DPNIOException;
import org.processmining.datapetrinets.io.DataPetriNetImporter;
import org.processmining.log.utils.XUtils;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PetriNetWithData;
import org.processmining.qut.exogenousdata.ab.jobs.ExoSLPNModelQualityTesting.FakeContext;

public class LoadyUtils {
	
	private LoadyUtils() {};
	
	/**
	 * Takes a pnml file to create a PetriNetWithData.
	 * Breaks silently, and if it does returns null instead of an object.
	 * @param pnmlFile The file for the pnml to parse and construct the DPN.
	 * @return either a DPN or null.
	 */
	public static PetriNetWithData loadDPNFromFile(File pnmlFile) {
		PetriNetWithData ret = null;
		try {
			ret = (PetriNetWithData)
					new DataPetriNetImporter()
					.importFromStream(new FileInputStream(pnmlFile))
					.getDPN();
		} catch (FileNotFoundException | DPNIOException e) {
			// failed to load dpn
			// breaking silently
		}
		return ret;
	}
	
	/**
	 * Takes a pnml file and creates a AcceptingPetriNet.
	 * May return null if something goes wrong
	 * @param pnmlFile
	 * @return AcceptingPetriNet or null if in case that parsing failed.
	 */
	public static AcceptingPetriNet loadAcceptingNetFromFile(File pnmlFile) {
		AcceptingPetriNet aNet = AcceptingPetriNetFactory.createAcceptingPetriNet();
		try {
			aNet.importFromStream(new FakeContext(), new FileInputStream(pnmlFile));
			return aNet;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static XLog loadLogFromFile(File logFile) {
		XLog ret = null;
		try {
			ret = XUtils.loadLog(logFile);
		} catch (Exception e) {
//			failed to load log
//			breaking silently
		}
		return ret;
	}
}
