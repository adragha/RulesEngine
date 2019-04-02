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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class for constants used in the project
 * 
 * @author adragha
 *
 */
public final class Constants {
	/** JSON file extension */
	public static final String JSON_EXT = ".json";
	/** UTF-8 file encoding */ 
	public static final String UTF_8 = "UTF-8";
	
	/** Relative path to core data model */
	public static final String DATA_MODEL_PATH = "src\\main\\resources\\";
	/** Base data model folder name */
	public static final String BASE_MODEL_FOLDER = "Base";	
	/** Common data model folder name for utility model data */
	public static final String COMMON_MODEL_FOLDER = "Common";
	
	/** Environment property name for model path */
	public static final String MODEL_PATH = "MODEL_PATH";
	/** Environment property name for session save path */
	public static final String SAVE_PATH = "SAVE_PATH";
	/** Environment property name for path to core engine java archive */
	public static final String ENGINE_JAR_PATH = "ENGINE_JAR_PATH";
	
	/** Empty string */
	public static final String EMPTY = "";
	/** End of line */
	public static final String EOL = "\n";
	/** String value of 'true' */
	public static final String TRUE = "true";
	/** String value of 'false' */ 
	public static final String FALSE= "false";

	/** Unmodifiable empty value list */
	public static final List<?> EMPTY_VALUE_LIST = Collections.unmodifiableList(new ArrayList<Object>(0));	
	/** Unmodifiable empty value map */
	public static final Map<String, Object> EMPTY_VALUE_MAP = Collections.unmodifiableMap(new HashMap<String, Object>());	
	/** Unmodifiable empty value list map */
	public static final Map<String, List<?>> EMPTY_VALUE_LIST_MAP = Collections.unmodifiableMap(new HashMap<String, List<?>>());
	/** Unmodifiable empty model object list */
	public static final List<ModelObject> EMPTY_OBJECT_LIST = Collections.unmodifiableList(new ArrayList<ModelObject>(0));

	/** Base ModelClass from which all top-level model objects are derived */ 
	public static final String BASE_OBJECT_CLASS = "BaseObject";
	/** Base ModelClass from which all child needer model objects are derived */
	public static final String BASE_NEEDER_CLASS = "BaseNeeder";
	/** Base ModelClass from which all child provider model objects are derived */
	public static final String BASE_PROVIDER_CLASS = "BaseProvider";
	/** Base ModelClass from which all rule model objects are derived */
	public static final String BASE_RULE_CLASS = "BaseRule";
	
	/** List of base model classes */
	public static final List<String> MODEL_ROOT_CLASSES = Arrays.asList(
																	BASE_OBJECT_CLASS,
																	BASE_NEEDER_CLASS,
																	BASE_PROVIDER_CLASS,
																	BASE_RULE_CLASS
																	);	
	
	/** Model property for model class ID */
	public static final String CLASS_ID = "classId";
	/** Model property for model parent class ID */
	public static final String PARENT_CLASS_ID = "parentClassId";
	/** Model property for object ID */
	public static final String OBJECT_ID = "objectId";
	/** Model property for description */
	public static final String DESCRIPTION = "description";
	/** Model property for part number */
	public static final String PART_NUMBER = "partNumber";	
	/** Model property for maximum quantity allowed */
	public static final String MAXIMUM = "maximum";
	/** Model property to specify validation order. Lower number is validated first. */
	public static final String VALIDATION_ORDER = "validationOrder";
	/** Model property for session object validation status */
	public static final String VALIDATION_STATUS = "validationStatus";	
	/** Model property for child provider model object IDs */
	public static final String PROVIDER_LIST = "providerObjectIds";
	/** Model property for child needer model object IDs */
	public static final String NEEDER_LIST = "neederObjectIds";
	/** Model property for protocol type */
	public static final String PROTOCOL_TYPE = "protocolType";
	/** Model property for quantity provided by child provider */
	public static final String QTY_PROVIDED = "quantityProvided";
	/** Model property for quantity needed by child needer */
	public static final String QTY_NEEDED = "quantityNeeded";
	/** Model property for model class IDs to which rule is applicable */
	public static final String APPLICABLE_CLASS_ID_LIST = "applicableClassIds";
	/** Model property for model object IDs to which rule is applicable */
	public static final String APPLICABLE_OBJECT_ID_LIST = "applicableObjectIds";
	/** Model property for model object IDs to which rule is not applicable */
	public static final String INAPPLICABLE_OBJECT_ID_LIST = "inapplicableObjectIds";
	/** Model property for model class IDs of parents of compatible providers */
	public static final String PROVIDER_CLASS_ID_LIST = "providerParentClassIds";
	/** Model property on model needers to specify if they can be satisfied by newly created providers. Default is true. */
	public static final String TRY_NEW_PROVIDERS = "tryNewProviders";
	/** Model property for qualified Java class name for rule class */
	public static final String RULE_JAVA_CLASS = "qualifiedJavaClass";
	/** Session object property for satisfying provider session ID */
	public static final String SATISFYING_PROVIDER_ID = "satisfyingProviderId";	
	/** Session object property for satisfying provider model object ID */
	public static final String SATISFYING_PROVIDER_MODEL_ID = "satisfyingProviderModelId";
	
	/** Model property value for use in a condition that applies regardless of protocol type */
	public static final String ANY_PROTOCOL = "AnyProtocol";
	
	/** Private constructor to prevent class instantiation */
	private Constants() {
		throw new AssertionError();
	}
}
