package org.processmining.qut.exogenousdata.data;

import java.util.List;
import java.util.NoSuchElementException;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.processmining.log.utils.XUtils;
import org.processmining.qut.exogenousdata.exceptions.ExogenousAttributeNotFoundException;
import org.processmining.qut.exogenousdata.steps.linking.AttributeLinker;
import org.processmining.qut.exogenousdata.steps.linking.ChainAttributeLinker;
import org.processmining.qut.exogenousdata.steps.linking.GrabFristLinker;
import org.processmining.qut.exogenousdata.steps.linking.Linker;

/**
 * Static class for helper functions around handling creating and using exogenous data sets.
 * @author Adam P Banham
 *
 */
public class ExogenousUtils {

	private ExogenousUtils() {};
	
	/**
	 * Extracts the timestamp of the given event in miliseconds from epoch.
	 * @param ev
	 * @return time in miliseconds.
	 * @throws NoSuchElementException
	 */
	public static long getEventTimeMillis(XEvent ev) throws NoSuchElementException {
		if (ev.getAttributes().containsKey("time:timestamp")) {
			return XUtils.getTimestamp(ev).getTime();
		} else {
			throw new NoSuchElementException("Unable to find suitable attribute for event to describe time.");
		}
	};
	
	/**
	 * Finds an exogenous data type for a given exogenous log.
	 * @param elog to be checked
	 * @return the data type of measurements within log
	 */
	public static ExogenousDatasetType findDataType(XLog elog) throws ExogenousAttributeNotFoundException {
		if (ExogenousLogAttributes.EXOGENOUS_DATA_TYPE_NUMERICAL.check(elog)) {
			return ExogenousDatasetType.NUMERICAL;
		} else if (ExogenousLogAttributes.EXOGENOUS_DATA_TYPE_DISCRETE.check(elog)) {
			return ExogenousDatasetType.DISCRETE;
		} else {
			throw new ExogenousAttributeNotFoundException(ExogenousDatasetType.class);
		}
	}
	
	/**
	 * Finds the link type of a given exogenous log.
	 * @param elog to be checked
	 * @return the link type for constructing a Linker
	 * @throws ExogenousAttributeNotFoundException 
	 */
	public static ExogenousDatasetLinkType findLinkType(XLog elog) throws ExogenousAttributeNotFoundException {
		if (ExogenousLogAttributes.EXOGENOUS_LINK_TYPE_TRACEATTRS.check(elog)) {
			return ExogenousDatasetLinkType.TRACE_ATTRIBUTE_MATCH;
		} else if (ExogenousLogAttributes.EXOGENOUS_LINK_TYPE_EVENTATTRS.check(elog)) {
			return ExogenousDatasetLinkType.EVENT_ATTRIBUTE_MATCH;
		} else if (ExogenousLogAttributes.EXOGENOUS_LINK_TYPE_ANY.check(elog)) {
			return ExogenousDatasetLinkType.ANY_MATCH;
		} else {
			throw new ExogenousAttributeNotFoundException(ExogenousDatasetLinkType.class);
		}
	}
	
	/**
	 * Constructs and returns a Linker for a given exogenous log and exogenous dataset.
	 * @param elog the source exogenous log
	 * @param edata the linked wrapper with linkType (if not set will attempt to)
	 * @return the linker for the exogenous data set for linkage in xPM
	 */
	public static Linker constructLinker(XLog elog, ExogenousDataset edata) {
		if (edata.linkType.equals(ExogenousDatasetLinkType.TRACE_ATTRIBUTE_MATCH)) {
			List<String> attributes = ExogenousLogAttributes.extractLinkAttributes(elog);
			if (attributes.size() == 1) {
				return AttributeLinker.builder().attributeName(attributes.get(0)).build();
			} else if (attributes.size() > 1) {
				return ChainAttributeLinker.builder().attributes(attributes).build().setup();
			} else {
//				TODO handle the case where no attributes are found
				return AttributeLinker.builder().attributeName("foo").build();
			}
		} else if (edata.linkType.equals(ExogenousDatasetLinkType.EVENT_ATTRIBUTE_MATCH)) {
//			TODO need to make a event based linker
			return AttributeLinker.builder().attributeName("foo").build();
		} else if (edata.linkType.equals(ExogenousDatasetLinkType.ANY_MATCH)) {
			return GrabFristLinker.builder().build();
		} else {
	//		return a dummy attribute linker in the otherwise case
			return AttributeLinker.builder().attributeName("foo").build();
		}
	}
	
	/**
	 * Checks whether an event log can be converted to an exogenous data set.
	 * @param elog to be checked
	 * @return can be converted as is
	 */
	public static Boolean isExogenousLog(XLog elog) {
		return ExogenousLogAttributes.EXOGENOUS_DATASET.check(elog);
	}
}
