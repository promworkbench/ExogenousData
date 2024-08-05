package org.processmining.qut.exogenousdata.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.processmining.datapetrinets.io.DPNIOException;
import org.processmining.datapetrinets.io.DataPetriNetImporter;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PetriNetWithData;

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
}
