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

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

/**
 * Test class for {@link adragha.rules.engine.KnowledgeBase}.
 * 
 * @author adragha
 *
 */
public class KnowledgeBaseTest {
	/** Test knowledge base loaded by setUp() method */
	private KnowledgeBase kB;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		FileInputStream in = new FileInputStream("engine.properties");
		Properties properties = new Properties();
		properties.load(in);
		in.close();
		kB = new KnowledgeBase("TestKB", properties);
	}

	/**
	 * Test method for {@link adragha.rules.engine.KnowledgeBase#getName()}.
	 */
	@Test
	public final void testGetName() {
		assertTrue("TestKB".equals(kB.getName()));
	}

	/**
	 * Test method for {@link adragha.rules.engine.KnowledgeBase#getModelObject(java.lang.Object)}.
	 * @throws EngineException 
	 */
	@Test
	public final void testGetModelObject() throws EngineException {
		assertNotNull(kB.getModelObject("universal_maximum_filter")); 
	}

	/**
	 * Test method for {@link adragha.rules.engine.KnowledgeBase#getModelObject(java.lang.Object)}.
	 * @throws EngineException 
	 */
	@Test(expected = EngineException.class)
	public final void testFailureGetModelObject() throws EngineException {
		assertNotNull(kB.getModelObject("universal_minimum_filter")); 
	}

	/**
	 * Test method for {@link adragha.rules.engine.KnowledgeBase#getModelClass(java.lang.Object)}.
	 * @throws EngineException 
	 */
	@Test
	public final void testGetModelClass() throws EngineException {
		assertNotNull(kB.getModelClass("BaseObject"));
	}

	/**
	 * Test method for {@link adragha.rules.engine.KnowledgeBase#getModelClass(java.lang.Object)}.
	 * @throws EngineException 
	 */
	@Test(expected = EngineException.class)
	public final void testFailureGetModelClass() throws EngineException {
		assertNotNull(kB.getModelClass("Baseobject"));
	}

	/**
	 * Test method for {@link adragha.rules.engine.KnowledgeBase#getModelObjects(java.util.List)}.
	 * @throws EngineException 
	 */
	@Test
	public final void testGetModelObjects() throws EngineException {
		assertTrue(kB.getModelObjects(Arrays.asList("universal_maximum_filter")).size() == 1);
	}

	/**
	 * Test method for {@link adragha.rules.engine.KnowledgeBase#getRuleClass(java.lang.Object)}.
	 */
	@Test
	public final void testGetRuleClass() {
		assertNotNull(kB.getRuleClass("adragha.rules.engine.common.PropertyValueFilter"));
	}
}
