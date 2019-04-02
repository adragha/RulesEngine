/* 
 * MIT License
 * 
 * Copyright (c) 2019 adragha
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package adragha.rules.engine;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to load knowledge bases and create, save, restore 
 * and remove rule engine sessions.
 *  
 * @author adragha
 *
 */
public final class SessionManager {
	/** Logger for session manager level logging */
	static final Logger logger = LoggerFactory.getLogger(SessionManager.class);
	/** Singleton instance */
	private static SessionManager thisInstance = new SessionManager(); 
	/** System properties */
	private Properties properties = null;
	/** Map of knowledge bases loaded */
	private Map<String, KnowledgeBase> knowledgeBases = null;
	/** List of in-progress session */
	private List<Session> sessions = null;
	/** Singleton instance initialization */
	private boolean initialized = false;
	
	/**
	 * Private constructor for singleton instance
	 */
	private SessionManager () {
		properties = new Properties();		
		knowledgeBases = new HashMap<String, KnowledgeBase>();
		sessions = new ArrayList<Session>();
	}
	
	/**
	 * Thread-safe method to get handle on singleton session manager initialized with default properties 
	 * @return SessionManager object
	 * @throws IOException
	 */
	public static SessionManager getManager() throws IOException {
		return getManager("engine.properties");
	}
	
	/**
	 * Thread-safe method to get handle on singleton session manager initialized with custom 
	 * properties. Previously initialized manager will ignore properties file name argument.
	 * @param propertiesFileName String path and name for custom properties file
	 * @return  SessionManager object
	 * @throws IOException
	 */
	public static SessionManager getManager(String propertiesFileName) throws IOException {
		synchronized(thisInstance) {
			if (!thisInstance.initialized) {
				// Load properties file if not initialized
				FileInputStream in = new FileInputStream(propertiesFileName);
				thisInstance.properties.load(in);
				in.close();

				thisInstance.initialized = true;				
			}
			
			// Return singleton
			return thisInstance;
		}
	}
	
	/**
	 * Thread-safe method to create a new session for the knowledge base specified.
	 * Creates and load the knowledge base if needed.
	 * @param knowledgeBaseName String name of knowledge base
	 * @return Session object created
	 * @throws EngineException
	 */
	public synchronized Session createSession(String knowledgeBaseName) throws EngineException {		
		// If needed, load knowledge base and store in memory
		if (!knowledgeBases.containsKey(knowledgeBaseName)) {
			knowledgeBases.put(knowledgeBaseName, new KnowledgeBase(knowledgeBaseName, properties));
			logger.info("Loaded knowledge base {}", knowledgeBaseName);
		}

		// Create session and add it to in-progression sessions list
		Session newSession = new Session(knowledgeBases.get(knowledgeBaseName));		
		sessions.add(newSession);
		
		return newSession;
	}
	
	/**
	 * Thread-safe method to remove/delete existing session  
	 * @param session Session object to remove
	 */
	public void removeSession(Session session) {
		// Pre-procession session for deletion
		session.prepareForDeletion();
		
		// Remove in thread-safe manner
		synchronized(this) {
			sessions.remove(session);
		}		
	}
	
	/**
	 * Thread-safe method to restore session from JSON file saved with session inputs.
	 * Does not trigger immediate validation of restored session inputs. Invoking code 
	 * is expected to call session validation.
	 * @param filename String name of JSON file to restore session from
	 * @return Session object restored
	 * @throws IOException
	 * @throws EngineException
	 */
	public Session restoreSession(String filename) throws IOException, EngineException {
		SavedSession savedSession = (SavedSession) Util.getObjectFromJson(FileUtils.readFileToString(new File(filename), Constants.UTF_8), SavedSession.class);
				
		Session restoredSession = createSession(savedSession.getknowledgeBaseName());
		
		synchronized(this) {
			sessions.add(restoredSession);
		}
		
		for(InputSelection restoredSelection : savedSession.getSelections()) {
			restoredSession.createAndQueueSelection(restoredSelection);
		}
		
		return restoredSession;
	}
	
	/**
	 * Thread-safe method to save session to a JSON file with session inputs.
	 * @param filename String name of saved file
	 * @param session Session object to save
	 * @throws IOException
	 */
	public void saveSession(String filename, Session session) throws IOException {
		Util.writeObjectToJson(filename, new SavedSession(session.getKnowledgeBase().getName(), session.getInputSelections()));
	}	

	/**
	 * Method to get system property value 
	 * @param key String name of property 
	 * @return String value of property
	 */
	public String getProperty(String key) {
		return properties.getProperty(key);
	}
	
	/**
	 * Helper class to create saved session
	 */
	private class SavedSession {
		/** Name of session knowledge base */
		private String knowledgeBaseName;
		
		/** List of session input selections */
		private List<InputSelection> selections = new ArrayList<InputSelection>();
		
		/**
		 * Constructor for save session class
		 * @param knowledgeBaseName String name of session knowledge base
		 * @param selectionList List of session input selections
		 */
		SavedSession(String knowledgeBaseName, List<InputSelection> selectionList) {
			this.knowledgeBaseName = knowledgeBaseName;
			
			if (selectionList != null) {
				this.selections = selectionList;
			}
			
			this.selections = Collections.unmodifiableList(this.selections);
		}

		/**
		 * Method to get saved session knowledge base name
		 * @return String name of knowledge base
		 */
		String getknowledgeBaseName() {
			return knowledgeBaseName;
		}
		
		/**
		 * Method to get list of input selections for saved session 
		 * @return List of session input selections
		 */
		List<InputSelection> getSelections() {
			return selections;
		}
	}
}
