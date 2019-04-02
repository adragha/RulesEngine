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
 * Class for universal filtering rule that enforces model object 
 * maximums before new providers are created in the session. This 
 * rule has no impact on existing provider filtering. 
 * 
 * @author adragha
 *
 */
public class SessionMaximumFilter implements IRule {
	/**
	 * Default implementation of interface. Does not filter or sort candidates.
	 */
	@Override
	public List<SessionObject> getExistingProviders(SessionObject needer, List<SessionObject> existingProviders, ModelObject rule) throws EngineException {
		return existingProviders;
	}

	/**
	 * Override to check maximum allowed for each potential new provider parent.
	 * If the current count in the session is less than the maximum allowed the
	 * candidate is deemed valid per this rule. 
	 */
	@Override
	public List<ModelObject> getNewProviders(SessionObject needer, List<ModelObject> newProviders, ModelObject rule) throws EngineException {
		List<ModelObject> invalidProviders = new ArrayList<ModelObject>();
		
		for(ModelObject newProvider : newProviders) {
			// Get candidate maximum allowed
			int maximum = ((Double) newProvider.getPropertyValue(Constants.MAXIMUM)).intValue();
			
			// Remove candidate if maximum already reached
			if(maximum <= needer.getSession().getSessionObjectCount(newProvider)) {
				invalidProviders.add(newProvider);
			}
		}
		
		// Remove all invalid candidates
		if(invalidProviders.size() > 0) {
			newProviders.removeAll(invalidProviders);
		}
		
		// Return filtered list
		return newProviders;
	}
}
