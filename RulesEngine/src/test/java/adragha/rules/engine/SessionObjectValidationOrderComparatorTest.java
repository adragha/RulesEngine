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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import adragha.rules.engine.comparator.SessionObjectValidationOrderComparator;

/**
 * Test class for {@link adragha.rules.engine.comparator.SessionObjectValidationOrderComparator}.
 * 
 * @author adragha
 *
 */
public class SessionObjectValidationOrderComparatorTest {
	
	private static final List<String> selectionIds = Arrays.asList("pcie_backplane_2_slot", "pcie_controller", "pciex_slot_drawer"); 

	/**
	 * Test method for {@link adragha.rules.engine.comparator.SessionObjectValidationOrderComparator#compare(adragha.rules.engine.SessionObject, adragha.rules.engine.SessionObject)}.
	 * @throws IOException 
	 * @throws EngineException 
	 */
	@Test
	public final void testCompare() throws EngineException, IOException {
		Session session = SessionManager.getManager().createSession("TestKB");

		List<SessionObject> sortList = new ArrayList<SessionObject>();		
		for(String selectionId : selectionIds) {
			session.createAndQueueSelection(new InputSelection(selectionId, 1));
			ModelObject modelObject = session.getKnowledgeBase().getModelObject(selectionId);
			sortList.addAll(session.getSessionObjects(modelObject));
		}
		
		Collections.sort(sortList, new SessionObjectValidationOrderComparator());
		assertTrue("pciex_slot_drawer".equals(sortList.get(0).getModelObject().getObjectId()));
	}

}
