package org.processmining.qut.exogenousdata.utils;

import java.util.Collection;
import java.util.concurrent.Executor;

import org.processmining.framework.connections.Connection;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.connections.ConnectionID;
import org.processmining.framework.connections.ConnectionManager;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.PluginContextID;
import org.processmining.framework.plugin.PluginDescriptor;
import org.processmining.framework.plugin.PluginExecutionResult;
import org.processmining.framework.plugin.PluginManager;
import org.processmining.framework.plugin.PluginParameterBinding;
import org.processmining.framework.plugin.ProMFuture;
import org.processmining.framework.plugin.Progress;
import org.processmining.framework.plugin.RecursiveCallException;
import org.processmining.framework.plugin.events.Logger.MessageLevel;
import org.processmining.framework.plugin.events.ProgressEventListener.ListenerList;
import org.processmining.framework.plugin.impl.FieldSetException;
import org.processmining.framework.providedobjects.ProvidedObjectManager;
import org.processmining.framework.util.Pair;

public class ContextUtils {

	private ContextUtils() {};
	
	public static class FakeContext implements PluginContext {

		public PluginManager getPluginManager() {
			// TODO Auto-generated method stub
			return null;
		}

		public ProvidedObjectManager getProvidedObjectManager() {
			// TODO Auto-generated method stub
			return null;
		}

		public ConnectionManager getConnectionManager() {
			// TODO Auto-generated method stub
			return new ConnectionManager() {
				
				public void setEnabled(boolean isEnabled) {
					// TODO Auto-generated method stub
					
				}
				
				public boolean isEnabled() {
					// TODO Auto-generated method stub
					return false;
				}
				
				public <T extends Connection> T getFirstConnection(Class<T> connectionType, PluginContext context,
						Object... objects) throws ConnectionCannotBeObtained {
					// TODO Auto-generated method stub
					return null;
				}
				
				public <T extends Connection> Collection<T> getConnections(Class<T> connectionType, PluginContext context,
						Object... objects) throws ConnectionCannotBeObtained {
					// TODO Auto-generated method stub
					return null;
				}
				
				public org.processmining.framework.plugin.events.ConnectionObjectListener.ListenerList getConnectionListeners() {
					// TODO Auto-generated method stub
					return null;
				}
				
				public Collection<ConnectionID> getConnectionIDs() {
					// TODO Auto-generated method stub
					return null;
				}
				
				public Connection getConnection(ConnectionID id) throws ConnectionCannotBeObtained {
					// TODO Auto-generated method stub
					return null;
				}
				
				public void clear() {
					// TODO Auto-generated method stub
					
				}
				
				public <T extends Connection> T addConnection(T connection) {
					// TODO Auto-generated method stub
					return null;
				}
			};
		}

		public PluginContextID createNewPluginContextID() {
			// TODO Auto-generated method stub
			return null;
		}

		public void invokePlugin(PluginDescriptor plugin, int index, Object... objects) {
			// TODO Auto-generated method stub
			
		}

		public void invokeBinding(PluginParameterBinding binding, Object... objects) {
			// TODO Auto-generated method stub
			
		}

		public Class<? extends PluginContext> getPluginContextType() {
			// TODO Auto-generated method stub
			return null;
		}

		public <T, C extends Connection> Collection<T> tryToFindOrConstructAllObjects(Class<T> type,
				Class<C> connectionType, String role, Object... input) throws ConnectionCannotBeObtained {
			// TODO Auto-generated method stub
			return null;
		}

		public <T, C extends Connection> T tryToFindOrConstructFirstObject(Class<T> type, Class<C> connectionType,
				String role, Object... input) throws ConnectionCannotBeObtained {
			// TODO Auto-generated method stub
			return null;
		}

		public <T, C extends Connection> T tryToFindOrConstructFirstNamedObject(Class<T> type, String name,
				Class<C> connectionType, String role, Object... input) throws ConnectionCannotBeObtained {
			// TODO Auto-generated method stub
			return null;
		}

		public PluginContext createChildContext(String label) {
			// TODO Auto-generated method stub
			return null;
		}

		public Progress getProgress() {
			// TODO Auto-generated method stub
			return null;
		}

		public ListenerList getProgressEventListeners() {
			// TODO Auto-generated method stub
			return null;
		}

		public org.processmining.framework.plugin.events.PluginLifeCycleEventListener.List getPluginLifeCycleEventListeners() {
			// TODO Auto-generated method stub
			return null;
		}

		public PluginContextID getID() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getLabel() {
			// TODO Auto-generated method stub
			return null;
		}

		public Pair<PluginDescriptor, Integer> getPluginDescriptor() {
			// TODO Auto-generated method stub
			return null;
		}

		public PluginContext getParentContext() {
			// TODO Auto-generated method stub
			return null;
		}

		public java.util.List<PluginContext> getChildContexts() {
			// TODO Auto-generated method stub
			return null;
		}

		public PluginExecutionResult getResult() {
			// TODO Auto-generated method stub
			return null;
		}

		public ProMFuture<?> getFutureResult(int i) {
			// TODO Auto-generated method stub
			return null;
		}

		public Executor getExecutor() {
			// TODO Auto-generated method stub
			return null;
		}

		public boolean isDistantChildOf(PluginContext context) {
			// TODO Auto-generated method stub
			return false;
		}

		public void setFuture(PluginExecutionResult resultToBe) {
			// TODO Auto-generated method stub
			
		}

		public void setPluginDescriptor(PluginDescriptor descriptor, int methodIndex)
				throws FieldSetException, RecursiveCallException {
			// TODO Auto-generated method stub
			
		}

		public boolean hasPluginDescriptorInPath(PluginDescriptor descriptor, int methodIndex) {
			// TODO Auto-generated method stub
			return false;
		}

		public void log(String message, MessageLevel level) {
			// TODO Auto-generated method stub
			
		}

		public void log(String message) {
			// TODO Auto-generated method stub
			
		}

		public void log(Throwable exception) {
			// TODO Auto-generated method stub
			
		}

		public org.processmining.framework.plugin.events.Logger.ListenerList getLoggingListeners() {
			// TODO Auto-generated method stub
			return null;
		}

		public PluginContext getRootContext() {
			// TODO Auto-generated method stub
			return null;
		}

		public boolean deleteChild(PluginContext child) {
			// TODO Auto-generated method stub
			return false;
		}

		public <T extends Connection> T addConnection(T c) {
			// TODO Auto-generated method stub
			return null;
		}

		public void clear() {
			// TODO Auto-generated method stub
			
		}
		
	}
}
