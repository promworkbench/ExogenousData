package org.processmining.qut.exogenousdata.stochastic.model.dot;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

import java.text.DecimalFormat;
import java.util.Map;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.qut.exogenousdata.ab.jobs.Tuple;
import org.processmining.qut.exogenousdata.stochastic.model.StochasticLabelledPetriNetWithExogenousData;
import org.processmining.stochasticlabelledpetrinets.plugins.StochasticLabelledPetriNetVisualisationPlugin;

public class SLPNEDPrettierVisualizer extends StochasticLabelledPetriNetVisualisationPlugin<StochasticLabelledPetriNetWithExogenousData>{
	
	
	@Plugin(
			name = "(Prettier) Stochastic labelled Petri net with "
					+ "Exogenous Data visualisation", 
			returnLabels = {"Dot visualization" }, 
			returnTypes = { JComponent.class }, 
			parameterLabels = {"slpned", "canceller" }, 
			userAccessible = true, 
			level = PluginLevel.NightlyBuild)
	@Visualizer
	@PluginVariant(variantLabel = "(Prettier) Stochastic labelled Petri with "
			+ "Exogenous Data visualisation", 
			requiredParameterLabels = { 0, 1 })
	public JComponent  visualize(final PluginContext context, StochasticLabelledPetriNetWithExogenousData net,
			ProMCanceller canceller) {
		return adjustDiGraph(visualise(net));
	}
	
	public DotPanel adjustDiGraph(DotPanel panel) {
		Dot dot = panel.getDot();
		dot.setOption("bgcolor", "none");
		dot.setOption("dpi", "50");
		return new DotPanel(dot);
	}
	
//	String templates
	public static final String WeightFormat ="%.4f";
	public static final String PlaceFill = "#f2f2f2";
	public static final String StartingPlaceFill = "#80ff00";
	public static final String EndingPlaceFill = "#FF3939";
	public static final String TransitionFill = "#e9c6af";
	public static final String TauFill = "#808080";
	public static final String WeightFill = "#c0bbbb";


	public void decoratePlace(
			StochasticLabelledPetriNetWithExogenousData net,
			int place, 
			DotNode dotNode) {	
		if (net.isInInitialMarking(place) > 0) {
			dotNode.setOption("width", "1.2");
			dotNode.setOption("fillcolor", StartingPlaceFill);
		}
		else if (net.getOutputTransitions(place).length < 1) {
			dotNode.setOption("width", "1.2");
			dotNode.setOption("fillcolor", EndingPlaceFill);
		} else {
			dotNode.setOption("fillcolor", PlaceFill);
		}
		dotNode.setOption("style", "filled");
	}

