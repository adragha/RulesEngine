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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import adragha.rules.engine.SessionObject.ValidationStatus;
import adragha.rules.engine.comparator.ModelObjectIdComparator;
import adragha.rules.engine.comparator.SessionObjectIdComparator;

/**
 * Class for individual validation step in generative, backtracking search.
 * Operates on a target object (a child needer session object), and computes
 * prioritized, valid alternatives to satisfy it. The step can represent one
 * of three validation stages: finding an existing provider, creating a new
 * provider parent, and consuming the new provider. The existing provider 
 * stage is always tried first. If it doesn't succeed, the next two stages 
 * are both attempted, and both must succeed for the validation step to succeed.
 * 
 * @author adragha
 *
 */
final class ValidationStep {
	/** Enum for validation stages */
	static enum ValidationStage {
		EXISTING("Find Existing Provider"), 
		NEW("Create New Provider Parent"), 
		EXISTING_AFTER_NEW("Use Created Provider");
		
		/** Validation stage description */
		private String description = "";
		
		/**
		 * Enum constructor
		 * @param description String description of stage
		 */
		ValidationStage(String description) {
			this.description = description;
		}
		
		/**
		 * Method to get validation stage description
		 * @return String description of stage
		 */
		String getDescription() {
			return description;
		}
	}

	/** Reference to session to which step belongs */
	private Session session = null;
	/** Target session object (a child needer) that validation step must satisfy */
	private SessionObject targetObject = null;
	/** Parent validation step (if any) */
	private ValidationStep parentStep = null;
	/** List of child steps (if any) */
	private List<ValidationStep> childSteps = new ArrayList<ValidationStep>(0);
	/** Validation stage for step */
	private ValidationStage stage = null;
	/** Stack of reversible changes to apply when backtracking */
	private Stack<ReversibleChange> undoStack = new Stack<ReversibleChange>();
	/** List of existing child providers if in that stage */
	private List<SessionObject> cachedExistingProviders = null;	
	/** List of new provider parents if in that stage */
	private List<ModelObject> cachedNewProviders = null;	
	/** Index of next valid provider alternative */
	private int alternativeIndex = 0;
	/** Flag to indicate a new provider parent was created */
	private SessionObject newProviderParentCreated = null;
	/** Auto-assigned step ID */
	private int stepId = 0;

	/**
	 * Constructor for validation step
	 * @param session Session being validated
	 * @param targetObject SessionObject for child needer being validated
	 * @param parentStep ValidationStep object parent (or null)
	 * @throws EngineException
	 */
	ValidationStep(Session session, SessionObject targetObject, ValidationStep parentStep) throws EngineException {
		this(session, targetObject, parentStep, ValidationStage.EXISTING);
	}

	/**
	 * Constructor for validation step with stage argument
	 * @param session Session being validated
	 * @param targetObject SessionObject for child needer being validated
	 * @param parentStep ValidationStep object parent (or null)
	 * @param stage ValidationStage enum value
	 * @throws EngineException
	 */
	ValidationStep(Session session, SessionObject targetObject, ValidationStep parentStep, ValidationStage stage) throws EngineException {
		this.session = session;
		this.targetObject = targetObject;
		this.parentStep = parentStep;
		if (this.parentStep != null) {
			this.parentStep.childSteps.add(this);
		}
		this.stage = stage;
		this.stepId = session.getNextStepId();
	}

	/**
	 * Method to get parent step
	 * @return ValidationStep parent or null
	 */
	ValidationStep getParentStep() {
		return parentStep;
	}
	
	/**
	 * Method to get list of child steps
	 * @return List of ValidationStep
	 */
	List<ValidationStep> getChildSteps() {
		return childSteps;
	}

	/**
	 * Method to get index of step as ID
	 * @return Integer step ID
	 */
	int getStepId() {
		return stepId;
	}
	
	/**
	 * Method to get step stage
	 * @return ValidationStage value
	 */
	ValidationStage getStage() {
		return stage;
	}
	
