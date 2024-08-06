package org.processmining.qut.exogenousdata;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PetriNetWithData;
import org.processmining.qut.exogenousdata.conformance.playout.PNWDPlayoutEngine;
import org.processmining.qut.exogenousdata.conformance.transitiontree.PNWDTransitionTree;

/**
 * Plugins related to data-aware conformance as presented in:<br>
 * <b>Comparing Conformance Checking for Decision Mining: 
 * An Axiomatic Approach</b>
 * <br>
 * Plugins included are:<br>
 * 	Generate Playout Transition Tree<br>
 * 	Compute guard-recall<br>
 *  Compute guard-precision<br>
 *  
 * @author adam banham
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

}
