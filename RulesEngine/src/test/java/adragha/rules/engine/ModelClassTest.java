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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for {@link adragha.rules.engine.ModelClass}.
 * 
 * @author adragha
 *
 */
public class ModelClassTest {
	/** Example base model class loaded by setUp() method */
	private ModelClass baseModelClass;
	/** Example derived model class loaded by setUp() method */
	private ModelClass derivedModelClass;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		File baseClassFile = new File(Constants.DATA_MODEL_PATH + Constants.BASE_MODEL_FOLDER + "\\BaseRule.json");
		File derivedClassFile =  new File(Constants.DATA_MODEL_PATH + Constants.COMMON_MODEL_FOLDER + "\\PropertyValueFilter.json");
				
		baseModelClass = (ModelClass) Util.getObjectFromJson(FileUtils.readFileToString(baseClassFile, Constants.UTF_8), ModelClass.class);
		derivedModelClass = (ModelClass) Util.getObjectFromJson(FileUtils.readFileToString(derivedClassFile, Constants.UTF_8), ModelClass.class);
	}

	/**
	 * Test method for {@link adragha.rules.engine.ModelClass#initialize(adragha.rules.engine.KnowledgeBase)}.
	 */
	@Test
	public final void testInitialize1() {
		baseModelClass.initialize();
		derivedModelClass.initialize();
		assertTrue("BaseRule".equals(baseModelClass.getClassId()) && "PropertyValueFilter".equals(derivedModelClass.getClassId()));
	}

	/**
	 * Test method for {@link adragha.rules.engine.ModelClass#initialize(adragha.rules.engine.KnowledgeBase)}.
	 */
	@Test
	public final void testInitialize2() {
		baseModelClass.initialize();
		derivedModelClass.initialize();
		derivedModelClass.setParentClass(baseModelClass);
		assertTrue("BaseRule".equals(derivedModelClass.getPropertyValue(Constants.PARENT_CLASS_ID)));
		assertTrue(derivedModelClass.getListPropertyValue(Constants.APPLICABLE_CLASS_ID_LIST).size() == 0);
	}

	/**
	 * Test method for {@link adragha.rules.engine.ModelClass#setParentClass(adragha.rules.engine.ModelClass)}.
	 */
	@Test
	public final void testSetParentClass() {
		baseModelClass.initialize();
		baseModelClass.setParentClass(null);
		derivedModelClass.initialize();		
		derivedModelClass.setParentClass(baseModelClass);
		assertTrue(!baseModelClass.isDescendant(derivedModelClass) && derivedModelClass.isDescendant(baseModelClass));
	}

	/**
	 * Test method for {@link adragha.rules.engine.ModelClass#getPropertyValue(java.lang.String)}.
	 */
	@Test
	public final void testGetPropertyValue() {
		baseModelClass.initialize();
		derivedModelClass.initialize();		
		derivedModelClass.setParentClass(baseModelClass);
		assertTrue(((String) derivedModelClass.getPropertyValue(Constants.RULE_JAVA_CLASS)).length() > 0);
	}

	/**
	 * Test method for {@link adragha.rules.engine.ModelClass#getListPropertyValue(java.lang.String)}.
	 */
	@Test
	public final void testGetListPropertyValue() {
		baseModelClass.initialize();
		derivedModelClass.initialize();		
		derivedModelClass.setParentClass(baseModelClass);
		assertTrue(((List<?>) derivedModelClass.getListPropertyValue(Constants.APPLICABLE_CLASS_ID_LIST)).size() == 0);
	}

	/**
	 * Test method for {@link adragha.rules.engine.ModelClass#isDescendant(adragha.rules.engine.ModelClass)}.
	 */
	@Test
	public final void testIsDescendant() {
		baseModelClass.initialize();
		baseModelClass.setParentClass(null);
		derivedModelClass.initialize();		
		derivedModelClass.setParentClass(baseModelClass);
		assertTrue(!baseModelClass.isDescendant(derivedModelClass) && derivedModelClass.isDescendant(baseModelClass));
	}

	/**
	 * Test method for {@link adragha.rules.engine.ModelClass#getModelObjects()}.
	 */
	@Test(expected = UnsupportedOperationException.class)
	public final void testGetModelObjects() {
		baseModelClass.initialize();
		baseModelClass.getModelObjects().add(new ModelObject());
	}

	/**
	 * Test method for {@link adragha.rules.engine.ModelClass#computeRecursiveDescendantData(java.util.List)}.
	 */
	@Test
	public final void testComputeRecursiveDescendantData() {
		List<ModelObject> result = new ArrayList<ModelObject>();
		baseModelClass.initialize();
		baseModelClass.setParentClass(null);
		derivedModelClass.initialize();		
		derivedModelClass.setParentClass(baseModelClass);
		baseModelClass.computeRecursiveDescendantData(result);
		assertTrue(result.size() == 0);
	}

	/**
	 * Test method for {@link adragha.rules.engine.ModelClass#computeRecursiveDescendantClasses(java.util.List)}.
	 */
	@Test
	public final void testComputeRecursiveDescendantClasses() {
		List<ModelClass> result = new ArrayList<ModelClass>();
		baseModelClass.initialize();
		baseModelClass.setParentClass(null);
		derivedModelClass.initialize();		
		derivedModelClass.setParentClass(baseModelClass);
		baseModelClass.computeRecursiveDescendantClasses(result);
		assertTrue(result.size() == 2);
	}

	/**
	 * Test method for {@link adragha.rules.engine.ModelClass#printData()}.
	 */
	@Test
	public final void testPrintData() {
		derivedModelClass.initialize();
		derivedModelClass.printData();
	}
}
