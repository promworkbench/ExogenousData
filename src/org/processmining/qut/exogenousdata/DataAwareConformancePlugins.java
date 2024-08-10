package org.processmining.qut.exogenousdata;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.time.StopWatch;
import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.util.HTMLToString;
import org.processmining.framework.util.ui.widgets.ProMPropertiesPanel;
import org.processmining.framework.util.ui.widgets.ProMTextField;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PetriNetWithData;
import org.processmining.qut.exogenousdata.conformance.BookkeepingMeasures;
import org.processmining.qut.exogenousdata.conformance.Determinism;
import org.processmining.qut.exogenousdata.conformance.playout.PNWDPlayoutEngine;
import org.processmining.qut.exogenousdata.conformance.transitiontree.PNWDTransitionTree;

/**
 * Plugins for performing conformance checking <br>
 * <br>
 * Current plugins are:<br>
 * <ul>
 * <li> Determinism for models with guards [1,2] </li>
 * <li> Guard-Recall for models with guards [3] </li>
 * <li> Guard-Precision for models with guards [3] </li>
 *</ul>
 *<br>
 *[1] 	A. Banham, S. J. J. Leemans, M. T. Wynn, R. Andrews, xPM: A framework 
 *       for process mining with exogenous data, in: Process Mining Workshops - 
 *       ICPM 2021 International Workshops, volume 433 of Lecture Notes in 
 *       Business Information Processing, Springer, 2021, pp. 85-97.
 *[2]	xPM: Enhancing Exogenous Data Visibility. Adam Banham, Sander J.J. 
 *		 Leemans, Moe T. Wynn, Robert Andrews, Kevin B. Laupland, Lucy Shinners.
 *		 Artificial Intelligence in Medicine 2022 (Accepted as-of 24/09/2022).
 *[3]	Comparing Conformance Checking for Decision Mining: An Axiomatic Approach
 *		 IEEE Access 2024, Adam Banham, Arthur H. M. ter Hofstede, 
 *		 Sander J.J. Leemans, Felix Mannhardt, Robert Andrews, Moe T. Wynn
 *
 *@author Adam Banham
*/
public class DataAwareConformancePlugins {
	
	private static final String packageName = ExogenousDataStatics.packageName;
	private static final String version = ExogenousDataStatics.version;
	private static final String authora = ExogenousDataStatics.authora;
	private static final String authorEmail = ExogenousDataStatics.authoraEmail;
	private static final String authorAff = ExogenousDataStatics.authoraAff;
	
	@Plugin(
			name = "Generate (Many) Playout Transition Trees (DPN)",
			parameterLabels = {"PetriNetWithData (DPN)"},
			returnLabels = {"Transition Tree"},
			returnTypes = {PNWDTransitionTree.class},
			help="Computes a playout transition tree for a given graph, "
					+ "25 times for testing purposes on runtime."
				 + version,
			categories={PluginCategory.Discovery,PluginCategory.ConformanceChecking},
			userAccessible = true
	)
	@UITopiaVariant(
			affiliation = authorAff,
			author = authora,
			email = authorEmail,
			pack = packageName
	)
	public PNWDTransitionTree generatePlayoutTrees(final UIPluginContext context, 
			PetriNetWithData dpn) {
//		ask for a length
		ProMPropertiesPanel wizard = new ProMPropertiesPanel("Parameters"
				+ " Playout Tree");
		ProMTextField texter = wizard.addTextField("longest trace (k)", "5");
		InteractionResult result = context.showConfiguration(
				"Configuration for generating playout tree",
				wizard);
		int len = Integer.parseInt(texter.getText());
		List<Long> times= new ArrayList();
		StopWatch watch = new StopWatch();
		PNWDTransitionTree tree = null;
		for(int i=0;i<25;i++) {
			watch.start();
			tree = new PNWDTransitionTree(
					new PNWDPlayoutEngine(context)
						.generateTraces(dpn, 
							len
						)
			);
			watch.split();
			times.add(watch.getSplitTime());
			watch.reset();
			if (context.getProgress().isCancelled()) {
				break;
			}
		}
		long mean = times.stream()
				.reduce(new Long(0),Long::sum);
		System.out.println("[generatePlayoutTrees] "
				+ "mean runtime :: "
				+mean/times.size()+"ms");
		System.out.println("[generatePlayoutTrees] "
				+ "total runtime :: "
				+mean+"ms");
		return tree;
	}
	
	@Plugin(
			name = "Generate Playout Transition Tree (DPN)",
			parameterLabels = {"PetriNetWithData (DPN)"},
			returnLabels = {"Transition Tree"},
			returnTypes = {PNWDTransitionTree.class},
			help="Computes a playout transition tree for a given graph."
				 + version,
			categories={PluginCategory.Discovery,PluginCategory.ConformanceChecking},
			userAccessible = true
	)
	@UITopiaVariant(
			affiliation = authorAff,
			author = authora,
			email = authorEmail,
			pack = packageName
	)
	public PNWDTransitionTree generatePlayoutTree(final UIPluginContext context, 
			PetriNetWithData dpn) {
//		ask for a length
		ProMPropertiesPanel wizard = new ProMPropertiesPanel("Parameters"
				+ " Playout Tree");
		ProMTextField texter = wizard.addTextField("longest trace (k)", "5");
		InteractionResult result = context.showConfiguration(
				"Configuration for generating playout tree",
				wizard);
		int len = Integer.parseInt(texter.getText());
		return new PNWDTransitionTree(
				new PNWDPlayoutEngine(context)
				.generateTraces(dpn, 
					len
				)
		);
	}
	
