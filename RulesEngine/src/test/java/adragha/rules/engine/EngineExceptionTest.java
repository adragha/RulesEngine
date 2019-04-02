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

import java.util.UUID;

import org.junit.Test;

/**
 * Test class for {@link adragha.rules.engine.EngineException}.
 * 
 * @author adragha
 * 
 */
public class EngineExceptionTest {

	/**
	 * Test method for {@link adragha.rules.engine.EngineException#EngineException(java.lang.String, java.lang.String)}.
	 */
	@Test
	public final void testEngineExceptionTwoArguments() {
		assertNotNull(new EngineException("Test exception", "TestKBName"));
	}

	/**
	 * Test method for {@link adragha.rules.engine.EngineException#EngineException(java.lang.String, java.lang.String, java.lang.String)}.
	 * @throws EngineException 
	 */
	@Test(expected = EngineException.class)
	public final void testEngineExceptionThreeArguments() throws EngineException {
		throw new EngineException("Test exception", "TestKBName", UUID.randomUUID().toString());
	}

	/**
	 * Test method for {@link adragha.rules.engine.EngineException#getknowledgeBaseName()}.
	 */
	@Test
	public final void testGetknowledgeBaseName() {
		EngineException e = new EngineException("Test exception", "TestKBName");
		assertTrue(e.getknowledgeBaseName().length() > 0);
	}

	/**
	 * Test method for {@link adragha.rules.engine.EngineException#getSessionId()}.
	 */
	@Test
	public final void testGetSessionId() {
		EngineException e = new EngineException("Test exception", "TestKBName", UUID.randomUUID().toString());
		assertTrue(e.getSessionId().length() > 0);
	}

}
