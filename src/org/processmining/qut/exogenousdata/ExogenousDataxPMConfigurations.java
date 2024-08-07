package org.processmining.qut.exogenousdata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.qut.exogenousdata.data.ExogenousAnnotatedLog;
import org.processmining.qut.exogenousdata.data.ExogenousDataset;
import org.processmining.qut.exogenousdata.exceptions.CannotConvertException;
import org.processmining.qut.exogenousdata.steps.determination.configs.AIIM2022;
import org.processmining.qut.exogenousdata.steps.determination.configs.ExemplarConfig;

/**
 * Contains plug-ins for the known xPM configurations.
 * @author n7176546
 *
 */
public class ExogenousDataxPMConfigurations {
	
	private static final String packageName = ExogenousDataStatics.packageName;
	private static final String version = ExogenousDataStatics.version;
	private static final String authora = ExogenousDataStatics.authora;
	private static final String authorEmail = ExogenousDataStatics.authoraEmail;
	private static final String authorAff = ExogenousDataStatics.authoraAff;

	@Plugin(
			name = "Exogenous Annotated Log Preparation (AIIM 2022) (XLogs)",
			parameterLabels = {"Event Log", "Exo-Panels"},
			returnLabels = {"Exogenous Annotated Log"},
			returnTypes = {ExogenousAnnotatedLog.class},
			help="Given an event log and several exo-panels, this plugin allows"
				 + " users to reproduce the xPM instantition used in :"
				 + " xPM: Enhancing Exogenous Data Visibility. Adam "
				 + "Banham et. al. Artificial Intelligence in Medicine 2022"
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
	public ExogenousAnnotatedLog AIIM2022preperationWithXLogs(final UIPluginContext context, 
			final XLog endogenous, final XLog[] exogenous) throws Throwable {
		final List<ExogenousDataset> exoLogs = new ArrayList<ExogenousDataset>();
		for(final XLog elog: exogenous) {
			ExogenousDataset temp;
			try {
				temp = ExogenousDataset.builder()
						.source(elog)
						.build()
						.setup();
			} catch (final CannotConvertException e) {
				// if log cannot naively be convert to dataset then move on
				System.out.println("[ExogenousAnnotatedLog] Cannot convert log="
						+ "'"
						+ elog.getAttributes().get("concept:name").toString()
						+"' to an exogenous dataset.");
				continue;
			}
			exoLogs.add(temp);
		}
		final ExogenousAnnotatedLog annotated = ExogenousAnnotatedLog
				.builder()
				.endogenousLog(endogenous)
				.exogenousDatasets(exoLogs)
				.classifiers(endogenous.getClassifiers())
				.extensions(endogenous.getExtensions())
				.useDefaultConfiguration(true)
				.determinations(AIIM2022.getConfiguration(exoLogs))
				.globalEventAttributes(endogenous.getGlobalEventAttributes())
				.globalTraceAttributes(endogenous.getGlobalTraceAttributes())
				.attributes(endogenous.getAttributes())
				.parsed(false)
				.build()
				.setup(context);
		return annotated;
		
	}
	
	@Plugin(
			name = "Exogenous Annotated Log Preparation (AIIM 2022)",
			parameterLabels = {"Event Log", "Exo-Panels"},
			returnLabels = {"Exogenous Annotated Log"},
			returnTypes = {ExogenousAnnotatedLog.class},
			help="Given an event log and several exo-panels, this plugin allows"
				 + " users to reproduce the xPM instantition used in :"
				 + " xPM: Enhancing Exogenous Data Visibility. Adam "
				 + "Banham et. al. Artificial Intelligence in Medicine 2022"
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
	public ExogenousAnnotatedLog AIIM2022preperation(final UIPluginContext context, 
			final XLog endogenous, final ExogenousDataset[] exogenous) throws Throwable {
		final List<ExogenousDataset> exoLogs = Arrays.asList(exogenous);
		
		final ExogenousAnnotatedLog annotated = ExogenousAnnotatedLog
				.builder()
				.endogenousLog(endogenous)
				.exogenousDatasets(exoLogs)
				.classifiers(endogenous.getClassifiers())
				.extensions(endogenous.getExtensions())
				.useDefaultConfiguration(true)
				.determinations(AIIM2022.getConfiguration(exoLogs))
				.globalEventAttributes(endogenous.getGlobalEventAttributes())
				.globalTraceAttributes(endogenous.getGlobalTraceAttributes())
				.attributes(endogenous.getAttributes())
				.parsed(false)
				.build()
				.setup(context);
		return annotated;
		
	}
	
	@Plugin(
			name = "Exogenous Annotated Log Preparation (Exemplar) (XLogs)",
			parameterLabels = {"Event Log", "Exo-Panels"},
			returnLabels = {"Exogenous Annotated Log"},
			returnTypes = {ExogenousAnnotatedLog.class},
			help="Given an event log and several exo-panels, this plugin allows"
				 + " users to reproduce the exemplar configuration."
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
	public ExogenousAnnotatedLog exemplarPreperationWithXLogs(final UIPluginContext context, 
			final XLog endogenous, final XLog[] exogenous) throws Throwable {
		final List<ExogenousDataset> exoLogs = new ArrayList<ExogenousDataset>();
		for(final XLog elog: exogenous) {
			ExogenousDataset temp;
			try {
				temp = ExogenousDataset.builder()
						.source(elog)
						.build()
						.setup();
			} catch (final CannotConvertException e) {
				// if log cannot naively be convert to dataset then move on
				System.out.println("[ExogenousAnnotatedLog] Cannot convert log="
						+ "'"
						+ elog.getAttributes().get("concept:name").toString()
						+"' to an exogenous dataset.");
				continue;
			}
			exoLogs.add(temp);
		}
		final ExogenousAnnotatedLog annotated = ExogenousAnnotatedLog
				.builder()
				.endogenousLog(endogenous)
				.exogenousDatasets(exoLogs)
				.classifiers(endogenous.getClassifiers())
				.extensions(endogenous.getExtensions())
				.useDefaultConfiguration(true)
				.determinations(ExemplarConfig.getConfiguration(exoLogs))
				.globalEventAttributes(endogenous.getGlobalEventAttributes())
				.globalTraceAttributes(endogenous.getGlobalTraceAttributes())
				.attributes(endogenous.getAttributes())
				.parsed(false)
				.build()
				.setup(context);
		return annotated;
		
	}
	
	@Plugin(
			name = "Exogenous Annotated Log Preparation (Exemplar)",
			parameterLabels = {"Event Log", "Exo-Panels"},
			returnLabels = {"Exogenous Annotated Log"},
			returnTypes = {ExogenousAnnotatedLog.class},
			help="Given an event log and several exo-panels, this plugin allows"
				 + " users to reproduce the exemplar configuration."
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
	public ExogenousAnnotatedLog exemplarPreperation(final UIPluginContext context, 
			final XLog endogenous, final ExogenousDataset[] exogenous) throws Throwable {
		final List<ExogenousDataset> exoLogs = Arrays.asList(exogenous);
		
		final ExogenousAnnotatedLog annotated = ExogenousAnnotatedLog
				.builder()
				.endogenousLog(endogenous)
				.exogenousDatasets(exoLogs)
				.classifiers(endogenous.getClassifiers())
				.extensions(endogenous.getExtensions())
				.useDefaultConfiguration(true)
				.determinations(ExemplarConfig.getConfiguration(exoLogs))
				.globalEventAttributes(endogenous.getGlobalEventAttributes())
				.globalTraceAttributes(endogenous.getGlobalTraceAttributes())
				.attributes(endogenous.getAttributes())
				.parsed(false)
				.build()
				.setup(context);
		return annotated;
		
	}
}
