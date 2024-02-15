package org.processmining.qut.exogenousaware.stochastic;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.qut.exogenousaware.ExogenousDataPlugins;
import org.processmining.qut.exogenousaware.data.ExogenousAnnotatedLog;
import org.processmining.qut.exogenousaware.stochastic.discovery.SLPNEDDiscovery;
import org.processmining.qut.exogenousaware.stochastic.model.StochasticLabelledPetriNetWithExogenousData;

public class StochasticExogenousDepenencies {
	
	private static final String version = ExogenousDataPlugins.version;
	
	@Plugin(
			name = "Discover Stochastic Exogenous Depenencies in a Petri Net.",
			parameterLabels = {"Exogenous Annotated Log", "Accepting Petri net"},
			returnLabels = {"Stochastic Labelled Petri Net with Exogenous Dependencies"},
			returnTypes = {StochasticLabelledPetriNetWithExogenousData.class},
			help="Discovers adjustment and base weights based on the following paper [x] (TODO)"
					+ version,
			categories={PluginCategory.Analytics, PluginCategory.Enhancement},
			userAccessible = true
	)
	@UITopiaVariant(
			affiliation = "QUT",
			author = "A. Banham",
			email = "adam.banham@hdr.qut.edu.au",
			pack = "ExogenousData"
	)
	public StochasticLabelledPetriNetWithExogenousData discoverySLPNED(final UIPluginContext context,
			final ExogenousAnnotatedLog xlog, final AcceptingPetriNet net) 
	throws Exception {
		return SLPNEDDiscovery.discover(xlog, net);
	}

}
