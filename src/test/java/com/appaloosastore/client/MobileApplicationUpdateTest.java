package com.appaloosastore.client;

import static org.junit.Assert.*;

import org.junit.Test;

import com.appaloosastore.client.MobileApplicationUpdate;

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

}
