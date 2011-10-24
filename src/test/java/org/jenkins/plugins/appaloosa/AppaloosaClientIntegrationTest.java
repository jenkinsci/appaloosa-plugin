package org.jenkins.plugins.appaloosa;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class AppaloosaClientIntegrationTest {

	private static final String ORGANISATION_TOKEN = "qv2o2c8wju9zbr0t9nh7pgdgu8sqy3zz";
	AppaloosaClient appaloosaClient;
	
	@Before
	public void setup() throws Exception{
		appaloosaClient = new AppaloosaClient(ORGANISATION_TOKEN);
		appaloosaClient.setBaseUrl("http://ool-appaloosa-int.heroku.com/");
	}
	
	@Test
	@Ignore
	public void deployFile() throws AppaloosaDeployException{
		appaloosaClient.deployFile("/tmp/GoogleIO.apk");
	}

}
