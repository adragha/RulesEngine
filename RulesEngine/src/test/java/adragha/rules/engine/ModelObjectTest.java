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

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for {@link adragha.rules.engine.ModelObjectTest}.
 * 
 * @author adragha
 *
 */
public class ModelObjectTest {
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
		File derivedClassFile =  new File(Constants.DATA_MODEL_PATH + Constants.COMMON_MODEL_FOLDER + "\\SessionMaximumFilter.json");
				
		baseModelClass = (ModelClass) Util.getObjectFromJson(FileUtils.readFileToString(baseClassFile, Constants.UTF_8), ModelClass.class);
		baseModelClass.initialize();
		derivedModelClass = (ModelClass) Util.getObjectFromJson(FileUtils.readFileToString(derivedClassFile, Constants.UTF_8), ModelClass.class);
		derivedModelClass.initialize();
		derivedModelClass.setParentClass(baseModelClass);
	}

	/**
	 * Test method for {@link adragha.rules.engine.ModelObject#initialize(adragha.rules.engine.ModelClass)}.
	 * @throws EngineException 
	 */
	@Test
	public final void testInitialize() throws EngineException {
		ModelObject modelObject = derivedModelClass.getModelObjects().get(0);
		modelObject.initialize(derivedModelClass);
		assertTrue(modelObject.getModelClass() == derivedModelClass);
	}

	/**
	 * Test method for {@link adragha.rules.engine.ModelObject#getPropertyValue(java.lang.String)}.
	 */
	@Test
	public final void testGetPropertyValue() {
		ModelObject modelObject = derivedModelClass.getModelObjects().get(0);
		modelObject.initialize(derivedModelClass);
		assertTrue(Constants.ANY_PROTOCOL.equals(modelObject.getPropertyValue(Constants.PROTOCOL_TYPE)));
	}

	/**
	 * Test method for {@link adragha.rules.engine.ModelObject#getListPropertyValue(java.lang.String)}.
	 */
	@Test
	public final void testGetListPropertyValue() {
		ModelObject modelObject = derivedModelClass.getModelObjects().get(0);
		modelObject.initialize(derivedModelClass);
		assertTrue(Constants.BASE_OBJECT_CLASS.equals(modelObject.getListPropertyValue(Constants.APPLICABLE_CLASS_ID_LIST).get(0)));
	}

	/**
	 * Test method for {@link adragha.rules.engine.ModelObject#getObjectId()}.
	 */
	@Test
	public final void testGetObjectId() {
		ModelObject modelObject = derivedModelClass.getModelObjects().get(0);
		modelObject.initialize(derivedModelClass);
		assertTrue("universal_maximum_filter".equals(modelObject.getObjectId()));
	}

	/**
	 * Test method for {@link adragha.rules.engine.ModelObject#getModelClass()}.
	 */
	@Test
	public final void testGetModelClass() {
		ModelObject modelObject = derivedModelClass.getModelObjects().get(0);
		modelObject.initialize(derivedModelClass);
		assertTrue(modelObject.getModelClass() == derivedModelClass);
	}

	/**
	 * Test method for {@link adragha.rules.engine.ModelObject#isDescendant(adragha.rules.engine.ModelClass)}.
	 */
	@Test
	public final void testIsDescendant() {
		ModelObject modelObject = derivedModelClass.getModelObjects().get(0);
		modelObject.initialize(derivedModelClass);
		assertTrue(modelObject.isDescendant(baseModelClass));
	}
}
