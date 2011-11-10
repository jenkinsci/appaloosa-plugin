/*
 * The MIT License
 *
 * Copyright (c) 2011 eXo platform
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.appaloosastore.client;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MobileApplicationUpdateTest {

	@Test
	public void isProcessedShouldReturnFalseWhenNoStatusAndNoApplicationId() {
		MobileApplicationUpdate update = new MobileApplicationUpdate();
		assertFalse(update.isProcessed());
	}

	@Test
	public void isProcessedShouldReturnTrueWhenStatusGreaterThan4() {
		MobileApplicationUpdate update = new MobileApplicationUpdate();
		update.status = 5;
		assertTrue(update.isProcessed());
	}

	@Test
	public void isProcessedShouldReturnFalseWhenNoStatusButApplicationId() {
		MobileApplicationUpdate update = new MobileApplicationUpdate();
		update.applicationId = "something";
		assertTrue(update.isProcessed());
	}

	@Test
	public void testHasError(){
		MobileApplicationUpdate update = new MobileApplicationUpdate();
		update.status = 1;
		assertFalse(update.hasError());
		update.status = 4;
		assertFalse(update.hasError());
		update.status = 5;
		assertTrue(update.hasError());
		update.status = 6;
		assertTrue(update.hasError());
		update.status = 19;
		assertTrue(update.hasError());
	}

}
