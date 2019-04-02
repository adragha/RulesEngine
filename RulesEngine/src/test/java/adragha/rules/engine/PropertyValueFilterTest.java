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

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * Test class for {@link adragha.rules.engine.common.PropertyValueFilter}.
 * 
 * @author adragha
 *
 */
public class PropertyValueFilterTest {
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
	 * Test method for {@link adragha.rules.engine.common.PropertyValueFilter#getExistingProviders(adragha.rules.engine.SessionObject, java.util.List, adragha.rules.engine.ModelObject)}.
	 * @throws EngineException 
	 */
	@Test
	public final void testGetExistingProviders() throws EngineException {
		session.createAndQueueSelection(new InputSelection("pcix_controller", 1));
		session.createAndQueueSelection(new InputSelection("pcie_backplane_2_slot", 1));
		SessionObject neederParent = session.getSessionObject("pcix_controller", "1001");
		SessionObject providerParent = session.getSessionObject("pcie_backplane_2_slot", "1002");
		SessionObject needer = neederParent.getChildNeeders().get(0);
		ModelObject ruleModelObject = session.getKnowledgeBase().getModelObject("valid_pci_type_filter");
		IRule rule = session.getApplicableRules(needer).get(ruleModelObject);
		List<SessionObject> existingProviders = new ArrayList<SessionObject>();
		existingProviders.addAll(providerParent.getChildProviders());
		assertTrue(rule.getExistingProviders(needer, existingProviders, ruleModelObject).size() == 0);
	}

	/**
	 * Test method for {@link adragha.rules.engine.common.PropertyValueFilter#getNewProviders(adragha.rules.engine.SessionObject, java.util.List, adragha.rules.engine.ModelObject)}.
	 * @throws EngineException 
	 */
	@Test
	public final void testGetNewProviders() throws EngineException {
		session.createAndQueueSelection(new InputSelection("pcix_controller", 1));
		SessionObject neederParent = session.getSessionObject("pcix_controller", "1001");
		SessionObject needer = neederParent.getChildNeeders().get(0);
		ModelObject ruleModelObject = session.getKnowledgeBase().getModelObject("valid_pci_type_filter");
		IRule rule = session.getApplicableRules(needer).get(ruleModelObject);
		List<ModelObject> newProviders = session.getKnowledgeBase().getModelClass("Backplane").getModelObjects();		
		assertTrue(rule.getNewProviders(needer, newProviders, ruleModelObject).size() == 1);
	}
}
