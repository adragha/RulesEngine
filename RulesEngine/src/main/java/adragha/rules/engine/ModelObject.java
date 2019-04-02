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
import java.util.List;
import java.util.Map;

import adragha.rules.engine.comparator.ModelObjectValidationOrderComparator;

/**
 * Class to represent a model object. All user defined objects inherit from
 * a model class. Model object definitions are loaded from JSON files 
 * named after the corresponding model class. Model objects can be parent
 * objects or child objects. Child objects represent needers and providers. 
 * 
 * @author adragha
 *
 */
public final class ModelObject {
	/** Map of single valued model properties specified on model object. Specified in JSON model class file. */
	private Map<String, Object> valueProperties = Constants.EMPTY_VALUE_MAP;
	/** Map of list value model properties specified on model object. Specified in JSON model class file. */
	private Map<String, List<?>> listProperties = Constants.EMPTY_VALUE_LIST_MAP;
	
	/** Parent model class */
	private ModelClass modelClass = null;
	/** Model object ID */
	private String objectId = null;
	/** Computed and cached applicable rules */
	private List<ModelObject> applicableRules = new ArrayList<ModelObject>();
	
	/**
	 * Method to initialize model object after creation from JSON data
	 * @param modelClass ModelClass parent object
	 */
	void initialize(ModelClass modelClass) {
		// Set model object ID
		objectId = getPropertyValue(Constants.OBJECT_ID).toString();

		// Set parent class
		this.modelClass = modelClass;		

		// Mark value properties as unmodifiable
		if (valueProperties != null) {
			this.valueProperties = Collections.unmodifiableMap(valueProperties);
		}

		// Mark both individual lists in list value map and the map itself as unmodifiable  
		listProperties = Util.getUnmodifiableMapOfLists(listProperties);		
	}
	
	/**
	 * Method to get the value of a property. The most specific value of the property
	 * from the model class hierarchy is returned. Needs to be cast/handled appropriately 
	 * by the invoking code. Returns an empty string if the property is not defined.
	 * @param propertyName String name of the property
	 * @return Object value
	 */
	public Object getPropertyValue(String propertyName) {
		if (valueProperties.containsKey(propertyName)) {
			return valueProperties.get(propertyName);
		}
		else {
			return modelClass.getPropertyValue(propertyName);
		}
	}
	
	/**
	 * Method to get an unmodifiable value list of a list property. The most specific value
	 * list from the model class hierarchy is returned. Needs to be cast/handled appropriately 
	 * by the invoking code. Returns an empty list if the property is not defined.
	 * @param propertyName String name of the property
	 * @return List of objects that can not be modified
	 */
	public List<?> getListPropertyValue(String propertyName) {
		if (listProperties.containsKey(propertyName)) {
			return listProperties.get(propertyName);
		}
		else {
			return modelClass.getListPropertyValue(propertyName);
		}		
	}
	
	/**
	 * Method to get model object ID
	 * @return String value of object ID
	 */
	public String getObjectId() {
		return objectId;
	}
	
	/**
	 * Method to get model class parent
	 * @return ModelClass parent object 
	 */
	public ModelClass getModelClass() {
		return modelClass;
	}
	
	/**
	 * Method to check if model object is derived from the specified model class
	 * @param ancestorClass ModelClass to check against
	 * @return true if a descendant, false otherwise
	 */
	public boolean isDescendant(ModelClass ancestorClass) {
		// Return true if match found
		if (modelClass == ancestorClass) {
			return true;
		}
		// Recurse on parent if it exists and match not found yet
		else {
			if (modelClass.getParentClass() != null) {
				return modelClass.getParentClass().isDescendant(ancestorClass);
			}
		}
		
		// Return false if no match found
		return false;
	}
	
	/**
	 * Method to get unmodifiable list of rule model objects applicable to this model object 
	 * @return List of ModelObject of applicable rules
	 */
	public List<ModelObject> getApplicableRules() {		
		return applicableRules;
	}
	
	/**
	 * Package helper method to add applicable rule to model object during knowledge base load
	 * @param ruleData ModelObject for rule
	 */
	void addApplicableRule(ModelObject ruleData) {
		applicableRules.add(ruleData);
	}
	
	/**
	 * Package helper method to make applicable rule collection unmodifiable
	 * @param modelObjectValidationOrderComparator Comparator to sort rules first on validation order
	 */
	void makeApplicableRulesUnmodifiable(ModelObjectValidationOrderComparator modelObjectValidationOrderComparator) {
		// First, sort rules based on validation order
		Collections.sort(applicableRules, modelObjectValidationOrderComparator);
		
		// Mark collection as unmodifiable
		applicableRules = Collections.unmodifiableList(applicableRules);
	}
	
	/**
	 * Debug method to print model object information
	 */
	public void printData() {
		KnowledgeBase.logger.debug("Model Object: {}", getPropertyValue("objectId"));

		for(Map.Entry<String, Object> svEntry : valueProperties.entrySet()) {
			KnowledgeBase.logger.debug("\t {} = {}", svEntry.getKey(), svEntry.getValue());
		}

		for(Map.Entry<String, List<?>> mvEntry : listProperties.entrySet()) {
			KnowledgeBase.logger.debug("\t {} = {}", mvEntry.getKey(), mvEntry.getValue().toString());
		}
	}
	
	/**
	 * Method to get model object ID as unique description
	 */
	@Override
	public String toString() {
		return getObjectId();
	}
}