	public void decorateTransition(
			StochasticLabelledPetriNetWithExogenousData net, 
			int transition, 
			DotNode dotNode) {
//		remake the label of the transition to showcase weights
		String label = escapeHtml(net.getTransitionLabel(transition));
		DecimalFormat df = new DecimalFormat("0.000");
		df.setMaximumFractionDigits(6);
		df.setMinimumFractionDigits(6);
		double base = net.getBaseWeight(transition);
		Map<String, Tuple<Double, Double>> adjusters = net.getAdjustments(transition);
//		change shape
		dotNode.setOption("style", "rounded, filled");
		dotNode.setOption("fontsize", "12");
		dotNode.setOption("fontname", "Helvetica");
		String dotLabel = "";
		String headLabel = "";
		if (net.isTransitionSilent(transition)) {
			dotNode.setOption("fillcolor", TauFill);
			headLabel = "<"
					+ "<TABLE"
					+ " BORDER=\"0\" "
					+ "><TR>"
					+ "<TD COLSPAN=\"3\">"
					+ "<FONT POINT-SIZE=\"16\" >"
					+ "&#120591;"
					+ "</FONT>"
					+ "<FONT POINT-SIZE=\"10\">(" 
					+ transition 
					+")</FONT></TD>"
					+ "</TR>"
					+ "<TR>"
					+ "<TD ALIGN=\"LEFT\" COLSPAN=\"3\">"
					+ "<FONT ALIGN=\"LEFT\" POINT-SIZE=\"10\" >"
					+ "<I>Base (&Phi;):</I>"
					+ "</FONT>"
					+ "</TD>"
					+ "</TR>"
					+ "<TR>"
					+ "<TD BORDER=\"1\" BGCOLOR=\"#c0bbbb\" "
					+ "STYLE=\"ROUNDED,DASHED\" "
					+ "CELLPADDING=\"5\" COLSPAN=\"3\" "
					+ ">"
					+ df.format(base)
					+ "</TD>"
					+ "</TR>";
		} else {
			dotNode.setOption("fillcolor", TransitionFill);
			headLabel = "<"
					+ "<TABLE"
					+ " BORDER=\"0\" "
					+ "><TR>"
					+ "<TD COLSPAN=\"3\">" 
					+ net.getTransitionLabel(transition)
					+"</TD>"
					+ "</TR>"
					+ "<TR>"
					+ "<TD ALIGN=\"LEFT\" COLSPAN=\"3\">"
					+ "<FONT ALIGN=\"LEFT\" POINT-SIZE=\"10\" >"
					+ "<I>Base (&Phi;):</I>"
					+ "</FONT>"
					+ "</TD>"
					+ "</TR>"
					+ "<TR>"
					+ "<TD BORDER=\"1\" BGCOLOR=\"#c0bbbb\" "
					+ "STYLE=\"ROUNDED,DASHED\" "
					+ "CELLPADDING=\"5\" COLSPAN=\"3\" "
					+ ">"
					+ df.format(base)
					+ "</TD>"
					+ "</TR>";
		}
		StringBuilder parameters = new StringBuilder();
		boolean addedprow = false;
		parameters.append("<TR>"
				+ "<TD>"
				+ "var"
				+ "</TD>"
				+ "<TD>"
				+ "adjuster (&phi;)"
				+ "</TD>"
				+ "<TD>"
				+ "alter (&psi;)"
				+ "</TD>"
				+ "</TR>");
//		add rows in pairs, one side for adjustment and other for not adjustment (lacking exogenous data)
		for(String adjuster : adjusters.keySet()) {
			StringBuilder prow = new StringBuilder();
			boolean keep = false;
			prow.append("<TR>"
					+ "<TD "
					+ "CELLPADDING=\"2\" "
					+ ">"
					+ adjuster
					+ "</TD>"
			);
			
			Tuple<Double, Double> adjustments = adjusters.get(adjuster);
			double adjusterValue = adjustments.getLeft();
			double alterValue = adjustments.getRight();
			if (adjusterValue < 1 || adjusterValue > 1) {
				prow.append("<TD "
						+ "BORDER=\"1\" BGCOLOR=\"#c0bbbb\" " 
						+ "STYLE=\"ROUNDED,DASHED\" " 
						+ "CELLPADDING=\"5\" "
						+ ">"
						+ " * "
						+ df.format(adjusterValue)
						+ " ^ |"
						+ adjuster
						+ "| "
						+ "</TD>");
				keep = true;
			} else {
				prow.append("<TD> </TD>");
			}
			if (alterValue < 1 || alterValue > 1) {
				prow.append("<TD "
						+ "BORDER=\"1\" BGCOLOR=\"#c0bbbb\" " 
						+ "STYLE=\"ROUNDED,DASHED\" " 
						+ "CELLPADDING=\"5\" "
						+ ">"
						+ " * "
						+ df.format(alterValue)
						+ "</TD>");
				keep = true;
			} else {
				prow.append("<TD> </TD>");
			}
//			dotLabel += "{";
//			dotLabel += "<f"+adjustHook+"l>  &#966;"+adjuster+"="
//					+ String.format(WeightFormat, adjustments.getLeft())
//					+ "| <f"+adjustHook+"r> &#966;&not;"+adjuster+"="
//					+ String.format(WeightFormat, adjustments.getRight());
//			dotLabel += "} |\n";
			prow.append("</TR>");
			if (keep) {
				parameters.append(prow.toString());
				addedprow = true;
			}
		}
		dotLabel = headLabel;
		if (addedprow) {
			dotLabel += parameters.toString();
		}
		dotLabel += "</TABLE> >";
		dotNode.setOption("label", dotLabel);
	}

}
