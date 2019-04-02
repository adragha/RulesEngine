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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import adragha.rules.engine.comparator.ModelObjectValidationOrderComparator;

/**
 * Class to represent model data in memory. The stored data is in JSON format.
 * Model data consists of ModelClass and ModelObject instances.
 *   
 * @author adragha
 *
 */
public final class KnowledgeBase {
	/** Logger for model knowledge base instance logging */
	static final Logger logger = LoggerFactory.getLogger(KnowledgeBase.class);
	
	/** Name of model knowledge base */
	private String name = null;
	
	/** Map of model classes by ID */
	private Map<String, ModelClass> modelClasses = new HashMap<String, ModelClass>();
	
	/** Map of model objects by ID */
	private Map<String, ModelObject> modelObjects = new HashMap<String, ModelObject>();
	
	/** Map of java classes for rules by qualified java class name */
	private Map<String, IRule> modelRuleClasses = new HashMap<String, IRule>();
	
	/** Re-usable comparator for default order of ModelObjects by object ID  */
	private final ModelObjectValidationOrderComparator modelObjectValidationOrderComparator = new ModelObjectValidationOrderComparator();
	
	/**
	 * Constructor to create model knowledge base
	 * @param name String name of model knowledge base
	 * @param properties Program properties to determine data load paths
	 * @throws EngineException
	 */
	KnowledgeBase(String name, Properties properties) throws EngineException {
		// Set name
		this.name = name;
		
		// Load base model and common model from Engine JAR if specified
		String engineJarPath = properties.getProperty(Constants.ENGINE_JAR_PATH, "");
		if(engineJarPath.length() > 0) {			
			loadModelFromJar(engineJarPath, Constants.BASE_MODEL_FOLDER);
			loadModelFromJar(engineJarPath, Constants.COMMON_MODEL_FOLDER);			
		}
		// ...or from RulesEngine project files if running tests on this project
		else {
			loadModel(new File(Constants.DATA_MODEL_PATH + Constants.BASE_MODEL_FOLDER));
			loadModel(new File(Constants.DATA_MODEL_PATH + Constants.COMMON_MODEL_FOLDER));
		}
		
		// Load selected knowledge base
		// NOTE: Will need re-factoring similar to above code block for models packaged in a JAR  
		loadModel(new File(properties.getProperty(Constants.MODEL_PATH) + File.separator + name));
					
		// Post-process model classes to setup class hierarchy references between ModelClass instances
		setupClassHierarchy();
		
		// Cache knowledge base rules
		cacheRules();
	}

	/**
	 * Helper method to load a model given from its folder.
	 * @param baseFolder File object handle for model folder
	 * @throws EngineException
	 */
	private void loadModel(File baseFolder) throws EngineException {
		// Get all files in model folder (
		File[] allFiles = baseFolder.listFiles();
		
		if (allFiles != null) {
			// Iterate over all files
			for(int i=0; i < allFiles.length; i++) {
				String filename = allFiles[i].getName();
				// Load each JSON file as a separate ModelClass instance
				if (filename.endsWith(Constants.JSON_EXT)) {
					try {
						loadModelClass(FileUtils.readFileToString(allFiles[i], Constants.UTF_8));
					} 
					catch (IOException e) {
						logger.error("Unable to load JSON file " +  baseFolder.getName() + "\\" + filename +  " during model knowledge base load.", e);
					}
				}
				else {
					logger.warn("Ignoring non-JSON file {} during model knowledge base load.", baseFolder.getName() + "\\" + filename);
				}
			}
		}	
	}
	
	/**
	 * Helper method to load the base and common models from the packaged engine JAR
	 * @param engineJarPath String path to engine JAR file
	 * @param baseFolderName String name of base/common model folder
	 * @throws EngineException
	 */
	private void loadModelFromJar(String engineJarPath, String baseFolderName) throws EngineException {
		try {
			// Load jar file
			JarFile jar = new JarFile(engineJarPath);
			Enumeration<JarEntry> entries = jar.entries();
			
			// Iterate over contents
			while(entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				// If entry is a JSON file, load its contents into a ModelClass instance
				if (!entry.isDirectory() 
					&& 
					entry.getName().startsWith(baseFolderName) 
					&& 
					entry.getName().endsWith(Constants.JSON_EXT)
				   ) 
				{
					InputStream input = jar.getInputStream(entry);
					String dataString = IOUtils.toString(input, Constants.UTF_8); 
					loadModelClass(dataString);
				    input.close();				    
				}
			}
			
			jar.close();
		}
		catch(IOException e) {
			throw new EngineException("Exception loading " + baseFolderName + " JSON data from engine JAR", getName());
		}
	}

	/**
	 * Helper method to load a model class from the class JSON data
	 * @param fileJson String JSON data for class
	 * @throws EngineException
	 */
	private void loadModelClass(String fileJson) throws EngineException {
		try {
			// Create ModelClass from JSON (also loads corresponding ModelObject instances) 
			ModelClass modelClass = (ModelClass) Util.getObjectFromJson(fileJson, ModelClass.class);
			// Setup and initialize
			modelClass.setKnowledgeBase(this);
			modelClass.initialize();
			// Add to model knowledge base map
			modelClasses.put(modelClass.getClassId(), modelClass);
			
			// Iterate over the class model objects 
			for(ModelObject modelObject : modelClass.getModelObjects()) {
				// Initialize model objects and add to model knowledge base map
				modelObject.initialize(modelClass);
				modelObjects.put(modelObject.getObjectId(), modelObject);
			}
		}
		catch(Exception e) {
			throw new EngineException(e.getMessage(), getName());
		}
	}