	@Plugin(
			name = "Compute determinism of a DPN",
			parameterLabels = {"Petri net with data"},
			returnLabels = {"DeterminismMeasure"},
			returnTypes = {HTMLToString.class},
			help=" Computes the determinism of a Petri net with guards, which may " +
					"be a Petri net with data.\r\n" + 
					" Determinism computes the portion of transitions that are "
					+ "in the postset of\r\n" + 
					" places with at least two outgoing arcs and have a "
					+ "non-trivial guard \r\n" + 
					" associated with them."
					+ version
					+"<br>"
					+ "<b>Related Work:</b> "
					+ "Process mining with exogenous data, 2024, "
					+ "Adam Banham, PhD Thesis, QUT, Australia.",
			categories={PluginCategory.ConformanceChecking},
			userAccessible = true
	)
	@UITopiaVariant(
			affiliation = authorAff,
			author = authora,
			email = authorEmail,
			pack = packageName
	)
	public HTMLToString ComputeDeterminism(
			final UIPluginContext context,
			final PetriNetWithData dpn) {
		
		return new HTMLToString() {

			public String toHTMLString(boolean includeHTMLTags) {
				return "<p>"
						+ "<b>Determinism:</b> " 
						+ Determinism.ComputeDeterminism(dpn)
						+ "</p>"; 
			}
		};
	}
	
	@Plugin(
			name = "Compute guard-recall of a DPN",
			parameterLabels = {"Petri net with data","Event log"},
			returnLabels = {"GuardRecallMeasure"},
			returnTypes = {HTMLToString.class},
			help=" Computes the guard-recall of a Petri net with guards, which may " +
					"be a Petri net with data.\r\n" + 
					" Guard-recall computes the portion of guards that were "
					+ "true when the corsponding playout tree is traversed for"
					+ "traces in the log (more one traversal may be considered "
					+ "for a single trace)."
					+ version
					+"<br>"
					+ "<b>Related Work:</b> "
					+ "Comparing Conformance Checking for Decision "
					+ "Mining: An Axiomatic Approach IEEE Access 2024, "
					+ "Adam Banham, Arthur H. M. ter Hofstede, Sander J.J. "
					+ "Leemans, Felix Mannhardt, Robert Andrews, Moe T. Wynn",
			categories={PluginCategory.ConformanceChecking},
			userAccessible = true
	)
	@UITopiaVariant(
			affiliation = authorAff,
			author = authora,
			email = authorEmail,
			pack = packageName
	)
	public HTMLToString ComputeGuardRecall(
			final UIPluginContext context,
			final PetriNetWithData dpn,
			final XLog log) {
		
		double grec = BookkeepingMeasures.computeGuardRecall(log, dpn, context);
		
		return new HTMLToString() {

			public String toHTMLString(boolean includeHTMLTags) {
				return "<p>"
						+ "<b>Guard-recall:</b> " 
						+ grec
						+ "</p>"; 
			}
		};
	}
	
	@Plugin(
			name = "Compute guard-precision of a DPN",
			parameterLabels = {"Petri net with data","Event log"},
			returnLabels = {"GuardPrecisionMeasure"},
			returnTypes = {HTMLToString.class},
			help=" Computes the guard-recall of a Petri net with guards, which may " +
					"be a Petri net with data.\r\n" + 
					" Guard-precision computes the portion of guards that were "
					+ "only true when for the travesed path in the corsponding "
					+ "playout tree for traces in the log (more one traversal "
					+ "may be considered for a single trace)."
					+ version
					+"<br>"
					+ "<b>Related Work:</b> "
					+ "Comparing Conformance Checking for Decision "
					+ "Mining: An Axiomatic Approach IEEE Access 2024, "
					+ "Adam Banham, Arthur H. M. ter Hofstede, Sander J.J. "
					+ "Leemans, Felix Mannhardt, Robert Andrews, Moe T. Wynn",
			categories={PluginCategory.ConformanceChecking},
			userAccessible = true
	)
	@UITopiaVariant(
			affiliation = authorAff,
			author = authora,
			email = authorEmail,
			pack = packageName
	)
	public HTMLToString ComputeGuardPrecision(
			final UIPluginContext context,
			final PetriNetWithData dpn,
			final XLog log) {
		
		
		double gprec = BookkeepingMeasures.computeGuardPrecision(
				log, dpn, 
				context
		);
		
		return new HTMLToString() {

			public String toHTMLString(boolean includeHTMLTags) {
				return "<p>"
						+ "<b>Guard-recall:</b> " 
						+ gprec
						+ "</p>"; 
			}
		};
	}

}
