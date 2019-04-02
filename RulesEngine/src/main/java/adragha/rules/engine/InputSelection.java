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
 * Class to specify type and quantity of individual input selections
 * that collectively form the input to a session validation call
 * 
 * @author adragha
 *
 */
public final class InputSelection {
	/** ModelObject Id selected as input */
	private String selectionId = null;
	
	/** Quantity of input selection */
	private int selectionQuantity = 0;

	/**
	 * Constructor to represent an input selection
	 * @param selectionId ModelObject Id selected as input
	 * @param selectionQuantity Quantity of input selection
	 */
	public InputSelection(String selectionId, int selectionQuantity) {		
		this.selectionId = selectionId;
		this.selectionQuantity = selectionQuantity;
	}
	
	/**
	 * Method to get ModelObject Id for selection
	 * @return String ModelObject Id 
	 */
	public String getSelectionId() {
		return selectionId;
	}
	
	/**
	 * Method to get quantity selected
	 * @return Integer quantity selected
	 */
	public int getSelectionQuantity() {
		return selectionQuantity;
	}
}
