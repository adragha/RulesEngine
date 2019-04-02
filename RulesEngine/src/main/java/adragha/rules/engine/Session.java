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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import adragha.rules.engine.SessionObject.ValidationStatus;
import adragha.rules.engine.ValidationStep.ValidationStage;
import adragha.rules.engine.comparator.ModelObjectIdComparator;
import adragha.rules.engine.comparator.SessionObjectValidationOrderComparator;

/**
 * Class for validation session. A session receives input selections
 * that it must then validate in the appropriate order. The validation
 * process attempts to satisfy all the requirements for each selection
 * using a backtracking, generative search mechanism that evaluates the
 * alternatives at each step, backtracks or adds steps as necessary until
 * it either finds a solution or fails to do so. Each selection is processed
 * independently, and will therefore succeed or fail independently.  
 * A selection is represented by a session object and each of its requirements
 * are represented by associated 'child' needer session objects. The
 * backtracking search is performed over the selections' child needer objects
 * and the needers associated with any new session objects the process spawns. 
 * 
 * @author adragha
 *
 */
public final class Session {
	/** Logger for session instance logging */
	static final Logger logger = LoggerFactory.getLogger(Session.class);
	/** Auto-assigned unique ID for session */
	private String sessionId;
	/** Counter for unique IDs assigned to session objects */
	private int objectIdCounter = 1000;
	/** Reference to knowledge base for session */
	private KnowledgeBase knowledgeBase = null;
	/** Map of session objects by model object and session object ID */
	private Map<ModelObject, Map<String, SessionObject>> sessionObjects = new LinkedHashMap<ModelObject, Map<String, SessionObject>>();
	/** List of input selections that drive validation */
	private List<InputSelection> inputSelections = new ArrayList<InputSelection>();
	/** List of session objects corresponding to input selections in order of validation */
	private List<SessionObject> selectionQueue = new ArrayList<SessionObject>();
	/** Stack of unprocessed validation steps in reverse order of processing. Validation is complete when this stack is empty */
	private Stack<ValidationStep> unprocessedStack = new Stack<ValidationStep>();
	/** Stack of processed validation steps in reverse order of processing. */
	private Stack<ValidationStep> processedStack = new Stack<ValidationStep>();
	/** Counter for unique step IDs assigned to validation steps */
	private int stepIdCounter = 0;
	/** Flag to indicate if session was previously validated */
	private boolean alreadyValidated = false;
	/** Re-usable comparator to correctly order session objects awaiting validation */
	private final SessionObjectValidationOrderComparator sessionObjectValidationOrderComparator = new SessionObjectValidationOrderComparator();
	
	/**
	 * Constructor for session. Call from SessionManager.
	 * @param knowledgeBase KnowledgeBase object reference
	 */
	Session(KnowledgeBase knowledgeBase) {
		this.sessionId = UUID.randomUUID().toString();
		this.knowledgeBase = knowledgeBase;
	}
	
	/**
	 * Method to get session ID
	 * @return String value of session ID
	 */
	public String getSessionId() {
		return sessionId;
	}
	
	/**
	 * Method to get input selections to be validated
	 * @return List of InputSelection to be validated
	 */
	List<InputSelection> getInputSelections() {
		return inputSelections;
	}
	
	/**
	 * Package method to create parent session object. Invoked for input selections
	 * and potential new providers created to satisfy a requirement. 
	 * @param modelObjectId String ID of ModelObject for which session object is being created
	 * @return SessionObject created
	 * @throws EngineException
	 */
	SessionObject createSessionObject(String modelObjectId) throws EngineException {
		// Get model object from model object ID
		ModelObject modelObject = knowledgeBase.getModelObject(modelObjectId);
		 
		// Throw exception if model object not found
		if (modelObject == null) {
			throw new EngineException("Cannot create session object. Invalid model object ID: " + modelObjectId, getKnowledgeBase().getName(), getSessionId());
		}
		 
		// Throw exception if model object is not a parent object.
		// Child objects are created only by their parents. 
		// Rule model objects can not be created in a session.
		if (!modelObject.isDescendant(knowledgeBase.getModelClass(Constants.BASE_OBJECT_CLASS))) {
			throw new EngineException("Cannot directly create session object for child model object ID: " + modelObjectId, getKnowledgeBase().getName(), getSessionId());
		}

		return new SessionObject(this, modelObject, getNextObjectId(), null);
	}

