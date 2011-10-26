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
