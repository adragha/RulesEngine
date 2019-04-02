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

/**
 * Class to represent a model class. All user defined model classes must inherit
 * from one of the following abstract base classes: BaseObject, BaseNeeder, 
 * BaseProvider, or BaseRule. Model class definitions are loaded from JSON files 
 * named after the class.
 * 
 * @author adragha
 *
 */
public final class ModelClass {
	/** Map of single valued model properties. Specified in JSON model class file. */
	private Map<String, Object> valueProperties = Constants.EMPTY_VALUE_MAP;	
	/** Map of list value model properties. Specified in JSON model class file. */
	private Map<String, List<?>> listProperties = Constants.EMPTY_VALUE_LIST_MAP;
	/** List of model objects that inherit directly from this model class. Specified in JSON model class file. */	
	private List<ModelObject> modelObjects = Constants.EMPTY_OBJECT_LIST;
	
	/** Model class ID */
	private String classId = null;
	/** Parent model class */
	private ModelClass parentClass = null;
	/** Knowledge base handle */
	private KnowledgeBase knowledgeBase = null;
	/** Child model class list */
	private List<ModelClass> childClasses = new ArrayList<ModelClass>();
	
	/**
	 * Method to initialize model class after creation from JSON data
	 */
	void initialize() {
		// Set class ID
		classId = getPropertyValue(Constants.CLASS_ID).toString();

		// Mark value properties as unmodifiable 
		if (valueProperties != null) {
			valueProperties = Collections.unmodifiableMap(valueProperties);
		}
		
		// Mark both individual lists in list value map and the map itself as unmodifiable  
		listProperties = Util.getUnmodifiableMapOfLists(listProperties);
		
		// Mark derived model objects as unmodifiable
		modelObjects = Collections.unmodifiableList(modelObjects);
	}
	
	/**
	 * Method to set model knowledge base 
	 * @param knowledgeBase KnowledgeBase object for knowledge base this class belongs to
	 */
	void setKnowledgeBase(KnowledgeBase knowledgeBase) {
		this.knowledgeBase = knowledgeBase;				
	}
	
	/**
	 * Method to set parent class during knowledge base load
	 * @param parentClass ModelClass object representing parent class
	 */
	void setParentClass(ModelClass parentClass) {
		// Set parent
		this.parentClass = parentClass;
		
		// Update non-null parent's child model classes
		if (this.parentClass != null) {
			this.parentClass.childClasses.add(this);
		}
		else {
			if (!Constants.MODEL_ROOT_CLASSES.contains(getClassId())) {
				KnowledgeBase.logger.error("Parent class missing on model data/class {}.", getClassId());
			}
		}
	}
	
	/**
	 * Method to get model class ID
	 * @return String value of class ID
	 */
	public String getClassId() {
		return classId;
	}
	
	/**
	 * Method to get model knowledge base handle
	 * @return KnowledgeBase object
	 */
	public KnowledgeBase getKnowledgeBase() {
		return knowledgeBase;
	}
	
	/**
	 * Method to get parent model class. Returns null for base classes.
	 * @return ModelClass object for parent class (or null)
	 */
	public ModelClass getParentClass() {
		return parentClass;
	}
	
	/**
	 * Method to get the value of a property. The most specific value of the property
	 * from the model class hierarchy is returned. Needs to be cast/handled appropriately 
	 * by the invoking code. Returns an empty string if the property is not defined.
	 * @param propertyName String name of the property
	 * @return Object value
	 */
	public Object getPropertyValue(String propertyName) {
		// Return value if it is defined
		if (valueProperties.containsKey(propertyName)) {
			return valueProperties.get(propertyName);
		}
		else {
			// Call recursively on parent class 			
			if (getParentClass() != null) {
				return getParentClass().getPropertyValue(propertyName);
			}
			// Return empty string if no definition exists
			else {
				return Constants.EMPTY;
			}
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
		// Return value if it is defined
		if (listProperties.containsKey(propertyName)) {
			return listProperties.get(propertyName);
		}
		else {
			// Call recursively on parent class 			
			if (getParentClass() != null) {
				return getParentClass().getListPropertyValue(propertyName);
			}
			// Return empty list if no definition exists
			else {
				return Constants.EMPTY_VALUE_LIST;
			}
		}		
	}
	
	/**
	 * Recursive method to determine if this model class is derived from another model class
	 * @param ancestorClass ModelClass object for potential ancestor class  
	 * @return true if argument is an ancestor model class, false otherwise
	 */
	public boolean isDescendant(ModelClass ancestorClass) {
		if (this == ancestorClass) {
			return true;
		}
		else {
			if (getParentClass() != null) {
				return getParentClass().isDescendant(ancestorClass);
			}
		}
		
		return false;
	}
	
	/**
	 * Method to get unmodifiable list of model objects derived directly from this class
	 * @return List of ModelObjects, or empty list
	 */
	public List<ModelObject> getModelObjects() {
		return modelObjects;
	}
	
	/**
	 * Method to recursively compute all model object data derived from this class.
	 * Invoking method passes in a modifiable list that is updated with the result.
	 * @param dataDescendants Modifiable list of model objects for result 
	 */
	public void computeRecursiveDescendantData(List<ModelObject> dataDescendants) {
		// Add own objects to result
		dataDescendants.addAll(modelObjects);
			
		// Call recursively on all child model classes
		for(ModelClass subClass : childClasses) {
			subClass.computeRecursiveDescendantData(dataDescendants);
		}
	}
	
	/**
	 * Method to recursively compute all model class data derived from this class.
	 * Invoking method passes in a modifiable list that is updated with the result.
	 * @param classDescendants Modifiable list of model classes for result
	 */
	public void computeRecursiveDescendantClasses(List<ModelClass> classDescendants) {
		// Add self to result
		classDescendants.add(this);
			
		// Call recursively on all child model classes
		for(ModelClass subClass : childClasses) {
			subClass.computeRecursiveDescendantClasses(classDescendants);
		}
	}
	
	/**
	 * Debug method to print class information (excluding derived data)
	 */
	public void printData() {
		KnowledgeBase.logger.debug("Model Class: {} ", getPropertyValue("classId"));

		for(Map.Entry<String, Object> svEntry : valueProperties.entrySet()) {
			KnowledgeBase.logger.debug("\t {} = {}", svEntry.getKey(), svEntry.getValue());
		}

		for(Map.Entry<String, List<?>> mvEntry : listProperties.entrySet()) {
			KnowledgeBase.logger.debug("\t {} = {}", mvEntry.getKey(), mvEntry.getValue().toString());
		}
	}
}
