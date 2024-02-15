package org.processmining.qut.exogenousaware.stochastic.model.out;

import java.io.File;
import java.io.IOException;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UIExportPlugin;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.qut.exogenousaware.stochastic.model.StochasticLabelledPetriNetWithExogenousData;

@Plugin(name = "StochasticLabelledPetriNetWithExogenousData exporter", 
	returnLabels = {}, returnTypes = {}, 
	parameterLabels = {"slpned", "File" }, 
	userAccessible = true, level = PluginLevel.NightlyBuild)
@UIExportPlugin(description = "Stochastic Labelled Petri Net With Exogenous Data", extension = "slpned")
public class SLPNEDExporter {
	
	@PluginVariant(variantLabel = "Export Stochastic Labelled Petri Net With Exogenous Data to file", requiredParameterLabels = { 0, 1 })
	public void exportDefault(UIPluginContext context, StochasticLabelledPetriNetWithExogenousData net, File file) throws IOException {
		export(net, file);
	}
	
	public static void export(StochasticLabelledPetriNetWithExogenousData net, File file) throws IOException {
		net.exportNet(file);
	}

}
