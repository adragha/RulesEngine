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

import org.junit.Test;

/**
 * Test class for {@link adragha.rules.engine.SessionManager}.
 * 
 * @author adragha
 *
 */
public class SessionManagerTest {

	/**
	 * Test method for {@link adragha.rules.engine.SessionManager#getManager()}.
	 * @throws IOException 
	 */
	@Test
	public final void testGetManager() throws IOException {
		assertNotNull(SessionManager.getManager());
	}

	/**
	 * Test method for {@link adragha.rules.engine.SessionManager#getManager(java.lang.String)}.
	 * @throws IOException 
	 */
	@Test
	public final void testGetManagerString() throws IOException {
		assertNotNull(SessionManager.getManager("engine.properties"));
	}

	/**
	 * Test method for {@link adragha.rules.engine.SessionManager#createSession(java.lang.String)}.
	 * @throws IOException 
	 * @throws EngineException 
	 */
	@Test
	public final void testCreateSession() throws EngineException, IOException {
		assertNotNull(SessionManager.getManager().createSession("TestKB"));
	}

	/**
	 * Test method for {@link adragha.rules.engine.SessionManager#removeSession(adragha.rules.engine.Session)}.
	 * @throws IOException 
	 * @throws EngineException 
	 */
	@Test
	public final void testRemoveSession() throws IOException, EngineException {
		SessionManager manager = SessionManager.getManager();
		Session session = manager.createSession("TestKB");
		manager.removeSession(session);		
	}

	/**
	 * Test method for {@link adragha.rules.engine.SessionManager#restoreSession(java.lang.String)}.
	 * @throws IOException 
	 * @throws EngineException 
	 */
	@Test
	public final void testRestoreSession() throws IOException, EngineException {
		SessionManager manager = SessionManager.getManager();
		assertNotNull(manager.restoreSession(manager.getProperty(Constants.SAVE_PATH) + "test01_input.json"));
	}

	/**
	 * Test method for {@link adragha.rules.engine.SessionManager#saveSession(java.lang.String, adragha.rules.engine.Session)}.
	 * @throws IOException 
	 * @throws EngineException 
	 */
	@Test
	public final void testSaveSession() throws IOException, EngineException {
		SessionManager manager = SessionManager.getManager();
		Session session = manager.restoreSession(manager.getProperty(Constants.SAVE_PATH) + "test01_input.json");
		String filename = manager.getProperty(Constants.SAVE_PATH) + "test_manager_save.json";
		manager.saveSession(filename, session);
		File savedFile = new File(filename);
		assertTrue(savedFile.exists());
		savedFile.delete();
	}

	/**
	 * Test method for {@link adragha.rules.engine.SessionManager#getProperty(java.lang.String)}.
	 * @throws IOException 
	 */
	@Test
	public final void testGetProperty() throws IOException {
		SessionManager manager = SessionManager.getManager();
		assertTrue(manager.getProperty(Constants.MODEL_PATH).length() > 0);
	}
}
