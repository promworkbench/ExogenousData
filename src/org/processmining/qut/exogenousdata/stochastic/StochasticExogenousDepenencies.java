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
import org.processmining.qut.exogenousdata.steps.slicing.data.SubSeries.Scaling;
import org.processmining.qut.exogenousdata.stochastic.conformance.eduEMSC;
import org.processmining.qut.exogenousdata.stochastic.discovery.SLPNEDDiscoverer;
import org.processmining.qut.exogenousdata.stochastic.discovery.SLPNEDDiscoveryOneShot;
import org.processmining.qut.exogenousdata.stochastic.discovery.SLPNEDDiscoveryOneShotWithContext;
import org.processmining.qut.exogenousdata.stochastic.discovery.SLPNEDDiscoveryTwoShotWithContext;
import org.processmining.qut.exogenousdata.stochastic.model.StochasticLabelledPetriNetWithExogenousData;
import org.processmining.qut.exogenousdata.stochastic.model.StochasticLabelledPetriNetWithExogenousData.WeightForm;

public class StochasticExogenousDepenencies {
	
	private static final String packageName = ExogenousDataStatics.packageName;
	private static final String version = ExogenousDataStatics.version;
	private static final String authora = ExogenousDataStatics.authora;
	private static final String authorEmail = ExogenousDataStatics.authoraEmail;
	private static final String authorAff = ExogenousDataStatics.authoraAff;
	
	private static Map<String,Class<? extends SLPNEDDiscoverer>> discoveryModes = 
			new HashMap<String,Class<? extends SLPNEDDiscoverer>>() {{
		put("one-shot", SLPNEDDiscoveryOneShotWithContext.class);
		put("two-shot", SLPNEDDiscoveryTwoShotWithContext.class);
//		put("batched-one-shot", SLPNEDDiscoveryBatchedOneShotWithContext.class);
//		put("batched-two-shot", SLPNEDDiscoveryBatchedTwoShotWithContext.class);
	}};
	
	@Plugin(
			name = "Discover Stochastic Exogenous Dependencies in a Petri Net (Exo-SLPN)(xLog)",
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
//		ask for the type of approach to use
		ProMPropertiesPanel wizard = new ProMPropertiesPanel("Parameters"
				+ " for Exo-slpn discovery");
		ProMComboBox<String> sbox = wizard.addComboBox("type of solving approach", 
				discoveryModes.keySet().toArray(new String[0])
		);
		sbox.setSelectedIndex(0);
		ProMComboBox<WeightForm> ebox = wizard.addComboBox("type of equation", 
				WeightForm.values()
		);
		ebox.setSelectedIndex(0);
		ProMComboBox<Scaling> tbox = wizard.addComboBox("time scaling", 
				Scaling.values()
		);
		tbox.setSelectedIndex(3);
		InteractionResult result = context.showConfiguration(
				"Configuration for generating playout tree",
				wizard);
		String mode = (String) sbox.getSelectedItem();
		WeightForm form = (WeightForm) ebox.getSelectedItem();
		Scaling scale = (Scaling) tbox.getSelectedItem();
		if (result != InteractionResult.CANCEL) {
	//		call method to return net
			SLPNEDDiscoverer disc = null;
			for(Entry<String, Class<? extends SLPNEDDiscoverer>> e : discoveryModes.entrySet()) {
				if (mode == e.getKey()) {
					disc = e.getValue().getConstructor(context.getClass()).newInstance(context);
				}
			}
			disc.configure(
				SLPNEDDiscoveryOneShot.DEFAULT_ROUNDING,
				SLPNEDDiscoveryOneShot.DEFAULT_BATCH, 
				scale, 
				SLPNEDDiscoveryOneShot.DEFAULT_SOLVING_VALUE, 
				form
			);
			return disc.discover(xlog, xlog.getExogenousDatasets(), net);
		}
		return null;
	}
	
	@Plugin(
			name = "Discover Stochastic Exogenous Dependencies in a Petri Net (Exo-SLPN)(Datasets)",
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
		ProMComboBox<String> sbox = wizard.addComboBox("type of solving approach", 
				discoveryModes.keySet().toArray(new String[0])
		);
		sbox.setSelectedIndex(0);
		ProMComboBox<WeightForm> ebox = wizard.addComboBox("type of equation", 
				WeightForm.values()
		);
		ebox.setSelectedIndex(0);
		ProMComboBox<Scaling> tbox = wizard.addComboBox("time scaling", 
				Scaling.values()
		);
		tbox.setSelectedIndex(3);
		InteractionResult result = context.showConfiguration(
				"Configuration for generating playout tree",
				wizard);
		String mode = (String) sbox.getSelectedItem();
		WeightForm form = (WeightForm) ebox.getSelectedItem();
		Scaling scale = (Scaling) tbox.getSelectedItem();
		if (result != InteractionResult.CANCEL) {
	//		handle datasets
			List<ExogenousDataset> temp = new ArrayList();
			for(ExogenousDataset dataset : datasets) {
				temp.add(dataset);
			}
	//		call method to return net
			SLPNEDDiscoverer disc = null;
			for(Entry<String, Class<? extends SLPNEDDiscoverer>> e : discoveryModes.entrySet()) {
				if (mode == e.getKey()) {
					disc = e.getValue().getConstructor(context.getClass()).newInstance(context);
				}
			}
			disc.configure(
				SLPNEDDiscoveryOneShot.DEFAULT_ROUNDING,
				SLPNEDDiscoveryOneShot.DEFAULT_BATCH, 
				scale, 
				SLPNEDDiscoveryOneShot.DEFAULT_SOLVING_VALUE, 
				form
			);
			return disc.discover(xlog, temp, net);
		}
		return null;
	}
	
	@Plugin(
			name = "Compute duEMSC for Exo-SLPN (Datasets).",
			parameterLabels = {"Log", "Exo-SLPN", "Exogenous Datasets"},
			returnLabels = {"duEMSC"},
			returnTypes = {HTMLToString.class},
			help="Computes duEMSC for an Exo-SLPN, by casting the exogenous factors to data attributes. [x] (TODO)"
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
		double measure = eduEMSC.measureLogModel(xlog, datasets, 
				new XEventNameClassifier(), model, true, canceller);
		
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
	
	@Plugin(
			name = "Compute duEMSC for Exo-SLPN (XLog).",
			parameterLabels = {"ExogenousAnnotatedLog", "Exo-SLPN"},
			returnLabels = {"duEMSC"},
			returnTypes = {HTMLToString.class},
			help="Computes duEMSC for an Exo-SLPN, by casting the exogenous factors to data attributes. [x] (TODO)"
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
	public HTMLToString computExogenouseduEMSC_XLOG(
			final UIPluginContext context,
			final ExogenousAnnotatedLog xlog, 
			final StochasticLabelledPetriNetWithExogenousData model) 
	{
		ProMCanceller canceller = new ProMCanceller() {
			public boolean isCancelled() {
				return context.getProgress().isCancelled();
			}
		};
		eduEMSC.setContext(context);
		double measure = eduEMSC.measureLogModel(xlog, 
				xlog.getExogenousDatasets().toArray(new ExogenousDataset[0]), 
				new XEventNameClassifier(), model, true, canceller);
		
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
