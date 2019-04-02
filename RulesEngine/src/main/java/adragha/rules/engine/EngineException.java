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

/**
 * Base class for a rules engine exception
 * 
 * @author adragha
 *
 */
@SuppressWarnings("serial")
public class EngineException extends Exception {	
	/** Name of knowledge base from which exception originates */
	private String knowledgeBaseName;
	
	/** ID of session from which exception originates */
	private String sessionId;
	
	/**
	 * Constructor for engine exception
	 * @param message String text for exception
	 * @param knowledgeBaseName String name of knowledge base from which exception originates
	 */
	public EngineException(String message, String knowledgeBaseName) {
		this(message, knowledgeBaseName, "NULL_SESSION");
	}

	/**
	 * Constructor for engine exception
	 * @param message String text for exception
	 * @param knowledgeBaseName String name of knowledge base from which exception originates
	 * @param sessionId String ID of session from which exception originates
	 */
	public EngineException(String message, String knowledgeBaseName, String sessionId) {
		super(message);
		this.knowledgeBaseName = knowledgeBaseName;
		this.sessionId = sessionId;		
	}

	/**
	 * Method to get knowledge base name for exception
	 * @return String knowledge base name
	 */
	public String getknowledgeBaseName() {
		return knowledgeBaseName;
	}

	/**
	 * Method to get session ID for exception
	 * @return String session ID
	 */
	public String getSessionId() {
		return sessionId;
	}
}
