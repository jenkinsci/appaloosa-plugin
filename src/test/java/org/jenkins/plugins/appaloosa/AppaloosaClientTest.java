package org.jenkins.plugins.appaloosa;

import static com.harlap.test.http.MockHttpServer.Method.GET;
import static com.harlap.test.http.MockHttpServer.Method.POST;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.harlap.test.http.MockHttpServer;
import com.harlap.test.http.MockHttpServer.Method;

public class AppaloosaClientTest {

	private static final int PORT = 45678;
	private static final String BASE_URL = "http://localhost";
	private static String ORGANISATION_TOKEN = "FAKETOKEN";
	AppaloosaClient appaloosaClient;
	
	MockHttpServer server;
	
	private String sampleBinaryFormResponse = "{\"policy\":\"eyJleH=\",\"success_action_status\":200,\"content_type\":\"\",\"signature\":\"LL/ZXXNCl+0NtI8=\",\"url\":" +
			"\""+BASE_URL+":"+PORT+"/\",\"access_key\":\"eERTYU\",\"key\":\"5/uploads/${filename}\",\"acl\":\"private\"}";
	
	@Before
	public void setup() throws Exception{
		appaloosaClient = new AppaloosaClient(ORGANISATION_TOKEN);
		appaloosaClient.setBaseUrl(BASE_URL);
		appaloosaClient.setPort(PORT);
		
		server = new MockHttpServer(PORT);
		server.start();
	}
	
	@After
	public void tearDown() throws Exception{
		server.stop();
	}
	
	@Test
	public void deployFileShouldAskAppaloosaForS3Form() throws AppaloosaDeployException{
		String url = "/api/upload_binary_form?token=" + ORGANISATION_TOKEN;
		server.expect(GET, url).respondWith(200, null, sampleBinaryFormResponse);
		server.expect(POST, "/").respondWith(200, null, "");
		
		appaloosaClient.deployFile(getTestFile("fake.ipa"));
		
		server.verify();
	}

	@Test
	public void deployFileShouldUploadFileWithReturnedParams() throws AppaloosaDeployException{
		String url = "/api/upload_binary_form?token=" + ORGANISATION_TOKEN;
		server.expect(GET, url).respondWith(200, null, sampleBinaryFormResponse);
		server.expect(POST, "/").respondWith(200, null, "");

		appaloosaClient.deployFile(getTestFile("fake.ipa"));
		
		server.verify();
	}

	
	@Test
	public void getUploadFormShouldCallAppaloosaAndReturnsObject() throws AppaloosaDeployException{
		String url = "/api/upload_binary_form?token=" + ORGANISATION_TOKEN;
		server.expect(GET, url).respondWith(200, null, sampleBinaryFormResponse);
		
		AppaloosaUploadBinaryForm uploadForm = appaloosaClient.getUploadForm();
		
		server.verify();
		Assert.assertEquals("eyJleH=", uploadForm.getPolicy());
		Assert.assertEquals(200, uploadForm.getSuccessActionStatus());
		Assert.assertEquals("", uploadForm.getContentType());
		Assert.assertEquals("LL/ZXXNCl+0NtI8=", uploadForm.getSignature());
		Assert.assertEquals(BASE_URL+":"+PORT + "/", uploadForm.getUrl());
		Assert.assertEquals("eERTYU", uploadForm.getAccessKey());
		Assert.assertEquals("5/uploads/${filename}", uploadForm.getKey());
		Assert.assertEquals("private", uploadForm.getAcl());
	}

	
	String getTestFile(String filename) {
		return filename;
	}

}
