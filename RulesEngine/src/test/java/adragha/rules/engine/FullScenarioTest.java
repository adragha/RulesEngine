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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class to run tests that restore saved session selections and compare the
 * validation output to the corresponding reference output previously vetted 
 * 
 * @author adragha
 *
 */
public class FullScenarioTest {
    /** Session manager set during setUp() */
	private SessionManager manager;
	
    /** Save path property set during setUp() */
	private String savePath;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		manager = SessionManager.getManager();
		savePath = manager.getProperty(Constants.SAVE_PATH);
	}

	/**
	 * Test scenario 1
	 * 
	 * @throws IOException 
	 * @throws EngineException 
     * 
     */
	@Test
	public void testValidation01() throws IOException, EngineException {
		String testName = "test01";
		Session session = manager.restoreSession(savePath + testName + "_input.json");		
		session.validateSelections();		
		String output = session.printOutput();
		String outputReference = FileUtils.readFileToString(new File(savePath + testName + "_output.txt"), Constants.UTF_8).replaceAll("\r\n", "\n");

		assertTrue(outputReference.equals(output));		
	}

	/**
	 * Test scenario 2
	 * 
	 * @throws IOException 
	 * @throws EngineException 
     * 
     */
	@Test
	public void testValidation02() throws IOException, EngineException {
		String testName = "test02";
		Session session = manager.restoreSession(savePath + testName + "_input.json");		
		session.validateSelections();		
		String output = session.printOutput();
		String outputReference = FileUtils.readFileToString(new File(savePath + testName + "_output.txt"), Constants.UTF_8).replaceAll("\r\n", "\n");

		assertTrue(outputReference.equals(output));		
	}

	/**
	 * Test scenario 3
	 * 
	 * @throws IOException 
	 * @throws EngineException 
     * 
     */
	@Test
	public void testValidation03() throws IOException, EngineException {
		String testName = "test03";
		Session session = manager.restoreSession(savePath + testName + "_input.json");		
		session.validateSelections();		
		String output = session.printOutput();
		String outputReference = FileUtils.readFileToString(new File(savePath + testName + "_output.txt"), Constants.UTF_8).replaceAll("\r\n", "\n");

		assertTrue(outputReference.equals(output));		
	}

	/**
	 * Test scenario 4
	 * 
	 * @throws IOException 
	 * @throws EngineException 
     * 
     */
	@Test
	public void testValidation04() throws IOException, EngineException {
		String testName = "test04";
		Session session = manager.restoreSession(savePath + testName + "_input.json");		
		session.validateSelections();		
		String output = session.printOutput();
		String outputReference = FileUtils.readFileToString(new File(savePath + testName + "_output.txt"), Constants.UTF_8).replaceAll("\r\n", "\n");

		assertTrue(outputReference.equals(output));		
	}


	/**
	 * Test scenario 5
	 * 
	 * @throws IOException 
	 * @throws EngineException 
     * 
     */
	@Test
	public void testValidation05() throws IOException, EngineException {
		String testName = "test05";
		Session session = manager.restoreSession(savePath + testName + "_input.json");		
		session.validateSelections();		
		String output = session.printOutput();
		String outputReference = FileUtils.readFileToString(new File(savePath + testName + "_output.txt"), Constants.UTF_8).replaceAll("\r\n", "\n");

		assertTrue(outputReference.equals(output));		
	}
}