	/**
	 * Helper method to validate individual step. Initial attempt is to find valid existing 
	 * providers. If that fails, new providers are created in a separate step followed by a 
	 * second step in which the new provider created is consumed.
	 * @throws EngineException
	 */
	void validate() throws EngineException {
		// Get protocol type and quantity needed by child needer
		double quantityNeeded = ((Double) targetObject.getPropertyValue(Constants.QTY_NEEDED)).doubleValue();
		String protocolType = (String) targetObject.getPropertyValue(Constants.PROTOCOL_TYPE);

		// Revert side-effects of trying the previous alternative
		if(alternativeIndex > 0) {
			revertChanges();
		}

		// Process 'EXISTING or EXISTING_AFTER_NEW' stage
		if (stage != ValidationStage.NEW) {
			validateWithExisting(quantityNeeded, protocolType);
		}
		// Process 'NEW' stage
		else {
			validateWithNew(quantityNeeded, protocolType);
		}
	}

	/**
	 * Helper method to validate individual step using existing providers. 
	 * @param quantityNeeded Quantity of protocol needed to satisfy needer
	 * @param protocolType Type of protocol needed
	 * @throws EngineException
	 */
	private void validateWithExisting(double quantityNeeded, String protocolType) throws EngineException {
		// Find and cache all existing if not done previously			
		if(cachedExistingProviders == null) {
			cachedExistingProviders = getExistingAlternatives(targetObject, quantityNeeded, protocolType);
		}
		
		// Log remaining valid, sorted alternatives when debugging
		Session.logger.debug(printRemainingAlternatives(cachedExistingProviders, alternativeIndex));
		
		// Get next alternative on the existing list
		if(cachedExistingProviders.size() > alternativeIndex) {
			// Get handle on provider
			SessionObject existingProvider = cachedExistingProviders.get(alternativeIndex);
			// Get handle on provider parent
			SessionObject existingObjectContext = existingProvider.getParentObject() != null ? existingProvider.getParentObject() : existingProvider;

			// Increment provider index for next call to this method
			alternativeIndex++;

			// Consume provider quantity, and update needer, and provider session object properties using reversible changes
			updateObjectValue(targetObject, Constants.QTY_NEEDED, Double.valueOf(0.0));
			double remainingQuantity = ((Double) existingProvider.getPropertyValue(Constants.QTY_PROVIDED)).doubleValue() - quantityNeeded;
			// Update remaining quantity on child provider
			updateObjectValue(existingProvider, Constants.QTY_PROVIDED, Double.valueOf(remainingQuantity));
			// Set satisfying child provider type on child needer
			updateObjectValue(targetObject, Constants.SATISFYING_PROVIDER_MODEL_ID, existingProvider.getModelObject().getObjectId());
			// Set satisfying child provider ID on child needer
			updateObjectValue(targetObject, Constants.SATISFYING_PROVIDER_ID, existingProvider.getObjectId());
			// Set validation status of child needer as successful
			updateObjectValue(targetObject, Constants.VALIDATION_STATUS, ValidationStatus.SUCCESSFUL.toString());
			// Confirm validation step and move it to the processed stack
			session.confirmValidationStep(existingProvider); 
				
			// If the provider parent is a previously un-validated input selection, validate it next 
			if (existingObjectContext != targetObject.getParentObject()
				&&
			    existingObjectContext.getValidationStatus() != ValidationStatus.SUCCESSFUL
			   ) 
			{
				session.addParentToValidationStack(existingObjectContext, this);
			}	
		}
		// If no existing providers alternatives remain, add validation steps to create a new provider and use it if permitted
		else {
			// Determine if new providers should be allowed
			boolean tryNewProviders = ((Boolean) targetObject.getPropertyValue(Constants.TRY_NEW_PROVIDERS)).booleanValue();
			
			// Try 'NEW' validation stage if it is allowed, and was not tried before
			if (tryNewProviders && stage == ValidationStage.EXISTING) {
				// Replace 'EXISTING' step with the 'NEW' step on the unprocessed stack 
				session.replaceDisjunctionStep(new ValidationStep(session, targetObject, parentStep, ValidationStage.NEW));
			}
			// Else, fail this step
			else {
				session.failValidationStep(this);
			}
		}
	}

