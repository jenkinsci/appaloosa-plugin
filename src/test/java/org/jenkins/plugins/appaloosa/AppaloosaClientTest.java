package org.jenkins.plugins.appaloosa;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class AppaloosaClientTest {

	AppaloosaClient appaloosaClient;
	
	@Before
	public void setup(){
		String organisationToken = "FAKETOKEN";
		appaloosaClient = new AppaloosaClient(organisationToken);
	}
	
	@Test
	public void postToS3Test() {
		
	}

}
