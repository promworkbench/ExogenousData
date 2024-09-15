package org.processmining.qut.exogenousdata.stochastic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.util.HTMLToString;
import org.processmining.framework.util.ui.widgets.ProMComboBox;
import org.processmining.framework.util.ui.widgets.ProMPropertiesPanel;
import org.processmining.qut.exogenousdata.ExogenousDataStatics;
import org.processmining.qut.exogenousdata.data.ExogenousAnnotatedLog;
import org.processmining.qut.exogenousdata.data.ExogenousDataset;
import org.processmining.qut.exogenousdata.stochastic.conformance.eduEMSC;
import org.processmining.qut.exogenousdata.stochastic.discovery.SLPNEDDiscoverer;
import org.processmining.qut.exogenousdata.stochastic.discovery.SLPNEDDiscoveryBatchedOneShotWithContext;
import org.processmining.qut.exogenousdata.stochastic.discovery.SLPNEDDiscoveryBatchedTwoShotWithContext;
import org.processmining.qut.exogenousdata.stochastic.discovery.SLPNEDDiscoveryOneShotWithContext;
import org.processmining.qut.exogenousdata.stochastic.discovery.SLPNEDDiscoveryTwoShotWithContext;
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
		
		return new SLPNEDDiscoveryOneShotWithContext(context).discoverFromLog(xlog, net);
	}
	
	
	private static Map<String,Class<? extends SLPNEDDiscoverer>> discoveryModes = 
			new HashMap<String,Class<? extends SLPNEDDiscoverer>>() {{
		put("one-shot", SLPNEDDiscoveryOneShotWithContext.class);
		put("two-shot", SLPNEDDiscoveryTwoShotWithContext.class);
		put("batched-one-shot", SLPNEDDiscoveryBatchedOneShotWithContext.class);
		put("batched-two-shot", SLPNEDDiscoveryBatchedTwoShotWithContext.class);
	}};
	
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
//		ask for the type of approach to use
		ProMPropertiesPanel wizard = new ProMPropertiesPanel("Parameters"
				+ " for Exo-slpn discovery");
		ProMComboBox<String> box = wizard.addComboBox("type of solving approach", 
				discoveryModes.keySet().toArray(new String[0])
		);
		box.setSelectedIndex(0);
		InteractionResult result = context.showConfiguration(
				"Configuration for generating playout tree",
				wizard);
		String mode = (String) box.getSelectedItem();
		if (result != InteractionResult.CANCEL) {
	//		handle datasts
			List<ExogenousDataset> temp = new ArrayList();
			for(ExogenousDataset dataset : datasets) {
				temp.add(dataset);
			}
	//		call method to return net
			for(Entry<String, Class<? extends SLPNEDDiscoverer>> e : discoveryModes.entrySet()) {
				if (mode == e.getKey()) {
					SLPNEDDiscoverer disc = e.getValue().getConstructor(context.getClass()).newInstance(context);
					return disc.discover(xlog, temp, net);
				}
			}
			throw new Exception("Unknown Discovery method selected.");
		}
		return null;
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
	{
		ProMCanceller canceller = new ProMCanceller() {
			public boolean isCancelled() {
				return context.getProgress().isCancelled();
			}
		};
		eduEMSC.setContext(context);
		double measure = eduEMSC.measureLogModel(xlog, datasets, new XEventNameClassifier(), model, true, canceller);
		
		String name;
		try {
			name = model.getName();
		} catch (Exception e) {
			System.out.println("[eduEMSC] unable to get net's name.");
			name = "exo-slpn";
		}
		final String outName = name;
		return new HTMLToString() {

			public String toHTMLString(boolean includeHTMLTags) {
				return "(for model '"
						+ outName
						+"')<br/>"
						+ "Data-aware Earth Movers' Stochastic Conformance: " + measure;
			}
		};
	}
	

}
