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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class for session object. Session objects are based on their corresponding
 * knowledge base model object. Each unit quantity of an input selection results
 * in a session object, and new session objects are created as necessary for 
 * satisfying needers during validation. Child needers and providers also
 * create associated session objects when the parent session object is created. 
 * For a parent object to be successfully validated, all its needer children must
 * be satisfied by appropriate providers.
 *   
 * @author adragha
 *
 */
public final class SessionObject {
	/** Enum for status of a session object */
	static enum ValidationStatus {
		UNVALIDATED, 
		SUCCESSFUL, 
		FAILED
	}

	/** Auto-assigned unique session object ID */
	private String sessionObjectId = null;
	/** ModelObject based on which session object was created */
	private ModelObject modelObject = null;
	/** Reference to session object */
	private Session session = null;
	/** Reference to parent object. Non-null for needer and provider objects */
	private SessionObject parentObject = null;
	/** Counter used to assign unique ID to child objects */
	private int childIdCounter = 0;
	/** Map of single valued model properties specified on session object */
	private Map<String, Object> valueProperties = new HashMap<String, Object>();
	/** Map of list value model properties specified on session object */
	private Map<String, List<?>> listProperties = new HashMap<String, List<?>>();
	/** List of child needers on parent session object */
	private List<SessionObject> childNeeders = new ArrayList<SessionObject>(0);
	/** List of child providers on parent session object */
	private List<SessionObject> childProviders = new ArrayList<SessionObject>(0);
	
	/**
	 * Package constructor to create a session object
	 * @param session Session object
	 * @param modelObject ModelObject to be instantiated as this session object
	 * @param sessionObjectId Unique object ID for session object
	 * @param parentObject SessionObject parent if applicable, null otherwise
	 * @throws EngineException
	 */
	SessionObject(Session session, ModelObject modelObject, String sessionObjectId, SessionObject parentObject) throws EngineException {
		// Initialize fields
		this.session = session;
		this.modelObject = modelObject;
		this.sessionObjectId = sessionObjectId;
		this.parentObject = parentObject;

		session.addToSession(this);

		// For parent objects, create child objects
		if (parentObject == null) {
			childNeeders = createChildObjects(Constants.NEEDER_LIST);
			// Sort needers to be in validation order
			Collections.sort(childNeeders, session.getSessionObjectValidationOrderComparator());
			
			// Mark child lists unmodifiable
			childNeeders = Collections.unmodifiableList(childNeeders);			
			childProviders = Collections.unmodifiableList(createChildObjects(Constants.PROVIDER_LIST));
		}
	}
	
	/**
	 * Helper method to create child needer and provider session objects for a parent object
	 * @param listPropertyName String name of needer/provider list property
	 * @return List of SessionObject that are children based on property value
	 * @throws EngineException
	 */
	private List<SessionObject> createChildObjects(String listPropertyName) throws EngineException {
		List<SessionObject> children = new ArrayList<SessionObject>();
		
		// Get list of children from model data
		List<ModelObject> childObjects = session.getKnowledgeBase().getModelObjects(modelObject.getListPropertyValue(listPropertyName));
		
		// Create child session object for each child
		for(ModelObject child : childObjects) {
			String childId = getObjectId() + ":" + getNextChildId();
			children.add(new SessionObject(session, child, childId, this));
		}
		
		return children;
	}
	
	/**
	 * Helper method to get a child index that is unique amongst the parent's children 
	 * @return Integer index for child
	 */
	private String getNextChildId() {
		childIdCounter++;
		 
		return Integer.toString(childIdCounter);
	}

	/**
	 * Package method to get validation status of session object. Parent objects
	 * with child needers are deemed successful when all the needers are satisfied.
	 * Not relevant for child provider session objects.
	 * @return ValidationStatus enum value
	 */
	ValidationStatus getValidationStatus() {
		// Get current status of object
		ValidationStatus objectStatus = ValidationStatus.valueOf((String) getPropertyValue(Constants.VALIDATION_STATUS));
		
		// If child object, just return status
		if (getParentObject() != null) {
			return objectStatus;
		}
		
		// If parent object, return combined status of all needers
		if (getChildNeeders().size() > 0) {
			ValidationStatus combinedNeederStatus = ValidationStatus.SUCCESSFUL;
			for(SessionObject needer : getChildNeeders()) {
				if (needer.getValidationStatus() != ValidationStatus.SUCCESSFUL) {
					combinedNeederStatus = needer.getValidationStatus();
					break;
				}
			}
			objectStatus = combinedNeederStatus;
		}
		// If parent without needer, deemed successful by default
		else {
			objectStatus = ValidationStatus.SUCCESSFUL;
		}
		
		return objectStatus;
	}
	
	/**
	 * Method to set property value on session object during session validation
	 * @param propertyName String name of property
	 * @param value Object value of property
	 */
	void setPropertyValue(String propertyName, Object value) {
		// Proceed if valid property name
		if (propertyName != null && propertyName.length() > 0) {
			// Set if valid property and value
			if (value != null) {
				valueProperties.put(propertyName, value);
			}
			// Set to empty string if not a valid value
			else {
				valueProperties.put(propertyName, Constants.EMPTY);				
			}
		}
		// Log warning if invalid property name 
		else {
			Session.logger.warn("Ignoring setPropertyValue(..) invocation with invalid property name on object {}", getObjectId());
		}
	}

	/**
	 * Method to remove property value on session object
	 * @param propertyName String name of property
	 */
	void removePropertyValue(String propertyName) {
		// Remove property value if it exists
		if (valueProperties.containsKey(propertyName)) {
			valueProperties.remove(propertyName);
		}
	}
	
