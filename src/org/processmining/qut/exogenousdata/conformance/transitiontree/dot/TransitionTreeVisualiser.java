package org.processmining.qut.exogenousdata.conformance.transitiontree.dot;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotEdge;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.qut.exogenousdata.conformance.transitiontree.PNWDTransitionTree;
import org.processmining.qut.exogenousdata.conformance.transitiontree.TTFlowWithGuard;
import org.processmining.qut.exogenousdata.conformance.transitiontree.TTNode;

public class TransitionTreeVisualiser {
	
	@Plugin(
			name = "(Prettier) Transition Tree Visualiser", 
			returnLabels = {"Dot visualization" }, 
			returnTypes = { JComponent.class }, 
			parameterLabels = {"Transition Tree", "canceller" }, 
			userAccessible = true, 
			level = PluginLevel.NightlyBuild)
	@Visualizer
	@PluginVariant(variantLabel = "(Prettier) Transition Tree Visualiser", 
			requiredParameterLabels = { 0, 1 })
	public JComponent  visualize(
			final PluginContext context,
			PNWDTransitionTree tree,
			ProMCanceller canceller) {
		return new DotPanel(generateDoTForTransitionTree(tree));
	}
	
	public Dot generateDoTForTransitionTree(PNWDTransitionTree tree) {
		Dot dot = new Dot();
		dot.setOption("rankdir", "LR");
		dot.setOption("bgcolor", "none");
		dot.setOption("pack", "true");
		dot.setOption("pad", "0.1");
		dot.setOption("compound", "true");
//		style nodes
		dot.setNodeOption("fixedsize", "true");
		dot.setNodeOption("width", "0.75");
		dot.setNodeOption("height", "0.75");
		dot.setNodeOption("fontname", "serif");
		dot.setNodeOption("fontsize", "12");
		dot.setNodeOption("fontcolor", "white");
		dot.setNodeOption("fillcolor", "cadetblue");
		dot.setNodeOption("style", "filled");
		dot.setNodeOption("shape", "circle");
		dot.setNodeOption("label", "");
		
		Map<TTNode, DotNode> nodes = new HashMap<>();
		for(TTNode node : tree.getNodes()) {
			DotNode dotNode = dot.addNode("<"
					+ node.getVariant().toString()
					+ ">");
			nodes.put(node, dotNode);
			if (node.isRoot()) {
				dotNode.setOption("shape", "doublecircle");
				dotNode.setOption("fillcolor", "aquamarine4");
			}
			if (node.isTerminal()) {
				dotNode.setOption("shape", "tripleoctagon");
				dotNode.setOption("fillcolor", "olivedrab4");
			}
		}
//		style flows
		dot.setEdgeOption("fontname", "serif");
		dot.setEdgeOption("fontcolor", "black");
		dot.setEdgeOption("labelfloat", "true");
		dot.setEdgeOption("minlen", "5");
		dot.setEdgeOption("fontsize", "10");
		dot.setEdgeOption("labeldistance", "4");
		
		int flowid = 1;
		for (TTFlowWithGuard flow : tree.getFlows()) {
			DotEdge dotEdge = dot.addEdge(
					nodes.get(flow.src()), 
					nodes.get(flow.tgt())
			);
			if (flow.hasGuard()) {
				dotEdge.setOption("taillabel", "<"
						+ "["
						+ flow.label()
						+ ","
						+ flow.guard().cannonRepresentation()
							.replace("<", "&lt;")
							.replace(">", "&gt;")
						+ "]>");
			} else {
				dotEdge.setOption("taillabel", "<"
						+ "["
						+ flow.label()
						+ ","
						+ "]>");
			}
			dotEdge.setOption("headlabel", "<"
					+ "<font color=\"darkgray\">"
					+ "f<sub>"
					+ flowid
					+ "</sub>"
					+ "</font>>");
			
			flowid += 1;
			
		}
		return dot;
	}

}