	/**
	 * Helper method to validate individual step by creating new provider. 
	 * @param quantityNeeded Quantity of protocol needed to satisfy needer
	 * @param protocolType Type of protocol needed
	 * @throws EngineException
	 */
	private void validateWithNew(double quantityNeeded, String protocolType) throws EngineException {
		// Find and cache all new provider parents if not done previously			
		if(cachedNewProviders == null) {
			cachedNewProviders = getNewAlternatives(targetObject, quantityNeeded, protocolType);
		}
		
		// Log remaining valid, sorted alternatives when debugging
		Session.logger.debug(printRemainingAlternatives(cachedNewProviders, alternativeIndex));
		
		// Get next alternative on new list
		if(cachedNewProviders.size() > alternativeIndex) {
			// Get new provider parent created
			newProviderParentCreated = session.createSessionObject(cachedNewProviders.get(alternativeIndex).getObjectId());
			
			// Increment provider index for next call to this method
			alternativeIndex++;

			// Confirm validation step and move it to the processed stack
			session.confirmValidationStep(newProviderParentCreated); 

			// Add follow-up steps in reverse order - first validate new provider parent, then consume provider created for this needer			
			session.addNeederValidationStep(new ValidationStep(session, targetObject, this, ValidationStage.EXISTING_AFTER_NEW));			
			session.addParentToValidationStack(newProviderParentCreated, this);					
		}
		// Fail validation step if no alternatives remain
		else {
			session.failValidationStep(this);					
		}		
	}

	/**
	 * Package method to get validation step target session object, i.e., child needer
	 * @return SessionObject for child needer
	 */
	SessionObject getTargetObject() {
		return targetObject;
	}
	
	/**
	 * Helper method to cleanup failed validation step to re-try later or discard
	 * @param discard Flag to specify if step is being discarded
	 */
	void cleanupFailedStep(boolean discard) {		
		// Log failure when debugging
		Session.logger.debug("...{} unsatisfiable validation: {} for {}", 
				            discard ? "Discarding" : "Undoing",
				            toString(), 
				            targetObject.toString()
				           );

		// Clear any previous cache of existing providers
		if (cachedExistingProviders != null) {
			cachedExistingProviders.clear();
			cachedExistingProviders = null;
		}

		// Clear any previous cache of new provider parents
		if (cachedNewProviders != null) {
			cachedNewProviders.clear();
			cachedNewProviders = null;
		}

		// Reset alternative index
		alternativeIndex = 0;

		// If discarding this step, remove associated references
		if (discard) {
			targetObject = null;
			
			if (parentStep != null) {
				parentStep.childSteps.remove(this);
				parentStep = null;
			}
		}
	}

	/**
	 * Method to get valid, sorted list of existing child provider session objects that could satisfy needer 
	 * @param needer SessionObject for child needer
	 * @param quantityNeeded Quantity of protocol needed to satisfy needer
	 * @param protocolType Type of protocol needed
	 * @return List of SessionObject of valid, sorted child providers
	 * @throws EngineException
	 */
	private List<SessionObject> getExistingAlternatives(SessionObject needer, double quantityNeeded, String protocolType) throws EngineException {
		// Allocate return list
		List<SessionObject> existingProviders = new ArrayList<SessionObject>();
		
		// To compute potential providers, first get existing provider parent model objects
		List<ModelObject> matchingDataObjects = new ArrayList<ModelObject>();
		for (Object classId : targetObject.getListPropertyValue(Constants.PROVIDER_CLASS_ID_LIST)) {
			ModelClass applicableClass = session.getKnowledgeBase().getModelClass(classId);
			applicableClass.computeRecursiveDescendantData(matchingDataObjects);
		}

		// Reduce to unique model object list and get existing session objects that match it
		Set<ModelObject> uniqueDataObjects = new HashSet<ModelObject>(matchingDataObjects);
		for (ModelObject modelObject : uniqueDataObjects) {
			for (SessionObject object : session.getSessionObjects(modelObject)) {
				// If stage is 'EXISTING_AFTER_NEW', restrict choice to newly created provider (as the others have already been tried unsuccessfully)				
				if (stage != ValidationStage.EXISTING_AFTER_NEW || object == parentStep.newProviderParentCreated) {
					// If a non-failed provider parent...
					if (object.getValidationStatus() != ValidationStatus.FAILED) {
						for (SessionObject provider : object.getChildProviders()) {
							// If child provider that can satisfy needer exists, then add it to the return list
							if (protocolType.equals(provider.getPropertyValue(Constants.PROTOCOL_TYPE))
								&&
							    ((Double) provider.getPropertyValue(Constants.QTY_PROVIDED)).doubleValue() >= quantityNeeded
							   ) 
							{
								existingProviders.add(provider);
							}
						}
					}
				}
			}
		}

		// Use applicable rules to further filter and sort the list computed above
		if (existingProviders.size() > 0) {
			// Perform default sort based on session ID, which effectively sorts in order of session object creation
			Collections.sort(existingProviders, new SessionObjectIdComparator());
			// Get applicable rules for this needer (already sorted in order of application)
			Map<ModelObject, IRule> rules = session.getApplicableRules(needer);
			// Apply filtering and/or sorting by each rule to existing providers list
			for(Map.Entry<ModelObject, IRule> entry : rules.entrySet()) {
				existingProviders = entry.getValue().getExistingProviders(needer, existingProviders, entry.getKey());
			}
		}
		
		// Return valid, sorted existing providers list
		return existingProviders;
	}

