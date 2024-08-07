package org.processmining.qut.exogenousdata.data.in;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.deckfour.xes.classification.XEventAttributeClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.extension.XExtensionManager;
import org.deckfour.xes.id.XID;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XAttributable;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeCollection;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.buffered.XTraceBufferedImpl;
import org.deckfour.xes.util.XTokenHelper;
import org.processmining.qut.exogenousdata.data.ExogenousAnnotatedLog;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ExogenousAnnotatedParser {
	
	public static ExogenousAnnotatedLog parse (File file) throws Exception {
		
		List<XLog> content = new XesExogenousAnnotatedParser().parse(new FileInputStream(file));
		
		return ExogenousAnnotatedLog.builder()
				.endogenousLog(content.get(0))
				.exoSubseries(content.get(1))
				.attributes(content.get(0).getAttributes())
				.parsed(true).build();
	}

	public static class XesExogenousAnnotatedParser extends XesXmlParser {
		
		@Override
		public List<XLog> parse(InputStream is) throws Exception {
			BufferedInputStream bis = new BufferedInputStream(is);
			// set up a specialized SAX2 handler to fill the container
			ExogenousAnnotatedXesXmlHandler handler = new ExogenousAnnotatedXesXmlHandler();
			// set up SAX parser and parse provided log file into the container
			SAXParserFactory parserFactory = SAXParserFactory.newInstance();
			parserFactory.setNamespaceAware(false);
			SAXParser parser = parserFactory.newSAXParser();
			parser.parse(bis, handler);
			bis.close();
			ArrayList<XLog> wrapper = new ArrayList<XLog>();
			wrapper.add(handler.getLog());
			wrapper.add(handler.getSubseries());
			return wrapper;
		}

		
		public class ExogenousAnnotatedXesXmlHandler extends DefaultHandler {
			/*
			 * A lot of pirate code from org.deckfour.xes.in.XesXmlParser.XesXmlHandler
			 */
			
			/**
			 * Buffer log.
			 */
			protected XLog log;
			/**
			 * Buffer trace.
			 */
			protected XTrace trace;
			/**
			 * Buffer event.
			 */
			protected XEvent event;
			/**
			 * Buffer for attributes.
			 */
			protected Stack<XAttribute> attributeStack;
			/**
			 * Buffer for attributables.
			 */
			protected Stack<XAttributable> attributableStack;
			/**
			 * Buffer for extensions.
			 */
			protected HashSet<XExtension> extensions;
			/**
			 * Buffer for globals.
			 */
			protected List<XAttribute> globals;
			
			/**
			 * Retrieves the parsed log.
			 * 
			 * @return The parsed log.
			 */
			public XLog getLog() {
				return log;
			}
			
			protected XLog subseries;
			
			protected XLog curr;
			
			public ExogenousAnnotatedXesXmlHandler() {
				log = null;
				trace = null;
				event = null;
				attributeStack = new Stack<XAttribute>();
				attributableStack = new Stack<XAttributable>();
				extensions = new HashSet<XExtension>();
				globals = null;
				subseries = null;
				curr = null;
			}
			
			public XLog getSubseries() {
				return subseries;
			}
			
			@Override
			public void startElement(String uri, String localName, String qName,
					Attributes attributes) throws SAXException {
				// resolve tag name
				String tagName = localName.trim();
				if (tagName.length() == 0) {
					tagName = qName;
				}
				// parse content
				if (tagName.equalsIgnoreCase("string")
						|| tagName.equalsIgnoreCase("date")
						|| tagName.equalsIgnoreCase("int")
						|| tagName.equalsIgnoreCase("float")
						|| tagName.equalsIgnoreCase("boolean")
						|| tagName.equalsIgnoreCase("id")
						|| tagName.equalsIgnoreCase("list")
						|| tagName.equalsIgnoreCase("container")) {
					// attribute tag.
					String key = attributes.getValue("key");
					if (key == null) {
						// Should not be
						key = "";
					}
					String value = attributes.getValue("value");
					if (value == null) {
						// Should not be. 
						value = "";
					}
					// derive extension, if attribute key hints that
					XExtension extension = null;
					if (key != null) {
						int colonIndex = key.indexOf(':');
						if (colonIndex > 0 && colonIndex < (key.length() - 1)) {
							String prefix = key.substring(0, colonIndex);
							extension = XExtensionManager.instance().getByPrefix(
									prefix);
						}
					}
					// create attribute of correct type
					XAttribute attribute = null;
					if (tagName.equalsIgnoreCase("string")) {
						attribute = factory.createAttributeLiteral(key, value,
								extension);
					} else if (tagName.equalsIgnoreCase("date")) {
						Date date = xsDateTimeConversion.parseXsDateTime(value);
						if (date != null) {
							attribute = factory.createAttributeTimestamp(key, date,
									extension);
						} else {
							return;
						}
					} else if (tagName.equalsIgnoreCase("int")) {
						attribute = factory.createAttributeDiscrete(key,
								Long.parseLong(value), extension);
					} else if (tagName.equalsIgnoreCase("float")) {
						attribute = factory.createAttributeContinuous(key,
								Double.parseDouble(value), extension);
					} else if (tagName.equalsIgnoreCase("boolean")) {
						attribute = factory.createAttributeBoolean(key,
								Boolean.parseBoolean(value), extension);
					} else if (tagName.equalsIgnoreCase("id")) {
						attribute = factory.createAttributeID(key,
								XID.parse(value), extension);
					} else if (tagName.equalsIgnoreCase("list")) {
						attribute = factory.createAttributeList(key, extension);
					} else if (tagName.equalsIgnoreCase("container")) {
						attribute = factory.createAttributeContainer(key, extension);
					}
					if (attribute != null) {
						// add to current attributable and push to stack
						attributeStack.push(attribute);
						attributableStack.push(attribute);
					}
				} else if (tagName.equalsIgnoreCase("event")) {
					// event element
					event = factory.createEvent();
					attributableStack.push(event);
				} else if (tagName.equalsIgnoreCase("trace")) {
					// trace element
					trace = factory.createTrace();
					attributableStack.push(trace);
//				EAXES extensions : 2 lines of code
				} else if (tagName.equalsIgnoreCase("exogenousUniverse")) {
					System.out.println("[ExogenousAnnotatedLogParser] Found exogenous universe");
					subseries = factory.createLog();
					attributableStack.push(subseries);
					log = curr;
					curr = subseries;
//				HOPE ITS ENOUGH..
				} else if (tagName.equalsIgnoreCase("log")) {
					// log element
					log = factory.createLog();
					attributableStack.push(log);
					curr = log;
				} else if (tagName.equalsIgnoreCase("extension")) {
					// extension element
					XExtension extension = null;
					String uriString = attributes.getValue("uri");
					if (uriString != null) {
						extension = XExtensionManager.instance().getByUri(
								URI.create(uriString));
					} else {
						String prefixString = attributes.getValue("prefix");
						if (prefixString != null) {
							extension = XExtensionManager.instance().getByPrefix(
									prefixString);
						}
					}
					if (extension != null) {
						curr.getExtensions().add(extension);
					} else {
						System.err.println("Unknown extension: " + uriString);
					}
				} else if (tagName.equalsIgnoreCase("global")) {
					// global element
					String scope = attributes.getValue("scope");
					if (scope.equalsIgnoreCase("trace")) {
						this.globals = curr.getGlobalTraceAttributes();
					} else if (scope.equalsIgnoreCase("event")) {
						this.globals = curr.getGlobalEventAttributes();
					}
				} else if (tagName.equalsIgnoreCase("classifier")) {
					// classifier element
					String name = attributes.getValue("name");
					String keys = attributes.getValue("keys");
					if (name != null && keys != null && name.length() > 0
							&& keys.length() > 0) {
						List<String> keysList = fixKeys(curr,
								XTokenHelper.extractTokens(keys));
						String[] keysArray = new String[keysList.size()];
						int i = 0;
						for (String key : keysList) {
							keysArray[i++] = key;
						}
						XEventClassifier classifier = new XEventAttributeClassifier(
								name, keysArray);
						curr.getClassifiers().add(classifier);
					}
				}
			}
			
			@Override
			public void endElement(String uri, String localName, String qName)
					throws SAXException {
				// resolve tag name
				String tagName = localName.trim();
				if (tagName.length() == 0) {
					tagName = qName;
				}
				// parse content
				if (tagName.equalsIgnoreCase("global")) {
					// close globals
					this.globals = null;
				} else if (tagName.equalsIgnoreCase("string")
						|| tagName.equalsIgnoreCase("date")
						|| tagName.equalsIgnoreCase("int")
						|| tagName.equalsIgnoreCase("float")
						|| tagName.equalsIgnoreCase("boolean")
						|| tagName.equalsIgnoreCase("id")
						|| tagName.equalsIgnoreCase("list")
						|| tagName.equalsIgnoreCase("container")) {
					XAttribute attribute = attributeStack.pop();
					attributableStack.pop(); // remove self from top
					if (globals != null) {
						globals.add(attribute);
					} else {
						attributableStack.peek().getAttributes()
								.put(attribute.getKey(), attribute);
						if (!attributeStack.isEmpty() && attributeStack.peek() instanceof XAttributeCollection) {
							// Has parent attribute which is a collection. Add the key to the collection.
							((XAttributeCollection) attributeStack.peek())
									.addToCollection(attribute);
						}
					}
				} else if (tagName.equalsIgnoreCase("event")) {
					trace.add(event);
					event = null;
					attributableStack.pop(); // remove self from top
				} else if (tagName.equalsIgnoreCase("trace")) {
					if (trace instanceof XTraceBufferedImpl) {
						((XTraceBufferedImpl) trace).consolidate();
					}
					curr.add(trace);
					trace = null;
					attributableStack.pop(); // remove self from top
				} else if (tagName.equalsIgnoreCase("exogenousUniverse")) {
					attributableStack.pop(); // remove self from top
					subseries = curr;
					curr = log;
				} else if (tagName.equalsIgnoreCase("log")) {
					// add all extensions
					for (XExtension ext : extensions) {
						curr.getExtensions().add(ext);
					}
					attributableStack.pop(); // remove self from top
				}
			}
			
			public List<String> fixKeys(XLog log, List<String> keys) {
					/*
					 * Try to fix the keys using the global event attributes.
					 */
					List<String> fixedKeys = fixKeys(log, keys, 0);
					return fixedKeys == null ? keys : fixedKeys;
			}
			
			public List<String> fixKeys(XLog log, List<String> keys, int index) {
				if (index >= keys.size()) {
					/*
					 * keys[0,...,length-1] are matched to global event attributes.
					 */
					return keys;
				} else {
					/*
					 * keys[0,...,index-1] are matched to global event attributes. Try
					 * to match keys[index].
					 */
					if (findGlobalEventAttribute(log, keys.get(index))) {
						/*
						 * keys[index] matches a global event attribute. Try if
						 * keys[index+1,..,length-1] match with global event attributes.
						 */
						List<String> fixedKeys = fixKeys(log, keys, index + 1);
						if (fixedKeys != null) {
							/*
							 * Yes they do. Return the match.
							 */
							return fixedKeys;
						}
						/*
						 * No, they do not. Fall thru to match
						 * keys[index]+" "+keys[index+1] to a global event attribute,
						 */
					}
					/*
					 * No such global event attribute, or no match when key[index] is
					 * matched to a global event attribute. Try merging key[index] with
					 * key[index+1].
					 */
					if (index + 1 == keys.size()) {
						/*
						 * No keys[index+1]. We cannot match keys[length-1]. Fail.
						 */
						return null;
					}
					/*
					 * Copy all matched keys.
					 */
					List<String> newKeys = new ArrayList<String>(keys.size() - 1);
					for (int i = 0; i < index; i++) {
						newKeys.add(keys.get(i));
					}
					/*
					 * Merge keys[index] with keys[index+1].
					 */
					newKeys.add(keys.get(index) + " " + keys.get(index + 1));
					/*
					 * Copy all keys still left to match.
					 */
					for (int i = index + 2; i < keys.size(); i++) {
						newKeys.add(keys.get(i));
					}
					/*
					 * Check match with merged key.
					 */
					return fixKeys(log, newKeys, index);
				}
			}
	
			public boolean findGlobalEventAttribute(XLog log, String key) {
				for (XAttribute attribute : log.getGlobalEventAttributes()) {
					if (attribute.getKey().equals(key)) {
						return true;
					}
				}
				/*
				 * Did not find attribute with given key.
				 */
				return false;
			}
	
		}
	}

}
