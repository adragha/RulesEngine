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

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

/**
 * Class for static utility methods
 * 
 * @author adragha
 *
 */
public final class Util {
	/** Re-usable gson object created on class load */
	private static final Gson gson = new Gson();
	
	/** Private constructor to prevent class instantiation */
	private Util() {
		throw new AssertionError();
	}

	/**
	 * Utility method to create java object from JSON data
	 * @param json String JSON content
	 * @param objectClass Class type of java object
	 * @return Object created
	 * @throws IOException
	 * @throws EngineException
	 */
	public static <T> Object getObjectFromJson(String json, Class<T> objectClass) throws IOException, EngineException {
		return gson.fromJson(json, objectClass);
	}

	/**
	 * Utility method to write Java object to a file in JSON format
	 * @param filename String name of file
	 * @param object Object to convert to JSON
	 * @throws IOException
	 */
	public static void writeObjectToJson(String filename, Object object) throws IOException {
		// Create file writer
		FileWriter outputFile = new FileWriter(filename);
		
		// Write JSON
		outputFile.write(gson.toJson(object));
		
		// Flush buffer and close writer
		outputFile.flush();		
		outputFile.close();
	}	
	
	/**
	 * Utility method to make unmodifiable a map containing lists as values  
	 * @param mapOfLists Map of List values
	 * @return Map of List values that is unmodifiable
	 */
	public static Map<String, List<?>> getUnmodifiableMapOfLists(Map<String, List<?>> mapOfLists) {
		Map<String, List<?>> unmodifiableListMap = new HashMap<String, List<?>>();
		
		if (mapOfLists != null) {
			// Iterate over map values
			for(Map.Entry<String, List<?>> mvEntry : mapOfLists.entrySet()) {
				List<?> value = mvEntry.getValue();
				
				// Mark each list value as unmodifiable
				if (value != null) {
					value = Collections.unmodifiableList(value);
				}
				
				unmodifiableListMap.put(mvEntry.getKey(), value); 
			}
		}			

		// Return an unmodifiable map
		return Collections.unmodifiableMap(unmodifiableListMap);
	}
}
