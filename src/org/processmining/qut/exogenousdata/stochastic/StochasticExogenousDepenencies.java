package org.processmining.qut.exogenousdata.stochastic;

import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.util.HTMLToString;
import org.processmining.qut.exogenousdata.ExogenousDataStatics;
import org.processmining.qut.exogenousdata.data.ExogenousAnnotatedLog;
import org.processmining.qut.exogenousdata.data.ExogenousDataset;
import org.processmining.qut.exogenousdata.stochastic.conformance.eduEMSC;
import org.processmining.qut.exogenousdata.stochastic.discovery.SLPNEDDiscovery;
import org.processmining.qut.exogenousdata.stochastic.model.StochasticLabelledPetriNetWithExogenousData;

public class StochasticExogenousDepenencies {
	
	private static final String packageName = ExogenousDataStatics.packageName;
	private static final String version = ExogenousDataStatics.version;
	private static final String authora = ExogenousDataStatics.authora;
	private static final String authorEmail = ExogenousDataStatics.authoraEmail;
	private static final String authorAff = ExogenousDataStatics.authoraAff;
	
	@Plugin(
			name = "Discover Stochastic Exogenous Dependencies in a Petri Net (Exo-SLPN)(xLog).",
			parameterLabels = {"Exogenous Annotated Log", "Accepting Petri net"},
			returnLabels = {"Exo-SLPN"},
			returnTypes = {StochasticLabelledPetriNetWithExogenousData.class},
			help="Discovers adjustment and base weights based on the following paper [x] (TODO)"
					+ version,
			categories={PluginCategory.Analytics, PluginCategory.Enhancement},
			userAccessible = true
	)
	@UITopiaVariant(
			affiliation = authorAff,
			author = authora,
			email = authorEmail,
			pack = packageName
	)
	public StochasticLabelledPetriNetWithExogenousData discoverySLPNEDFromLog(final UIPluginContext context,
			final ExogenousAnnotatedLog xlog, final AcceptingPetriNet net) 
	throws Exception {
		return SLPNEDDiscovery.discoverFromLog(xlog, net);
	}
	
	@Plugin(
			name = "Discover Stochastic Exogenous Dependencies in a Petri Net (Exo-SLPN)(Datasets).",
			parameterLabels = {"Exogenous Annotated Log", "Accepting Petri net", "Exogenous Datasets"},
			returnLabels = {"Exo-SLPN"},
			returnTypes = {StochasticLabelledPetriNetWithExogenousData.class},
			help="Discovers adjustment and base weights based on the following paper [x] (TODO)"
					+ version,
			categories={PluginCategory.Analytics, PluginCategory.Enhancement},
			userAccessible = true
	)
	@UITopiaVariant(
			affiliation = authorAff,
			author = authora,
			email = authorEmail,
			pack = packageName
	)
	public StochasticLabelledPetriNetWithExogenousData discoverySLPNEDFromSets(final UIPluginContext context,
			final XLog xlog, final AcceptingPetriNet net, final ExogenousDataset[] datasets) 
	throws Exception {
		List<ExogenousDataset> temp = new ArrayList();
		for(ExogenousDataset dataset : datasets) {
			temp.add(dataset);
		}
		return SLPNEDDiscovery.discover(xlog, temp , net);
	}
	
	@Plugin(
			name = "Compute duEMSC for Exo-SLPN (Datasets).",
			parameterLabels = {"Log", "Exo-SLPN", "Exogenous Datasets"},
			returnLabels = {"duEMSC"},
			returnTypes = {HTMLToString.class},
			help="Computes duEMSC for an SLPNED, by casting the exogenous factors to data attributes. [x] (TODO)"
					+ version,
			categories={PluginCategory.ConformanceChecking},
			userAccessible = true
	)
	@UITopiaVariant(
			affiliation = authorAff,
			author = authora,
			email = authorEmail,
			pack = packageName
	)
	public HTMLToString computExogenouseduEMSC(
			final UIPluginContext context,
			final XLog xlog, 
			final StochasticLabelledPetriNetWithExogenousData model, 
			final ExogenousDataset[] datasets) 
	throws Exception {
		ProMCanceller canceller = new ProMCanceller() {
			public boolean isCancelled() {
				return context.getProgress().isCancelled();
			}
		};
		double measure = eduEMSC.measureLogModel(xlog, datasets, new XEventNameClassifier(), model, true, canceller);
		return new HTMLToString() {

			public String toHTMLString(boolean includeHTMLTags) {
				return "Data-aware Earth Movers' Stochastic Conformance: " + measure;
			}
		};
	}
	

}
