package org.jenkins.plugins.appaloosa;

import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;


public class AppaloosaClient {
	private final String organisationToken;
	private PrintStream logger = System.out;
	private HttpClient httpClient = new DefaultHttpClient();
	private String appaloosaUrl = "http://www.appaloosa-store.com";
	private int appaloosaPort = 80;
	
	public AppaloosaClient(String organisationToken) {
		this.organisationToken = organisationToken;
	}

	/**
	 * @param filePath physical path of the file to upload 
	 * 
	 * */
	public void deployFile(String filePath) throws AppaloosaDeployException{
        logger.print("Uploading file to Appaloosa");
        logger.println(filePath);
        
        // Retrieve details from Appaloosa to do the upload
        AppaloosaUploadBinaryForm uploadForm = getUploadForm();
        
        // Upload the file on Amazon
        uploadFile(filePath, uploadForm);

        // Notify Appaloosa that the file is available

        // Wait for Appaloosa to process the file
        
	}

	protected void uploadFile(String filePath,
			AppaloosaUploadBinaryForm uploadForm) throws AppaloosaDeployException {
		HttpPost post= new HttpPost(uploadForm.getUrl());
		try {
			httpClient.execute(post);
		} catch (Exception e) {
			throw new AppaloosaDeployException("Error while uploading "+filePath, e);
		}
	}

	protected AppaloosaUploadBinaryForm getUploadForm() throws AppaloosaDeployException {
		HttpGet httpGet = new HttpGet(newBinaryUrl());
        try {
			HttpResponse response = httpClient.execute(httpGet);
			String json = IOUtils.toString(response.getEntity().getContent());
			AppaloosaUploadBinaryForm uploadForm = AppaloosaUploadBinaryForm.createFormJson(json);
			return uploadForm;
		} catch (Exception e) {
			throw new AppaloosaDeployException("impossible to retrive informations form appaloosa-store.com", e);
		}
	}

	public void useLogger(PrintStream logger) {
		this.logger = logger;
	}

	protected String newBinaryUrl() {
		String url = appaloosaUrl;
		if (appaloosaPort != 80){
			url = url + ":"+appaloosaPort;
		}
		url = url + "/api/upload_binary_form?token="+organisationToken;
		return url;
	}

	public void setBaseUrl(String appaloosaUrl) {
		this.appaloosaUrl = appaloosaUrl;
	}

	public void setPort(int port) {
		appaloosaPort = port;
	}
	
}
