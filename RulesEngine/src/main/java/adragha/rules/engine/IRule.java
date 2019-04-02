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

import java.util.List;

/**
 * Interface that must be implemented by all rule classes.
 * A rule is used to determine the valid options to satisfy
 * a needer object
 * 
 * @author adragha
 *
 */
public interface IRule {
	/**
	 * Method to get list of filtered, sorted and valid providers that already exist in the session
	 * 
	 * @param needer Child needer session object that must be satisfied
	 * @param existingCandidateProviders List of existing child provider session objects that are candidates
	 * @param rule Model object that represents rule information 
	 * @return List of valid existing session providers in order of preference
	 * @throws EngineException
	 */
	public List<SessionObject> getExistingProviders(SessionObject needer, List<SessionObject> existingCandidateProviders, ModelObject rule) throws EngineException;
	
	/**
	 * Method to get list of filtered, sorted and valid provider parents that can be created in the session
	 * 
	 * @param needer Child needer session object that must be satisfied
	 * @param newCandidateProviders List of provider parent model objects that are candidates for creation
	 * @param rule Model object that represents rule information
	 * @return List of valid new provider parents in order of preference
	 * @throws EngineException
	 */
	public List<ModelObject> getNewProviders(SessionObject needer, List<ModelObject> newCandidateProviders, ModelObject rule) throws EngineException;
}
