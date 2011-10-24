package org.jenkins.plugins.appaloosa;

import static com.harlap.test.http.MockHttpServer.Method.GET;
import static com.harlap.test.http.MockHttpServer.Method.POST;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.harlap.test.http.MockHttpServer;

public class AppaloosaClientTest {

	private static final int PORT = 45678;
	private static final String BASE_URL = "http://localhost";
	private static String ORGANISATION_TOKEN = "FAKETOKEN";
	AppaloosaClient appaloosaClient;
	
	MockHttpServer server;
	
	private String sampleBinaryFormResponse = "{\"policy\":\"eyJleH=\",\"success_action_status\":200,\"content_type\":\"\",\"signature\":\"LL/ZXXNCl+0NtI8=\",\"url\":" +
			"\""+BASE_URL+":"+PORT+"/\",\"access_key\":\"eERTYU\",\"key\":\"5/uploads/${filename}\",\"acl\":\"private\"}";
	
	private String sampleOnBinaryUploadResponse = "{\"id\":590,\"activation_date\":null, \"other\":\"test\"}";
	
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
	public void deployFileIntegrationTest() throws AppaloosaDeployException{
		server.expect(GET, "/api/upload_binary_form.json?token=" + ORGANISATION_TOKEN)
			  .respondWith(200, null, sampleBinaryFormResponse);
		
		server.expect(POST, "/")
			  .respondWith(200, null, "");
		
		server.expect(POST, "/api/on_binary_upload")
			  .respondWith(200, null, sampleOnBinaryUploadResponse);
		
		server.expect(GET, "/mobile_application_updates/590.json?token="+ORGANISATION_TOKEN)
			  .respondWith(200, null, "{\"id\":590, \"status\":1,\"application_id\":\"com.appaloosa.sampleapp\"}");

		server.expect(POST, "/api/publish_update.json")
		  .respondWith(200, null, "{\"id\":590, \"status\":4,\"application_id\":\"com.appaloosa.sampleapp\"}");
		
		appaloosaClient.deployFile(getTestFile("fake.ipa"));
		
		server.verify();
	}

	
	@Test
	public void getUploadFormShouldCallAppaloosaAndReturnsObject() throws AppaloosaDeployException{
		String url = "/api/upload_binary_form.json?token=" + ORGANISATION_TOKEN;
		server.expect(GET, url).respondWith(200, null, sampleBinaryFormResponse);
		
		AppaloosaUploadBinaryForm uploadForm = appaloosaClient.getUploadForm();
		
		server.verify();
		assertEquals("eyJleH=", uploadForm.getPolicy());
		assertEquals(200, uploadForm.getSuccessActionStatus());
		assertEquals("", uploadForm.getContentType());
		assertEquals("LL/ZXXNCl+0NtI8=", uploadForm.getSignature());
		assertEquals(BASE_URL+":"+PORT + "/", uploadForm.getUrl());
		assertEquals("eERTYU", uploadForm.getAccessKey());
		assertEquals("5/uploads/${filename}", uploadForm.getKey());
		assertEquals("private", uploadForm.getAcl());
	}
	
	@Test
	public void notifyAppaloosaForFileShouldCallAppaloosaServer() throws AppaloosaDeployException{
		AppaloosaUploadBinaryForm uploadForm = new AppaloosaUploadBinaryForm();
		uploadForm.setKey("54/uploads/{filename}");
		
		String url = "/api/on_binary_upload";
		server.expect(POST, url).respondWith(200, null, "{\"id\":590,\"activation_date\":null, \"other\":\"test\"}");
		
		MobileApplicationUpdate update = appaloosaClient.notifyAppaloosaForFile(getTestFile("fake.ipa"), uploadForm);
		
		assertEquals(590, update.id);
		
		server.verify();
	} 
	
	@Test 
	public void constructKeyTest(){
		 assertEquals("54/uploads/test.ipa", appaloosaClient.constructKey("54/uploads/${filename}", "/tmp/test.ipa"));
		 assertEquals("/54/uploads/test.ipa", appaloosaClient.constructKey("/54/uploads/${filename}", "/tmp/test.ipa"));
		 assertEquals("elsewhere/youpi.apk", appaloosaClient.constructKey("elsewhere/${filename}", "/tmp/other/youpi.apk"));
	}
	
	@Test
	public void getMobileApplicationUpdateDetailsShouldAskAppaloosa() throws AppaloosaDeployException{
		String url = "/mobile_application_updates/772.json?token="+ORGANISATION_TOKEN;
		server.expect(GET, url).respondWith(200, null, "{\"id\":772,\"activation_date\":null, \"status\":1,\"application_id\":\"com.appaloosa.sampleapp\", \"other\":\"test\"}");
		
		MobileApplicationUpdate update = appaloosaClient.getMobileApplicationUpdateDetails(772);
		
		assertNotNull(update);
		
		server.verify();
	}
	
	String getTestFile(String filename) {
		return "src/test/resources/"+filename;
	}

}
