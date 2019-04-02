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
package adragha.rules.engine.comparator;

import java.util.Comparator;

import adragha.rules.engine.Constants;
import adragha.rules.engine.SessionObject;

/**
 * Comparator for session objects based on validation order value.
 * Lower number is validated first.
 * 
 * @author adragha
 *
 */
public class SessionObjectValidationOrderComparator implements Comparator<SessionObject> {
	/**
	 * Comparator for session objects based on validation order value.
	 * Lower number is validated first.
	 */
	@Override
	public int compare(SessionObject o1, SessionObject o2) {
		return ((Double) o1.getPropertyValue(Constants.VALIDATION_ORDER)).intValue() 
				- ((Double) o2.getPropertyValue(Constants.VALIDATION_ORDER)).intValue();
	}		
}