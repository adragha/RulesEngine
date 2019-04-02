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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import adragha.rules.engine.comparator.ModelObjectIdComparator;

/**
 * Test class for {@link adragha.rules.engine.comparator.ModelObjectIdComparator}.
 * 
 * @author adragha
 *
 */
public class ModelIdObjectComparatorTest {

	/**
	 * Test method for {@link adragha.rules.engine.comparator.ModelObjectIdComparator#compare(adragha.rules.engine.ModelObject, adragha.rules.engine.ModelObject)}.
	 * @throws EngineException 
	 * @throws IOException 
	 */
	@Test
	public final void testCompare() throws IOException, EngineException {
		File classFile = new File("src\\test\\data\\TestKB\\Controller.json");
		ModelClass modelClass = (ModelClass) Util.getObjectFromJson(FileUtils.readFileToString(classFile, Constants.UTF_8), ModelClass.class);
		
		List<ModelObject> sortList = new ArrayList<ModelObject>();
		for(ModelObject object : modelClass.getModelObjects()) {
			object.initialize(modelClass);
			sortList.add(object);
		}
		
		Collections.sort(sortList, new ModelObjectIdComparator());
		assertTrue("pcie_controller".equals(sortList.get(0).getObjectId()));
	}

}
