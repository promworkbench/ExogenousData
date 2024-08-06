package org.processmining.qut.exogenousdata;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.util.HTMLToString;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PetriNetWithData;
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
			name = "Generate Playout Transition Tree (DPN)",
			parameterLabels = {"PetriNetWithData (DPN)"},
			returnLabels = {"Transition Tree"},
			returnTypes = {PNWDTransitionTree.class},
			help="Computes a playout transition tree for a given graph.\n"
				 + "version: "
				 + version,
			categories={PluginCategory.Discovery},
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
		return new PNWDTransitionTree(
				new PNWDPlayoutEngine().generateTraces(dpn, 4)
		);
	}
	
	@Plugin(
			name = "Compute determinism of a DPN",
			parameterLabels = {"Petri net with data"},
			returnLabels = {"DeterminismMeasure"},
			returnTypes = {HTMLToString.class},
			help=" Computes the determinism of a Petri with guards, which may " +
					"be a Petri net with data.\r\n" + 
					" Determinism computes the portion of transitions that are "
					+ "in the postset of\r\n" + 
					" places with at least two outgoing arcs and have a "
					+ "non-trivial guard \r\n" + 
					" associated with them."
					+ version,
			categories={PluginCategory.ConformanceChecking},
			userAccessible = true
	)
	@UITopiaVariant(
			affiliation = authorAff,
			author = authora,
			email = authorEmail,
			pack = "ExogenousData"
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

}
