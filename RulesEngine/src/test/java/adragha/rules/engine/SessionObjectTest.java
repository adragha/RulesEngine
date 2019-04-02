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

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import adragha.rules.engine.SessionObject.ValidationStatus;

/**
 * Test class for {@link adragha.rules.engine.SessionObject}.
 * 
 * @author adragha
 *
 */
public class SessionObjectTest {
	/** Test session created by setUp() method */
	private Session session;
	
	/** Test model object created by setUp() method */
	private ModelObject modelObject;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		SessionManager manager = SessionManager.getManager();
		session = manager.createSession("TestKB");
		modelObject = session.getKnowledgeBase().getModelObject("pcie_controller");
	}

	/**
	 * Test method for {@link adragha.rules.engine.SessionObject#SessionObject(adragha.rules.engine.Session, adragha.rules.engine.ModelObject, java.lang.String, adragha.rules.engine.SessionObject)}.
	 * @throws EngineException 
	 */
	@Test
	public final void testSessionObject() throws EngineException {
		assertNotNull(new SessionObject(session, modelObject, "1001", null));
	}

	/**
	 * Test method for {@link adragha.rules.engine.SessionObject#getValidationStatus()}.
	 * @throws EngineException 
	 */
	@Test
	public final void testGetValidationStatus() throws EngineException {
		SessionObject object = new SessionObject(session, modelObject, "1001", null);
		assertTrue(object.getValidationStatus().equals(ValidationStatus.UNVALIDATED));
	}

	/**
	 * Test method for {@link adragha.rules.engine.SessionObject#setPropertyValue(java.lang.String, java.lang.Object)}.
	 * @throws EngineException 
	 */
	@Test
	public final void testSetPropertyValue() throws EngineException {
		SessionObject object = new SessionObject(session, modelObject, "1001", null);
		object.setPropertyValue(Constants.PART_NUMBER, "1234");
		assertTrue("1234".equals(object.getPropertyValue(Constants.PART_NUMBER)));
	}

	/**
	 * Test method for {@link adragha.rules.engine.SessionObject#removePropertyValue(java.lang.String)}.
	 * @throws EngineException 
	 */
	@Test
	public final void testRemovePropertyValue() throws EngineException {
		SessionObject object = new SessionObject(session, modelObject, "1001", null);
		object.setPropertyValue(Constants.PART_NUMBER, "1234");
		object.removePropertyValue(Constants.PART_NUMBER);
		assertTrue("1234".equals(object.getPropertyValue(Constants.PART_NUMBER)) == false);
	}

	/**
	 * Test method for {@link adragha.rules.engine.SessionObject#setListPropertyValue(java.lang.String, java.util.List)}.
	 * @throws EngineException 
	 */
	@Test
	public final void testSetListPropertyValue() throws EngineException {
		SessionObject object = new SessionObject(session, modelObject, "1001", null);
		SessionObject childNeeder = object.getChildNeeders().get(0);
		childNeeder.setListPropertyValue(Constants.PROVIDER_CLASS_ID_LIST, Arrays.asList("Drawer","Backplane"));
		assertTrue(childNeeder.getListPropertyValue(Constants.PROVIDER_CLASS_ID_LIST).size() == 2);
	}

	/**
	 * Test method for {@link adragha.rules.engine.SessionObject#removeListPropertyValue(java.lang.String)}.
	 * @throws EngineException 
	 */
	@Test
	public final void testRemoveListPropertyValue() throws EngineException {
		SessionObject object = new SessionObject(session, modelObject, "1001", null);
		SessionObject childNeeder = object.getChildNeeders().get(0);
		childNeeder.setListPropertyValue(Constants.PROVIDER_CLASS_ID_LIST, Arrays.asList("Drawer","Backplane"));
		childNeeder.removeListPropertyValue(Constants.PROVIDER_CLASS_ID_LIST);
		assertTrue(childNeeder.getListPropertyValue(Constants.PROVIDER_CLASS_ID_LIST).size() == 1);
	}

	/**
	 * Test method for {@link adragha.rules.engine.SessionObject#prepareForRemoval()}.
	 * @throws EngineException 
	 */
	@Test
	public final void testPrepareForRemoval() throws EngineException {
		SessionObject object = new SessionObject(session, modelObject, "1001", null);
		ModelObject childNeederModelObject = object.getChildNeeders().get(0).getModelObject();
		object.prepareForRemoval();
		assertTrue(session.getSessionObjects(childNeederModelObject).size() == 0);
	}

	/**
	 * Test method for {@link adragha.rules.engine.SessionObject#getChildNeeders()}.
	 * @throws EngineException 
	 */
	@Test(expected = UnsupportedOperationException.class)
	public final void testGetChildNeeders() throws EngineException {
		SessionObject object = new SessionObject(session, modelObject, "1001", null);
		object.getChildNeeders().remove(0);
	}

	/**
	 * Test method for {@link adragha.rules.engine.SessionObject#getSession()}.
	 * @throws EngineException 
	 */
	@Test
	public final void testGetSession() throws EngineException {
		SessionObject object = new SessionObject(session, modelObject, "1001", null);
		assertNotNull(object.getSession());
	}

	/**
	 * Test method for {@link adragha.rules.engine.SessionObject#getModelObject()}.
	 * @throws EngineException 
	 */
	@Test
	public final void testGetModelObject() throws EngineException {
		SessionObject object = new SessionObject(session, modelObject, "1001", null);
		assertNotNull(object.getModelObject());
	}

	/**
	 * Test method for {@link adragha.rules.engine.SessionObject#getObjectId()}.
	 * @throws EngineException 
	 */
	@Test
	public final void testGetObjectId() throws EngineException {
		SessionObject object = new SessionObject(session, modelObject, "1001", null);
		SessionObject childNeeder = object.getChildNeeders().get(0);
		assertTrue("1001:1".equals(childNeeder.getObjectId()));
	}

	/**
	 * Test method for {@link adragha.rules.engine.SessionObject#getPropertyValue(java.lang.String)}.
	 * @throws EngineException 
	 */
	@Test
	public final void testGetPropertyValue() throws EngineException {
		SessionObject object = new SessionObject(session, modelObject, "1001", null);
		assertTrue(((String) object.getPropertyValue(Constants.PARENT_CLASS_ID)).length() > 0);
	}

	/**
	 * Test method for {@link adragha.rules.engine.SessionObject#getListPropertyValue(java.lang.String)}.
	 * @throws EngineException 
	 */
	@Test
	public final void testGetListPropertyValue() throws EngineException {
		SessionObject object = new SessionObject(session, modelObject, "1001", null);
		SessionObject childNeeder = object.getChildNeeders().get(0);
		assertTrue(childNeeder.getListPropertyValue(Constants.PROVIDER_CLASS_ID_LIST).size() > 0);
	}

	/**
	 * Test method for {@link adragha.rules.engine.SessionObject#printDetails(java.lang.String, boolean)}.
	 * @throws EngineException 
	 */
	@Test
	public final void testPrintDetails() throws EngineException {
		SessionObject object = new SessionObject(session, modelObject, "1001", null);
		assertTrue(object.printDetails("", false).length() > 0);
	}
}