	/**
	 * Helper method to setup class hierarchy references between ModelClass
	 * instances during a second pass after loading all the JSON data first
	 */
	private void setupClassHierarchy() {
		for(ModelClass modelClass : modelClasses.values()) {
			modelClass.setParentClass(modelClasses.get(modelClass.getPropertyValue(Constants.PARENT_CLASS_ID)));
		}
	}
	
	/**
	 * Helper method to create a re-usable java object for each rule class
	 * and cache potentially applicable rule data on model objects
	 * @throws EngineException
	 */
	private void cacheRules() throws EngineException {
		// Get all ModelClass instances derived from the BaseRule model class
		ModelClass baseRuleClass = getModelClass(Constants.BASE_RULE_CLASS);		
		List<ModelObject> rules = new ArrayList<ModelObject>();			
		baseRuleClass.computeRecursiveDescendantData(rules);

		// Create re-usable java rule objects for each rule class
		List<ModelClass> ruleClasses = new ArrayList<ModelClass>(); 
		baseRuleClass.computeRecursiveDescendantClasses(ruleClasses);				
		ruleClasses.remove(baseRuleClass); // Remove abstract base class itself
		
		for(ModelClass ruleClass : ruleClasses) {
			String ruleClassName = (String) ruleClass.getPropertyValue(Constants.RULE_JAVA_CLASS);
			try {
				IRule dbRule = (IRule) Class.forName(ruleClassName).newInstance();
				
				if(!modelRuleClasses.containsKey(ruleClassName)) {
					modelRuleClasses.put(ruleClassName, dbRule);
				}
			}
			catch(Exception e) {
				throw new EngineException("Invalid qualified java class for model rule with class ID: " + ruleClass.getClassId(), getName());				
			}
		}
		
		// Create basic applicability cache	on model objects	
		for(ModelObject rule : rules) {			
			List<ModelObject> matchingDataObjects = new ArrayList<ModelObject>();

			// Get model objects for all applicable classes
			for(Object classId : rule.getListPropertyValue(Constants.APPLICABLE_CLASS_ID_LIST)) {
				ModelClass applicableClass = modelClasses.get(classId);
				applicableClass.computeRecursiveDescendantData(matchingDataObjects);
			}

			// Add all directly applicable model objects
			for(Object dataId : rule.getListPropertyValue(Constants.APPLICABLE_OBJECT_ID_LIST)) {
				matchingDataObjects.add(modelObjects.get(dataId));
			}

			// Remove all directly inapplicable model objects
			for(Object dataId : rule.getListPropertyValue(Constants.INAPPLICABLE_OBJECT_ID_LIST)) {
				matchingDataObjects.remove(modelObjects.get(dataId));
			}
			
			// Create unique model object set and cache rule on each one 
			Set<ModelObject> uniqueDataObjects = new HashSet<ModelObject>(matchingDataObjects);
			for(ModelObject modelObject : uniqueDataObjects) {
				modelObject.addApplicableRule(rule);
			}
		}
		
		// Make rule collections cached on model objects unmodifiable
		for(ModelObject modelObject : modelObjects.values()) {
			modelObject.makeApplicableRulesUnmodifiable(getModelObjectValidationOrderComparator());
		}
	}

	/**
	 * Method to get re-suable model object validation order comparator
	 * @return ModelObjectValidationOrderComparator object
	 */
	ModelObjectValidationOrderComparator getModelObjectValidationOrderComparator() {
		return modelObjectValidationOrderComparator;
	}
	
	/**
	 * Method to get name of knowledge base
	 * @return String name of knowledge base
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Method to get model object given the model object's ID 
	 * @param modelObjectId ID of model object (uncast String expected)
	 * @return ModelObject object if found
	 * @throws EngineException
	 */
	public ModelObject getModelObject(Object modelObjectId) throws EngineException {
		if (modelObjects.containsKey(modelObjectId)) {
			return modelObjects.get(modelObjectId);
		}
		else {
			throw new EngineException("No definition for model object with ID: " + modelObjectId, getName());
		}
	}
	
	/**
	 * Method to get model class given the model class' ID
	 * @param modelClassId ID of model class (uncast String expected)
	 * @return ModelClass object if found
	 * @throws EngineException
	 */
	public ModelClass getModelClass(Object modelClassId) throws EngineException {		
		if (modelClasses.containsKey(modelClassId)) {
			return modelClasses.get(modelClassId);
		}
		else {
			throw new EngineException("No definition for model class with ID: " + modelClassId, getName());
		}
	}
	
	/**
	 * Method to get modifiable list of model objects from their corresponding object IDs
	 * @param objectIds List (of uncast String values) of IDs of model objects 
	 * @return List of ModelObject
	 * @throws EngineException
	 */
	public List<ModelObject> getModelObjects(List<?> objectIds) throws EngineException {
		List<ModelObject> results = new ArrayList<ModelObject>();
		
		for(Object objectId : objectIds) {
			results.add(getModelObject(objectId));
		}
		
		return results;
	}

	/**
	 * Method to get Java object representing model class for a rule 
	 * @param qualifiedRuleClassName String value of fully qualified java class name
	 * @return IRule object
	 */
	public IRule getRuleClass(Object qualifiedRuleClassName) {
		return modelRuleClasses.get(qualifiedRuleClassName);
	}
	
	/**
	 * Debug method to print knowledge base contents
	 */
	public void printData() {
		for(ModelClass cls : modelClasses.values()) {
			cls.printData();
			for(ModelObject object : cls.getModelObjects()) {
				object.printData();
			}
		}
	}
}