	/**
	 * Method to get valid, sorted list of provider parent model objects whose child providers could satisfy needer 
	 * @param needer SessionObject for child needer
	 * @param quantityNeeded Quantity of protocol needed to satisfy needer
	 * @param protocolType Type of protocol needed
	 * @return List of ModelObject of valid, sorted provider parents that can be created
	 * @throws EngineException
	 */
	private List<ModelObject> getNewAlternatives(SessionObject needer, double quantityNeeded, String protocolType) throws EngineException {
		// Allocate return list
		List<ModelObject> newProviders = new ArrayList<ModelObject>();

		// To compute potential new providers, first get provider parent model objects
		List<ModelObject> matchingDataObjects = new ArrayList<ModelObject>();
		for (Object classId : targetObject.getListPropertyValue(Constants.PROVIDER_CLASS_ID_LIST)) {
			ModelClass applicableClass = session.getKnowledgeBase().getModelClass(classId);
			applicableClass.computeRecursiveDescendantData(matchingDataObjects);
		}

		// Reduce to unique model object list and find ones that have child providers that could satisfy needer
		Set<ModelObject> uniqueDataObjects = new HashSet<ModelObject>(matchingDataObjects);
		for (ModelObject modelObject : uniqueDataObjects) {
			List<ModelObject> childProviders = session.getKnowledgeBase().getModelObjects(modelObject.getListPropertyValue(Constants.PROVIDER_LIST));
			for (ModelObject childProvider :childProviders) {
				// If child provider has necessary protocol type and quantity, include parent in return list
				if (protocolType.equals(childProvider.getPropertyValue(Constants.PROTOCOL_TYPE))
					&&
				    ((Double) childProvider.getPropertyValue(Constants.QTY_PROVIDED)).doubleValue() >= quantityNeeded
				   ) 
				{					
					newProviders.add(modelObject);
					break;
				}
			}
		}

		// Use applicable rules to further filter and sort the list computed above
		if (newProviders.size() > 0) {
			// Perform default sort based on model object ID
			Collections.sort(newProviders, new ModelObjectIdComparator());
			// Get applicable rules for this needer (already sorted in order of application)
			Map<ModelObject, IRule> rules = session.getApplicableRules(needer);
			// Apply filtering and/or sorting by each rule to new provider parent model object list
			for(Map.Entry<ModelObject, IRule> entry : rules.entrySet()) {
				newProviders = entry.getValue().getNewProviders(needer, newProviders, entry.getKey());
			}
		}
		
		// Return valid, sorted new provider parents list
		return newProviders;
	}