	/**
	 * Method to get re-usable comparator to correctly order session objects awaiting validation
	 * @return SessionObjectValidationOrderComparator object
	 */
	SessionObjectValidationOrderComparator getSessionObjectValidationOrderComparator() {
		return sessionObjectValidationOrderComparator;
	}
	
	/**
	 * Package helper method to get map of rule model object IDs to their corresponding Java singleton objects
	 * @param needer SessionObject for child needer
	 * @return Ordered Map<ModelObject, IRule> object 
	 */
	Map<ModelObject, IRule> getApplicableRules(SessionObject needer) {
		// Allocate map to return
		Map<ModelObject, IRule> applicableRules = new LinkedHashMap<ModelObject, IRule>();		
		
		// Get needer protocol type
		String neederProtocol = (String) needer.getPropertyValue(Constants.PROTOCOL_TYPE);

		if (neederProtocol.length() > 0) {
			// Get all rule model objects applicable to needer parent model object
			List<ModelObject> modelComputedRules = needer.getParentObject().getModelObject().getApplicableRules();
			for(ModelObject ruleData : modelComputedRules) {
				String ruleProtocolType = (String) ruleData.getPropertyValue(Constants.PROTOCOL_TYPE);
				// If rule protocol type matches needer, then add rule to return map
				if (neederProtocol.equals(ruleProtocolType) || Constants.ANY_PROTOCOL.equals(ruleProtocolType)) {
					applicableRules.put(ruleData, knowledgeBase.getRuleClass(ruleData.getPropertyValue(Constants.RULE_JAVA_CLASS)));
				}
			}		
		}
		
		return applicableRules;
	}
	
	/**
	 * Package helper method to add a parent session object to the validation stack
	 * @param object SessionObject parent
	 * @param parentStep ValidationStep that spawns this step (null for input selection itself)
	 * @throws EngineException
	 */
	void addParentToValidationStack(SessionObject object, ValidationStep parentStep) throws EngineException {
		List<SessionObject> childNeeders = object.getChildNeeders();
		
		// Add needer children to validation stack in reverse order of validation to maintain intended order
		if (childNeeders.size() > 0) {
			for(int i = childNeeders.size() - 1; i >= 0; i--) {
				addNeederValidationStep(new ValidationStep(this, childNeeders.get(i), parentStep));
			}			
		}
	}
	
	/**
	 * Method to create and queue an input selection prior to session validation
	 * @param selection InputSelection object with model object ID and quantity
	 * @throws EngineException
	 */
	public void createAndQueueSelection(InputSelection selection) throws EngineException {
		// Throw exception if already validated. The intent here is to make the Session instance effectively stateless, but
		// that can be changed in the future to also support stateful sessions that allow incremental validation. 
		if (alreadyValidated) {
			throw new EngineException("A Session instance is intended to be stateless and can only be validated once. Create a new Session instance to validate again.", getKnowledgeBase().getName(), getSessionId());
		}
		
		// Add selection to list
		inputSelections.add(selection);
		
		// Setup session object queue from inputs
		for(int i=0; i < selection.getSelectionQuantity(); i++) {
			selectionQueue.add(createSessionObject(selection.getSelectionId()));
		}
	}
	
	/**
	 * Method to trigger selection validation. Should be called after all selection inputs have been added.
	 * @throws EngineException
	 */
	public void validateSelections() throws EngineException {
		// Set validation flag on session
		alreadyValidated = true;
		
		// Sort selection queue for inputs by validation order
		Collections.sort(selectionQueue, getSessionObjectValidationOrderComparator());

		// Process ordered selection queue till it is empty
		while(selectionQueue.size() > 0) {
			// Reset processing stacks
			unprocessedStack.clear();
			processedStack.clear();
			
			// Find next queued selection that is yet to be validated. This check needed to avoid processing 
			// a queued selection that was already processed while satisfying a prior queued selection
			int nextUnvalidatedSelectionIndex = -1;
			for(int i=0; i < selectionQueue.size(); i++) {
				if (selectionQueue.get(i).getValidationStatus() == ValidationStatus.UNVALIDATED) {
					nextUnvalidatedSelectionIndex = i;
					break;
				}
			}
			
			// If unvalidated selection exists, process it
			if (nextUnvalidatedSelectionIndex >= 0) {
				// Remove any queued selections that have already been validated
				for(int removeIndex=0; removeIndex < nextUnvalidatedSelectionIndex; removeIndex++) {
					selectionQueue.remove(removeIndex);
				}
				
				logger.debug("Starting validation of input selection: {}", selectionQueue.get(0));
				// Add next un-validated selection to processing stack
				addParentToValidationStack(selectionQueue.remove(0), null);
	
				// Process stack till it is empty (i.e., succeeds or fails)
	   		    while(unprocessedStack.size() > 0) {
					// Validate top item on the unprocessed stack
	   		    	logger.debug("UNPROCESSED = {} :: PROCESSED = {}", unprocessedStack.toString(), processedStack.toString());
					unprocessedStack.peek().validate();
				}		
	
	   		    // Log current state when debugging
				logger.debug("UNPROCESSED = {} :: PROCESSED = {}", unprocessedStack.toString(), processedStack.toString());
			}
			// Else, clear already validated queue if necessary and exit
			else {
				selectionQueue.clear();
			}
		}
	}
	
