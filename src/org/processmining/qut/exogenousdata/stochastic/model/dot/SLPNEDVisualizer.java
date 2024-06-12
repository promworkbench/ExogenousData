package org.processmining.qut.exogenousdata.stochastic.model.dot;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

import java.util.Map;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.qut.exogenousdata.ab.jobs.Tuple;
import org.processmining.qut.exogenousdata.stochastic.model.StochasticLabelledPetriNetWithExogenousData;
import org.processmining.stochasticlabelledpetrinets.plugins.StochasticLabelledPetriNetVisualisationPlugin;

public class SLPNEDVisualizer extends StochasticLabelledPetriNetVisualisationPlugin<StochasticLabelledPetriNetWithExogenousData>{
	
	
	@Plugin(name = "Stochastic labelled Petri net with Exogenous Data visualisation", returnLabels = {
	"Dot visualization" }, returnTypes = { JComponent.class }, parameterLabels = {
			"slpned", "canceller" }, userAccessible = true, level = PluginLevel.NightlyBuild)
	@Visualizer
	@PluginVariant(variantLabel = "Stochastic labelled Petri with Exogenous Data visualisation", requiredParameterLabels = { 0, 1 })
	public JComponent  visualize(final PluginContext context, StochasticLabelledPetriNetWithExogenousData net,
			ProMCanceller canceller) {
		return visualise(net);
	}


	public void decoratePlace(StochasticLabelledPetriNetWithExogenousData net, int place, DotNode dotNode) {
//		do nothing for places.		
	}

	
//	String templates
	public static final String WeightFormat ="%.4f";

	public void decorateTransition(StochasticLabelledPetriNetWithExogenousData net, int transition, DotNode dotNode) {
//		remake the label of the transition to showcase weights
		String label = escapeHtml(net.getTransitionLabel(transition));
		double base = net.getBaseWeight(transition);
		Map<String, Tuple<Double, Double>> adjusters = net.getAdjustments(transition);
//		change shape
		dotNode.setOption("shape", "record");
		dotNode.setOption("style", "rounded, filled");
		dotNode.setOption("fontsize", "12");
		dotNode.setOption("fontname", "Helvetica");
		String dotLabel = "";
//		add top rows (transition label) and base weight
		int adjustHook = 0;
		dotLabel += "<f"+adjustHook+"> "+label+" |\n";
		adjustHook++;
		dotLabel += "<f"+adjustHook+"> &#981;=" + String.format(WeightFormat, base) + " |\n";
		adjustHook++;
//		add rows in pairs, one side for adjustment and other for not adjustment (lacking exogenous data)
		for(String adjuster : adjusters.keySet()) {
			Tuple<Double, Double> adjustments = adjusters.get(adjuster);
			dotLabel += "{";
			dotLabel += "<f"+adjustHook+"l>  &#966;"+adjuster+"="
					+ String.format(WeightFormat, adjustments.getLeft())
					+ "| <f"+adjustHook+"r> &#966;&not;"+adjuster+"="
					+ String.format(WeightFormat, adjustments.getRight());
			dotLabel += "} |\n";
		}
		dotLabel = dotLabel.substring(0, dotLabel.length()-3);
		dotNode.setOption("label", dotLabel);
	}

}