	/**
	 * Debug helper method to create string of remaining alternatives from a list and starting index position
	 * @param list List of alternatives
	 * @param index Starting position index
	 * @return String of remaining alternatives
	 */
	private String printRemainingAlternatives(List<?> list, int index) {
		StringBuilder sb = new StringBuilder("Remaining Alternatives (").append(Integer.toString(Math.max(0, list.size() - index))).append("): [");
		
		for(int i=index; i < list.size(); i++) {
			sb.append(list.get(i).toString());
			
			if (i <list.size() - 1) {
				sb.append(", ");
			}
		}
		
		sb.append("]");
				
		return sb.toString();		
	}
	
	/**
	 * Debug method to describe validation step 
	 */
	public String toString() {
		if (targetObject != null) {
			return stage.getDescription() + "(" + targetObject.getObjectId() + ")";
		}
		
		return super.toString();
	}	

	/**
	 * Inner abstract class to manage reversible property value changes during validation
	 */
	private abstract class ReversibleChange {
		/** Session object that was changed */
		SessionObject changedObject = null;

		/** Name of property changed **/
		String propertyName = null;

		/** Abstract method that implements reversal of change */
		abstract void reverseChange();
	}

	/**
	 * Inner class that extends abstract class to capture change for a single valued property
	 */
	private class ValueChange extends ReversibleChange {
		/** Property value */
		Object previousValue = null;

		/**
		 * Constructor for single-valued property change
		 * @param changedObject SessionObject that changed
		 * @param propertyName String name of property
		 * @param previousValue Object value to revert to
		 */
		ValueChange(SessionObject changedObject, String propertyName, Object previousValue) {
			this.changedObject = changedObject;
			this.propertyName = propertyName;
			this.previousValue = previousValue;
		}

		/**
		 * Implementation of reversal method
		 */
		@Override
		void reverseChange() {
			changedObject.setPropertyValue(propertyName, previousValue);
		}
	}

	/**
	 * Inner class that extends abstract class to capture change for a list valued property
	 */
	private class ListChange extends ReversibleChange {
		/** List property value */
		List<?> previousValue = null;

		/**
		 * Constructor for list valued property change
		 * @param changedObject SessionObject that changed
		 * @param propertyName String name of property
		 * @param previousValue List<?> value to revert to
		 */
		ListChange(SessionObject changedObject, String propertyName, List<?> previousValue) {
			this.changedObject = changedObject;
			this.propertyName = propertyName;
			this.previousValue = previousValue;
		}

		/**
		 * Implementation of reversal method
		 */
		@Override
		void reverseChange() {
			changedObject.setListPropertyValue(propertyName, previousValue);
		}
	}	

	/**
	 * Helper method to update object value after preserving current value for future reversal
	 * @param object SessionObject being changed
	 * @param propertyName String name of property being changed
	 * @param newValue  Object value to set
	 */
	void updateObjectValue(SessionObject object, String propertyName, Object newValue) {
		// Get previous value
		Object previousValue = object.getPropertyValue(propertyName);

		// Create reversible change object
		ReversibleChange reversibleChange = new ValueChange(object, propertyName, previousValue);

		// Add to validation step stack
		undoStack.push(reversibleChange);

		// Update session object with new value
		object.setPropertyValue(propertyName, newValue);
	}

	/**
	 * Helper method to update object value after preserving current value for future reversal
	 * @param object SessionObject being changed
	 * @param propertyName String name of property being changed
	 * @param newValue  List<?> value to set
	 */
	void updateObjectListValue(SessionObject object, String propertyName, List<?> newValue) {
		// Get previous list value
		List<?> previousValue = object.getListPropertyValue(propertyName);

		// Create reversible change object
		ReversibleChange reversibleChange = new ListChange(object, propertyName, previousValue);

		// Add to validation step stack
		undoStack.push(reversibleChange);

		// Update session object with new list value
		object.setListPropertyValue(propertyName, newValue);
	}

	/**
	 * Helper method to revert changes for validation step
	 */
	void revertChanges() {
		// Revert changes in reverse order of occurrence till stack is empty
		while (undoStack.size() > 0) {			
			undoStack.pop().reverseChange();
		}
		
		// If new provider was created previously, remove it from the session
		if(newProviderParentCreated != null) {
			session.removeFromSession(newProviderParentCreated);
		}
	}
}