	/**
	 * Method to get modifiable list of session objects corresponding to a model object
	 * @param modelObject ModelObject from which session objects were created
	 * @return List of SessionObject. Always non-null. 
	 */
	public List<SessionObject> getSessionObjects(ModelObject modelObject) {
		List<SessionObject> objects = new ArrayList<SessionObject>();
		
		// Get objects in session from map using model object as key
		if (sessionObjects.containsKey(modelObject)) {
			objects.addAll(sessionObjects.get(modelObject).values());
		}
		
		return objects;
	}
	
	/**
	 * Method to get count of session objects corresponding to a model object
	 * @param modelObject ModelObject from which session objects were created
	 * @return Integer count
	 */
	public int getSessionObjectCount(ModelObject modelObject) {
		int count = 0;
		
		if (sessionObjects.containsKey(modelObject)) {
			count = sessionObjects.get(modelObject).size();
		}
		
		return count;
	}
	
	/**
	 * Helper method to get unique ID for session object
	 * @return String value of unique object ID
	 */
	private String getNextObjectId() {
		objectIdCounter++;
	 
		return Integer.toString(objectIdCounter);
	}
	 
	/**
	 * Helper method to get unique ID for validation step object
	 * @return String value of step ID
	 */
	int getNextStepId() {
		return stepIdCounter++;
	}
	
	/**
	 * Helper method to add a session object to the session.
	 * @param object SessionObject to add
	 */
	void addToSession(SessionObject object) {
		Map<String, SessionObject> objects = sessionObjects.get(object.getModelObject());
		 
		// Create map based on model object if it doesn't already exist
		if(objects == null) {
			objects = new LinkedHashMap<String, SessionObject>();
			sessionObjects.put(object.getModelObject(), objects);
		}
		 
		// Put session object in map by session object ID
		objects.put(object.getObjectId(), object);
	}
	
	/**
	 * Helper method to remove a session object from the session.
	 * @param object
	 */
	void removeFromSession(SessionObject object) {
		// Call pre-removal logic
		object.prepareForRemoval();
		 
		// Remove from map based on the associated model object 
		sessionObjects.get(object.getModelObject()).remove(object.getObjectId());
	}
	
	/**
	 * Helper method to add a validation step for a needer object to the top of the processing stack
	 * @param step ValidationStep object
	 */
	void addNeederValidationStep(ValidationStep step) {
		unprocessedStack.push(step);
		logger.debug("...Adding validation: {} for {}", step.toString(), step.getTargetObject().toString());
	}
	
	/**
	 * Helper method to move a satisfied validation step from the unprocessed to processed stack
	 * @param satisfyingObject SessionObject that satisfies step's needer object 
	 * @throws EngineException
	 */
	void confirmValidationStep(SessionObject satisfyingObject) throws EngineException {
		// Pop and push
		ValidationStep step = unprocessedStack.pop(); 
		processedStack.push(step);		
		// Log when debugging
		logger.debug("...Satisfied validation: {} for {} with {}", 
				     step.toString(), 
				     step.getTargetObject().toString(),
				     satisfyingObject.toString()
				    );
	}
	
	/**
	 * Helper method to replace a failed step with a new step in a disjunction 
	 * @param newStep ValidationStep object that replaces the failed step
	 */
	void replaceDisjunctionStep(ValidationStep newStep) {
		ValidationStep failedStep = unprocessedStack.pop();
		failedStep.cleanupFailedStep(true);
		addNeederValidationStep(newStep);
	}

