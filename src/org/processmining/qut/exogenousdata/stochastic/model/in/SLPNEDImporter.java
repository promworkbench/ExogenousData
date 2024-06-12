package org.processmining.qut.exogenousdata.stochastic.model.in;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.framework.abstractplugins.AbstractImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.qut.exogenousdata.stochastic.model.StochasticLabelledPetriNetWithExogenousData;

@Plugin(
		name = "StochasticLabelledPetriNetWithExogenousData",
		parameterLabels = { "Filename" }, 
		returnLabels = {"StochasticLabelledPetriNetWithExogenousData" }, 
		returnTypes = { StochasticLabelledPetriNetWithExogenousData.class },
		userAccessible = true,
		level = PluginLevel.NightlyBuild
		)
@UIImportPlugin(
		description = "StochasticLabelledPetriNetWithExogenousData Importer", 
		extensions = { "slpned" })
public class SLPNEDImporter extends AbstractImportPlugin{

	public StochasticLabelledPetriNetWithExogenousData importFromStream(
			PluginContext context, 
			InputStream input, 
			String filename,
			long fileSizeInBytes) throws Exception {
		return read(input);
	}
	
	public static StochasticLabelledPetriNetWithExogenousData read(InputStream input) 
			throws NumberFormatException, IOException {

		StochasticLabelledPetriNetWithExogenousData result = new StochasticLabelledPetriNetWithExogenousData();
		
		BufferedReader r = new BufferedReader(new InputStreamReader(input));
		
		int numberOfAdjusters = Integer.parseInt(getNextLine(r));
		for(int adj=0; adj < numberOfAdjusters; adj++) {
			result.addExogenousFactor(getNextLine(r).replace("&#35;","#"));
		}
		
		int numberOfPlaces = Integer.parseInt(getNextLine(r));
		for (int place = 0; place < numberOfPlaces; place++) {
			result.addPlace();

			int inInitialMarking = Integer.parseInt(getNextLine(r));
			if (inInitialMarking > 0) {
				result.addPlaceToInitial(place);
			}
		}

		int numberOfTransitions = Integer.parseInt(getNextLine(r));
		for (int transition = 0; transition < numberOfTransitions; transition++) {
			String label = getNextLine(r);
			double bweight = Double.valueOf(getNextLine(r));
			Double[] adjusters = new Double[numberOfAdjusters];
			Double[] notadjusters = new Double[numberOfAdjusters];
			for(int adj=0; adj < numberOfAdjusters; adj++) {
				adjusters[adj] = Double.valueOf(getNextLine(r));
			}
			for(int adj=0; adj < numberOfAdjusters; adj++) {
				notadjusters[adj] = Double.valueOf(getNextLine(r));
			}
			
			if (label.startsWith("silent")) {
				result.addTransition(bweight, adjusters, notadjusters);
			} else if (label.startsWith("label ")) {
				result.addTransition(label.substring(6), bweight, adjusters, notadjusters);
			} else {
				throw new RuntimeException("invalid transition");
			}

			//incoming places
			{
				int numberOfIncomingPlaces = Integer.parseInt(getNextLine(r));
				for (int p = 0; p < numberOfIncomingPlaces; p++) {
					int place = Integer.parseInt(getNextLine(r));
					result.addPlaceTransitionArc(place, transition);
				}
			}

			//outgoing places
			{
				int numberOfOutgoingPlaces = Integer.parseInt(getNextLine(r));
				for (int p = 0; p < numberOfOutgoingPlaces; p++) {
					int place = Integer.parseInt(getNextLine(r));
					result.addTransitionPlaceArc(transition, place);
				}
			}
		}
		
		r.close();

		return result;
	}

	public static String getNextLine(BufferedReader r) throws IOException {
		String line = r.readLine();
		while (line != null && line.startsWith("#")) {
			line = r.readLine();
		}
		return line;
	}
}
