package com.appaloosastore.client;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.appaloosastore.client.AppaloosaClient;
import com.appaloosastore.client.AppaloosaDeployException;

public class AppaloosaClientIntegrationTest {

	private static final String ORGANISATION_TOKEN = "my904ssw8zas50e3ja7jmk6f43trm4gu";
	AppaloosaClient appaloosaClient;
	
	@Before
	public void setup() throws Exception{
		appaloosaClient = new AppaloosaClient(ORGANISATION_TOKEN);
		appaloosaClient.setBaseUrl("http://localhost");
		appaloosaClient.setPort(3000);
	}
	
	@Test
	@Ignore
	public void deployFile() throws AppaloosaDeployException{
		appaloosaClient.deployFile("/tmp/sample_1.ipa");
	}

}
