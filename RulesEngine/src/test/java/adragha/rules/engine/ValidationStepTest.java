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
 * Test class for {@link adragha.rules.engine.ValidationStep}.
 * 
 * @author adragha
 *
 */
public class ValidationStepTest {
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
	 * Test method for {@link adragha.rules.engine.ValidationStep#validate()}.
	 * @throws EngineException 
	 */
	@Test
	public final void testValidate() throws EngineException {
		session.createAndQueueSelection(new InputSelection("pcie_controller", 1));
		SessionObject parent = session.getSessionObject("pcie_controller", "1001");
		session.addParentToValidationStack(parent, null);
		SessionObject needer = parent.getChildNeeders().get(0);
		session.validateSelections();
		assertTrue(needer.getValidationStatus().equals(ValidationStatus.SUCCESSFUL));
	}

	/**
	 * Test method for {@link adragha.rules.engine.ValidationStep#cleanupFailedStep(boolean)}.
	 * @throws EngineException 
	 */
	@Test
	public final void testCleanupFailedStep() throws EngineException {
		session.createAndQueueSelection(new InputSelection("pcie_controller", 1));
		SessionObject parent = session.getSessionObject("pcie_controller", "1001");
		SessionObject needer = parent.getChildNeeders().get(0);
		ValidationStep step = new ValidationStep(session, needer, null);
		step.cleanupFailedStep(true);
		assertNull(step.getTargetObject());
	}

	/**
	 * Test method for {@link adragha.rules.engine.ValidationStep#updateObjectValue()}.
	 * @throws EngineException 
	 */
	@Test
	public final void testUpdateObjectValue() throws EngineException {
		session.createAndQueueSelection(new InputSelection("pcie_controller", 1));
		SessionObject parent = session.getSessionObject("pcie_controller", "1001");
		SessionObject needer = parent.getChildNeeders().get(0);
		ValidationStep step = new ValidationStep(session, needer, null);
		step.updateObjectValue(needer, Constants.SATISFYING_PROVIDER_ID, "1002:1");
		assertTrue(((String) needer.getPropertyValue(Constants.SATISFYING_PROVIDER_ID)).length() > 0);
	}

	/**
	 * Test method for {@link adragha.rules.engine.ValidationStep#revertChanges()}.
	 * @throws EngineException 
	 */
	@Test
	public final void testRevertChanges() throws EngineException {
		session.createAndQueueSelection(new InputSelection("pcie_controller", 1));
		SessionObject parent = session.getSessionObject("pcie_controller", "1001");
		SessionObject needer = parent.getChildNeeders().get(0);
		ValidationStep step = new ValidationStep(session, needer, null);
		step.updateObjectValue(needer, Constants.SATISFYING_PROVIDER_ID, "1002:1");
		step.revertChanges();
		assertTrue(((String) needer.getPropertyValue(Constants.SATISFYING_PROVIDER_ID)).length() == 0);
	}

	/**
	 * Test method for {@link adragha.rules.engine.ValidationStep#updateObjectValue()}.
	 * @throws EngineException 
	 */
	@Test
	public final void testUpdateObjectValueList() throws EngineException {
		session.createAndQueueSelection(new InputSelection("pcie_controller", 1));
		SessionObject parent = session.getSessionObject("pcie_controller", "1001");
		SessionObject needer = parent.getChildNeeders().get(0);
		ValidationStep step = new ValidationStep(session, needer, null);
		step.updateObjectListValue(needer, "testList", Arrays.asList("1002:1"));
		assertTrue(needer.getListPropertyValue("testList").size() > 0);
	}

	/**
	 * Test method for {@link adragha.rules.engine.ValidationStep#revertChanges()}.
	 * @throws EngineException 
	 */
	@Test
	public final void testRevertChangesList() throws EngineException {
		session.createAndQueueSelection(new InputSelection("pcie_controller", 1));
		SessionObject parent = session.getSessionObject("pcie_controller", "1001");
		SessionObject needer = parent.getChildNeeders().get(0);
		ValidationStep step = new ValidationStep(session, needer, null);
		step.updateObjectListValue(needer, "testList", Arrays.asList("1002:1"));
		step.revertChanges();
		assertTrue(needer.getListPropertyValue("testList").size() == 0);
	}
}
