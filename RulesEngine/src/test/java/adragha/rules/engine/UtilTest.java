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

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

/**
 * Test class for {@link adragha.rules.engine.Util}.
 * 
 * @author adragha
 *
 */
public class UtilTest {

	/**
	 * Test method for {@link adragha.rules.engine.Util#getObjectFromJson(java.lang.String, java.lang.Class)}.
	 * @throws EngineException 
	 * @throws IOException 
	 */
	@Test
	public final void testGetObjectFromJson() throws IOException, EngineException {
		File file = new File(Constants.DATA_MODEL_PATH + Constants.BASE_MODEL_FOLDER + "\\BaseRule.json");
		assertNotNull(Util.getObjectFromJson(FileUtils.readFileToString(file, Constants.UTF_8), ModelClass.class));
	}

	/**
	 * Test method for {@link adragha.rules.engine.Util#writeObjectToJson(java.lang.String, java.lang.Object)}.
	 * @throws EngineException 
	 * @throws IOException 
	 */
	@Test
	public final void testWriteObjectToJson() throws IOException, EngineException {
		File file = new File(Constants.DATA_MODEL_PATH + Constants.BASE_MODEL_FOLDER + "\\BaseRule.json");
		Object object = Util.getObjectFromJson(FileUtils.readFileToString(file, Constants.UTF_8), ModelClass.class);
		String filename = "src\\test\\saved\\test_util_write.json";
		Util.writeObjectToJson(filename, object);

		File savedFile = new File(filename);
		assertTrue(savedFile.exists());
		savedFile.delete();
	}

	/**
	 * Test method for {@link adragha.rules.engine.Util#getUnmodifiableMapOfLists(java.util.Map)}.
	 */
	@Test(expected = UnsupportedOperationException.class)
	public final void testGetUnmodifiableMapOfLists() {
		Map<String, List<?>> map = new HashMap<String, List<?>>();
		map.put("test_key1", Arrays.asList("test_value1", "test_value2"));		
		Map<String, List<?>> unmodifiableMap = Util.getUnmodifiableMapOfLists(map);
		unmodifiableMap.put("test_key2", Arrays.asList("test_value1", "test_value2"));
	}
}