	/**
	 * Method to set list property value on session object during session validation
	 * @param propertyName String name of list property
	 * @param value List value of property
	 */
	void setListPropertyValue(String propertyName, List<?> value) {
		// Proceed if valid property name
		if (propertyName != null && propertyName.length() > 0) {
			// Set if valid property and value
			if (value != null) {
				listProperties.put(propertyName, value);
			}
			// Set to empty list if not a valid value
			else {
				listProperties.put(propertyName, Constants.EMPTY_VALUE_LIST);				
			}
		}
		// Log warning if invalid property name 
		else {
			Session.logger.warn("Ignoring setListPropertyValue(..) invocation with invalid property name on object {}", getObjectId());
		}
	}

	/**
	 * Method to remove list property value on session object
	 * @param propertyName String name of list property
	 */
	void removeListPropertyValue(String propertyName) {
		if (listProperties.containsKey(propertyName)) {
			listProperties.remove(propertyName);
		}
	}

	/**
	 * Helper method to remove child objects prior to removal of self 
	 */
	void prepareForRemoval() {
		// Remove needers
		for(SessionObject child : childNeeders) {
			session.removeFromSession(child);
		}
		
		// Remove providers
		for(SessionObject child : childProviders) {
			session.removeFromSession(child);
		}
	}	
	
	/**
	 * Method to get parent session object
	 * @return SessionObject for parent, or null
	 */
	public SessionObject getParentObject() {
		return parentObject;
	}
	
	/**
	 * Method to get unmodifiable list of child needer session objects
	 * @return List of SessionObject needers
	 */
	public List<SessionObject> getChildNeeders() {
		return childNeeders;
	}

	/**
	 * Method to get unmodifiable list of child provider session objects
	 * @return List of SessionObject providers
	 */
	public List<SessionObject> getChildProviders() {
		return childProviders;
	}
	
	/**
	 * Method to get session handle
	 * @return Session object
	 */
	public Session getSession() {
		return session; 
	}
	
	/**
	 * Method to get model object handle
	 * @return ModelObject from which session object was created
	 */
	public ModelObject getModelObject() {
		return modelObject;
	}
	
	/**
	 * Method to get session object unique ID
	 * @return String session object ID
	 */
	public String getObjectId() {
		return sessionObjectId;
	}
	
	/**
	 * Method to get value of a property. Most specific value is computed.
	 * @param propertyName String name of property
	 * @return Object value
	 */
	public Object getPropertyValue(String propertyName) {
		// Return session object value if it exists
		if (valueProperties.containsKey(propertyName)) {
			return valueProperties.get(propertyName);
		}
		// Else, get most specific value from model hierarchy
		else {
			return modelObject.getPropertyValue(propertyName);
		}
	}
	
	/**
	 * Method to get list value of a property. Most specific value is computed.
	 * @param propertyName
	 * @return List of values
	 */
	public List<?> getListPropertyValue(String propertyName) {
		// Return session object value if it exists
		if (listProperties.containsKey(propertyName)) {
			return listProperties.get(propertyName);
		}
		// Else, get most specific value from model hierarchy
		else {
			return modelObject.getListPropertyValue(propertyName);
		}		
	}

	/**
	 * Debug method to report session object details after validation
	 * @param tab String to indent for multi-level formatting 
	 * @param skipProviders Flag to skip separate provider listing on output
	 * @return String of detailed output
	 */
	public String printDetails(String tab, boolean skipProviders) {
		StringBuilder sb = new StringBuilder();
		
		boolean isTopLevel = getParentObject() == null;
		String partNumber = Constants.EMPTY;
		String validationStatus = Constants.EMPTY;
		
		// Display part number and validation status for parent objects
		if(isTopLevel) {
	        partNumber = (String) getPropertyValue(Constants.PART_NUMBER);
	        validationStatus = getValidationStatus().toString();
		}
		
		sb.append(tab).append(getModelObject().getObjectId()).append(" (ID = ").append(getObjectId()).append(")");
		if (partNumber.length() > 0) {
			sb.append(", Part Number = ").append(partNumber);
		}
		if(validationStatus.length() > 0) {
			sb.append(", Status = ").append(validationStatus);
		}
		sb.append(Constants.EOL);
		
		// Display child needers and their satisfying providers if they exist
		if (childNeeders.size() > 0) {
			sb.append(tab).append("NEEDERS:").append(Constants.EOL);
			for(SessionObject needer : childNeeders) {
				sb.append(needer.printDetails("\t", skipProviders));
				String satisfyingModelObjectId = (String) needer.getPropertyValue(Constants.SATISFYING_PROVIDER_MODEL_ID);
				sb.append("\t\tSatisfying Provider = ").append(needer.getPropertyValue(Constants.SATISFYING_PROVIDER_MODEL_ID));
				if (satisfyingModelObjectId.length() > 0) {
					sb.append(" (ID = ").append(needer.getPropertyValue(Constants.SATISFYING_PROVIDER_ID)).append(")");
				}
				sb.append(Constants.EOL);
			}
		}

		// Display all child providers separately based on optional flag
		if (!skipProviders && childProviders.size() > 0) {
			sb.append(tab).append("PROVIDERS:").append(Constants.EOL);
			for(SessionObject provider : childProviders) {
				sb.append(provider.printDetails("\t", skipProviders));
				sb.append("\t\tUnused Quantity = ").append(provider.getPropertyValue(Constants.QTY_PROVIDED));
				sb.append(Constants.EOL);
			}
		}

		return sb.toString();
	}

	/**
	 * Method to get unique ID description
	 */
	public String toString() {		
		return  (getParentObject() != null ? getParentObject().getModelObject() + " :: " : "")
				+ 
				getModelObject().getObjectId() + " (" + getObjectId() + ")";
	}
}
