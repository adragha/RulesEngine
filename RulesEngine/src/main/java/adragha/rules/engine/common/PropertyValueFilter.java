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
package adragha.rules.engine.common;

import java.util.ArrayList;
import java.util.List;

import adragha.rules.engine.Constants;
import adragha.rules.engine.EngineException;
import adragha.rules.engine.IRule;
import adragha.rules.engine.ModelObject;
import adragha.rules.engine.SessionObject;

/**
 * Class for rule that filters provider candidates based on permitted values
 * of a specified property on the provider or the provider's parent.
 * 
 * @author adragha
 *
 */
public class PropertyValueFilter implements IRule {
	/** Name of property to examine on provider */
	public static final String RULE_PROPERTY_NAME = "objectPropertyName";
	/** Values of property that are permitted */
	public static final String RULE_PROPERTY_PERMITTED_VALUES = "permittedObjectPropertyValues";
	/** Flag to determine if property on provider parent should be examined. Default is true. */
	public static final String RULE_PROPERTY_ON_PARENT = "objectPropertyOnParent";

	/**
	 * Override to filter existing provider candidates based on permitted property values
	 */
	@Override
	public List<SessionObject> getExistingProviders(SessionObject needer, List<SessionObject> existingProviders, ModelObject rule) throws EngineException {
		// Get property name and permitted values
		String propertyName = (String) rule.getPropertyValue(RULE_PROPERTY_NAME);
		List<?> permittedValues = rule.getListPropertyValue(RULE_PROPERTY_PERMITTED_VALUES);
		
		// If not fully specified, skip filtering
		if (propertyName.length() == 0 || permittedValues.size() == 0) {
			return existingProviders;
		}
		
		// Check for parent provider flag
		boolean useParent = ((Boolean) rule.getPropertyValue(RULE_PROPERTY_ON_PARENT)).booleanValue();
		List<SessionObject> filteredProviders = new ArrayList<SessionObject>();
		
		for(SessionObject existingProvider : existingProviders) {
			// Get appropriate provider property value from candidate
			Object value = useParent ? existingProvider.getParentObject().getPropertyValue(propertyName) : existingProvider.getPropertyValue(propertyName);
			
			// Compare candidate value against permitted values to determine validity
			if (permittedValues.contains(value)) {
				filteredProviders.add(existingProvider);	
			}				
		}
		
		// Return filtered provider list
		return filteredProviders;
	}

	/**
	 * Override to filter new provider candidates based on permitted property values
	 */
	@Override
	public List<ModelObject> getNewProviders(SessionObject needer, List<ModelObject> newProviders, ModelObject rule) throws EngineException {
		// Get property name and permitted values
		String propertyName = (String) rule.getPropertyValue(RULE_PROPERTY_NAME);
		List<?> permittedValues = rule.getListPropertyValue(RULE_PROPERTY_PERMITTED_VALUES);
		
		// If not fully specified, skip filtering
		if (propertyName.length() == 0 || permittedValues.size() == 0) {
			return newProviders;
		}
		
		// Check for parent provider flag
		boolean useParent = ((Boolean) rule.getPropertyValue(RULE_PROPERTY_ON_PARENT)).booleanValue();
		List<ModelObject> filteredProviders = new ArrayList<ModelObject>();
		
		for(ModelObject newProvider : newProviders) {
			// Check provider parent if appropriate
			if (useParent) {
				// Compare candidate value against permitted values to determine validity
				Object value = newProvider.getPropertyValue(propertyName); 
				if (permittedValues.contains(value)) {
					filteredProviders.add(newProvider);	
				}
			}
			// Else check child providers (any child satisfying the comparison is sufficient)
			else {
				List<?> childProviderIds = newProvider.getListPropertyValue(Constants.PROVIDER_LIST);
				for(Object childProviderId : childProviderIds) {
					ModelObject childProvider = needer.getSession().getKnowledgeBase().getModelObject(childProviderId);
					Object value = childProvider.getPropertyValue(propertyName); 
					// Compare candidate child provider value against permitted values to determine validity
					if (permittedValues.contains(value)) {
						filteredProviders.add(newProvider);	
						break;
					}					
				}
			}
		}
		
		// Return filtered provider list
		return filteredProviders;
	}
}