	/**
	 * Helper method to process a failed step and backtrack
	 * @param failedStep ValidationStep object for failed step
	 * @throws EngineException
	 */
	void failValidationStep(ValidationStep failedStep) throws EngineException {
		// Get last successful step
		ValidationStep lastSuccessful = null;
		if (processedStack.size() > 0) {
			lastSuccessful = processedStack.peek();					
		}
		
		// If last successful step exists, attempt to backtrack
		if(lastSuccessful != null) {
			// If the failed step is a child of the last successful step, then discard
			// the failed step and all its unprocessed siblings before reverting
			if(failedStep.getParentStep() == lastSuccessful) {
				List<ValidationStep> currentChildSteps = new ArrayList<ValidationStep>(lastSuccessful.getChildSteps()); 
				for(int i = currentChildSteps.size() - 1; i >= 0; i--) {
					ValidationStep child = currentChildSteps.get(i);
					if (unprocessedStack.contains(child)) {
						unprocessedStack.remove(child);
						child.cleanupFailedStep(true);
					}
				}				
			}
			// If the failed step is not a parent of the last successful step, then...
			else {
				//...if failed step is the second part of a disjunction, then replace it with the first part and let that roll-back as needed
				if (failedStep.getStage() == ValidationStage.NEW) {
					replaceDisjunctionStep(new ValidationStep(this, failedStep.getTargetObject(), failedStep.getParentStep(), ValidationStage.EXISTING));						
				}
				//...else just clean up failed step without discarding it so it is validation again later
				else {
					failedStep.cleanupFailedStep(false);
				}
			}

			// Revert changes, and try next alternative in last successful step
			lastSuccessful.revertChanges();
					
			// Revert stack to previous state
			processedStack.pop();
			unprocessedStack.push(lastSuccessful);

			logger.debug("...Reverting to next option in {} for {}", lastSuccessful.toString(), lastSuccessful.getTargetObject().toString());			
		}
		// If this is the first step and it has failed, no solution exists for this input selection
		else {
			failedStep.getTargetObject().setPropertyValue(Constants.VALIDATION_STATUS, ValidationStatus.FAILED.toString());
			logger.debug("...Unable to satisfy validation {} for {}", failedStep.toString(), failedStep.getTargetObject().toString());
			// Clear unprocessed stack to end validation of current input selection
			unprocessedStack.clear();
		}
	}
	
	/**
	 * Method to prepare session for deletion by clearing data
	 */
	void prepareForDeletion() {
		inputSelections.clear();
		selectionQueue.clear();
		sessionObjects.clear();
	}
	
	/**
	 * Method to get handle on corresponding knowledge base
	 * @return KnowledgeBase object
	 */
	public KnowledgeBase getKnowledgeBase() {
		return knowledgeBase;
	}
	
	/**
	 * Method to get specific session object based on its own ID and its model object ID 
	 * @param modelObjectId String value of model object ID
	 * @param objectId String value of session object ID
	 * @return SessionObject, or null
	 * @throws EngineException
	 */
	public SessionObject getSessionObject(String modelObjectId, String objectId) throws EngineException {
		return sessionObjects.get(knowledgeBase.getModelObject(modelObjectId)).get(objectId);
	}
	 
	/**
	 * Utility method to get handle for session logger
	 * @return Logger object
	 */
	public static Logger getSessionLogger() {
		return logger;
	}
	
	/**
	 * Method to generate full session validation output
	 * @return String of validation results
	 */
	public String printOutput() {
		return printOutput(false);
	}
	
	/**
	 * Method to generate session validation output
	 * @param skipProviders Flag to determine if all child provider details should be included 
	 * @return String of validation results
	 */
	public String printOutput(boolean skipProviders) {
		StringBuilder sb = new StringBuilder();

		sb.append("\nRESULTS:\n").append(Constants.EOL);
		List<ModelObject> modelObjects = new ArrayList<ModelObject>(sessionObjects.keySet());
		Collections.sort(modelObjects, new ModelObjectIdComparator());
		for(ModelObject modelObject : modelObjects) {
			for(SessionObject object : sessionObjects.get(modelObject).values()) {
				if (object.getParentObject() == null) {
					sb.append(object.printDetails(Constants.EMPTY, skipProviders)).append(Constants.EOL);
				}
			}
		}
		
		return sb.toString();
	}
}

