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

import org.junit.Before;
import org.junit.Test;

import adragha.rules.engine.SessionObject.ValidationStatus;

/**
 * Test class for {@link adragha.rules.engine.Session}.
 * 
 * @author adragha
 *
 */
public class SessionTest {
	/** Test session created by setUp() method */
	private Session session;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		SessionManager manager = SessionManager.getManager();
		session = manager.createSession("TestKB");
	}

	/**
	 * Test method for {@link adragha.rules.engine.Session#getSessionId()}.
	 */
	@Test
	public final void testGetSessionId() {
		assertTrue(session.getSessionId().length() > 0);
	}

	/**
	 * Test method for {@link adragha.rules.engine.Session#createSessionObject(java.lang.String)}.
	 * @throws EngineException 
	 */
	@Test
	public final void testCreateSessionObject() throws EngineException {
		assertNotNull(session.createSessionObject("pcix_controller"));
	}

	/**
	 * Test method for {@link adragha.rules.engine.Session#getApplicableRules(adragha.rules.engine.SessionObject)}.
	 * @throws EngineException 
	 */
	@Test
	public final void testGetApplicableRules() throws EngineException {
		SessionObject parent = session.createSessionObject("pcix_controller");
		SessionObject needer = parent.getChildNeeders().get(0);
		assertTrue(session.getApplicableRules(needer).size() > 0);
	}

	/**
	 * Test method for {@link adragha.rules.engine.Session#createAndQueueSelection(adragha.rules.engine.InputSelection)}.
	 * @throws EngineException 
	 */
	@Test
	public final void testCreateAndQueueSelection() throws EngineException {
		session.createAndQueueSelection(new InputSelection("pcie_controller", 1));
		session.createAndQueueSelection(new InputSelection("pcix_controller", 1));
		assertTrue(session.getInputSelections().size() == 2);
	}

	/**
	 * Test method for {@link adragha.rules.engine.Session#validateSelections()}.
	 * @throws EngineException 
	 */
	@Test
	public final void testValidateSelections() throws EngineException {
		session.createAndQueueSelection(new InputSelection("pcie_controller", 1));
		session.validateSelections();
		SessionObject validatedSelection = session.getSessionObjects(session.getKnowledgeBase().getModelObject("pcie_controller")).get(0);
		assertTrue(validatedSelection.getValidationStatus().equals(ValidationStatus.SUCCESSFUL));
	}

	/**
	 * Test method for {@link adragha.rules.engine.Session#getSessionObjects(adragha.rules.engine.ModelObject)}.
	 * @throws EngineException 
	 */
	@Test
	public final void testGetSessionObjects() throws EngineException {
		session.createAndQueueSelection(new InputSelection("pcie_controller", 1));
		assertTrue(session.getSessionObjects(session.getKnowledgeBase().getModelObject("pcie_controller")).size() == 1);
	}

	/**
	 * Test method for {@link adragha.rules.engine.Session#getSessionObjectCount(adragha.rules.engine.ModelObject)}.
	 * @throws EngineException 
	 */
	@Test
	public final void testGetSessionObjectCount() throws EngineException {
		session.createAndQueueSelection(new InputSelection("pcie_controller", 3));
		assertTrue(session.getSessionObjects(session.getKnowledgeBase().getModelObject("pcie_controller")).size() == 3);
	}

	/**
	 * Test method for {@link adragha.rules.engine.Session#removeFromSession(adragha.rules.engine.SessionObject)}.
	 * @throws EngineException 
	 */
	@Test
	public final void testRemoveFromSession() throws EngineException {
		session.createAndQueueSelection(new InputSelection("pcie_controller", 3));
		SessionObject removeObject = session.getSessionObjects(session.getKnowledgeBase().getModelObject("pcie_controller")).get(0);
		session.removeFromSession(removeObject);
		assertTrue(session.getSessionObjects(session.getKnowledgeBase().getModelObject("pcie_controller")).size() == 2);
	}

	/**
	 * Test method for {@link adragha.rules.engine.Session#prepareForDeletion()}.
	 * @throws EngineException 
	 */
	@Test
	public final void testPrepareForDeletion() throws EngineException {
		session.createAndQueueSelection(new InputSelection("pcie_controller", 3));
		session.prepareForDeletion();
		assertTrue(session.getSessionObjects(session.getKnowledgeBase().getModelObject("pcie_controller")).size() == 0);
	}

	/**
	 * Test method for {@link adragha.rules.engine.Session#getSessionObject(java.lang.String, java.lang.String)}.
	 * @throws EngineException 
	 */
	@Test
	public final void testGetSessionObject() throws EngineException {
		session.createAndQueueSelection(new InputSelection("pcie_controller", 1));
		assertNotNull(session.getSessionObject("pcie_controller", "1001"));
	}

	/**
	 * Test method for {@link adragha.rules.engine.Session#printOutput()}.
	 * @throws EngineException 
	 */
	@Test
	public final void testPrintOutput() throws EngineException {
		session.createAndQueueSelection(new InputSelection("pcie_controller", 1));
		session.validateSelections();
		assertTrue(session.printOutput().length() > 100);
	}

	/**
	 * Test method for {@link adragha.rules.engine.Session#printOutput(boolean)}.
	 * @throws EngineException 
	 */
	@Test
	public final void testPrintOutputBoolean() throws EngineException {
		session.createAndQueueSelection(new InputSelection("pcie_controller", 1));
		session.validateSelections();
		assertTrue(session.printOutput(true).length() < session.printOutput(false).length());
	}
}
