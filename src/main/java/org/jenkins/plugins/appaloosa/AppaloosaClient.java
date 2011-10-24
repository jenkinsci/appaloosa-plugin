package org.jenkins.plugins.appaloosa;

import java.io.File;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;


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
        logger.println("Uploading file "+filePath+" to Appaloosa");
        
        // Retrieve details from Appaloosa to do the upload
        AppaloosaUploadBinaryForm uploadForm = getUploadForm();
        
        // Upload the file on Amazon
        uploadFile(filePath, uploadForm);

        // Notify Appaloosa that the file is available
        MobileApplicationUpdate update = notifyAppaloosaForFile(filePath, uploadForm);

        // Wait for Appaloosa to process the file
        while (!update.isProcessed()){
        	smallWait();
        	update = getMobileApplicationUpdateDetails(update.id); 
        }
        
        // publish update
        if ( ! update.hasError()){
        	publish(update);
        }
	}

	protected MobileApplicationUpdate publish(MobileApplicationUpdate update) throws AppaloosaDeployException {
		HttpPost httpPost = new HttpPost(publishUpdateUrl());
		
		List<NameValuePair> parameters = new ArrayList<NameValuePair>();
		parameters.add(new BasicNameValuePair("token", organisationToken));
		parameters.add(new BasicNameValuePair("id", update.id.toString()));
		
		try{
			httpPost.setEntity(new UrlEncodedFormEntity(parameters ));
			HttpResponse response = httpClient.execute(httpPost);
			String json = EntityUtils.toString( response.getEntity(), "UTF-8" );
			
			return MobileApplicationUpdate.createFrom(json);
		} catch (AppaloosaDeployException e) {
			throw e;
		} catch (Exception e) {
			throw new AppaloosaDeployException("Error during publishing update (id="+update.id+")", e);
		}
	}

	protected String publishUpdateUrl() {
		return getAppaloosaBaseUrl() + "api/publish_update.json";
	}

	protected MobileApplicationUpdate getMobileApplicationUpdateDetails(Integer id) throws AppaloosaDeployException {
		HttpGet httpGet = new HttpGet(updateUrl(id));
		
		HttpResponse response;
		try {
			response = httpClient.execute(httpGet);
			String json = EntityUtils.toString( response.getEntity(), "UTF-8" );
			return MobileApplicationUpdate.createFrom(json);
		} catch (Exception e) {
			throw new AppaloosaDeployException("Error while get details for update id = " + id, e);
		}
	}

	private String updateUrl(Integer id) {
		return getAppaloosaBaseUrl() + "mobile_application_updates/"+id+".json?token="+organisationToken;
	}

	protected void smallWait() {
		
	}

	protected MobileApplicationUpdate notifyAppaloosaForFile(String filePath, AppaloosaUploadBinaryForm uploadForm) throws AppaloosaDeployException {
		
		HttpPost httpPost = new HttpPost(onBinaryUploadUrl());
		
		List<NameValuePair> parameters = new ArrayList<NameValuePair>();
		parameters.add(new BasicNameValuePair("token", organisationToken));
		String key = constructKey(uploadForm.getKey(), filePath);
		parameters.add(new BasicNameValuePair("key", key));
		
		try{
			httpPost.setEntity(new UrlEncodedFormEntity(parameters ));
			HttpResponse response = httpClient.execute(httpPost);
			String json = EntityUtils.toString( response.getEntity(), "UTF-8" );
			
			return MobileApplicationUpdate.createFrom(json);
		} catch (AppaloosaDeployException e) {
			throw e;
		} catch (Exception e) {
			throw new AppaloosaDeployException("Error during appaloosa notification", e);
		}
	}

	protected String constructKey(String key, String filePath) {
		String filename = new File(filePath).getName();
		return StringUtils.replace(key, "${filename}", filename);
	}

	protected void uploadFile(String filePath,
			AppaloosaUploadBinaryForm uploadForm) throws AppaloosaDeployException {
		
		try {
			File file = new File(filePath);

		    MultipartEntity entity = new MultipartEntity();
		    ContentBody cbFile = new FileBody(file);
			entity.addPart("policy", new StringBody( uploadForm.getPolicy(), "text/plain", Charset.forName( "UTF-8" )));
			entity.addPart("success_action_status", new StringBody( uploadForm.getSuccessActionStatus().toString(), "text/plain", Charset.forName( "UTF-8" )));
			entity.addPart("Content-Type", new StringBody( uploadForm.getContentType(), "text/plain", Charset.forName( "UTF-8" )));
			entity.addPart("signature", new StringBody( uploadForm.getSignature(), "text/plain", Charset.forName( "UTF-8" )));
			entity.addPart("AWSAccessKeyId", new StringBody( uploadForm.getAccessKey(), "text/plain", Charset.forName( "UTF-8" )));
			entity.addPart("key", new StringBody( uploadForm.getKey(), "text/plain", Charset.forName( "UTF-8" )));
			entity.addPart("acl", new StringBody( uploadForm.getAcl(), "text/plain", Charset.forName( "UTF-8" )));
		    entity.addPart("file", cbFile);

			HttpPost httppost = new HttpPost(uploadForm.getUrl());
			httppost.setEntity(entity);
			HttpResponse response = httpClient.execute(httppost);
			String strResponse = EntityUtils.toString( response.getEntity(), "UTF-8" );
			
			System.out.println(strResponse);
			
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
			throw new AppaloosaDeployException("impossible to retrieve upload information from "+appaloosaUrl, e);
		}
	}

	public void useLogger(PrintStream logger) {
		this.logger = logger;
	}

	protected String onBinaryUploadUrl() {
		String url = getAppaloosaBaseUrl();
		url = url + "api/on_binary_upload";
		return url;
	}
	
	protected String newBinaryUrl() {
		String url = getAppaloosaBaseUrl();
		url = url + "api/upload_binary_form.json?token="+organisationToken;
		return url;
	}

	protected String getAppaloosaBaseUrl() {
		String url = appaloosaUrl;
		if (appaloosaPort != 80){
			url = url + ":"+appaloosaPort;
		}
		if (! url.endsWith("/")){
			url = url + "/";
		}
		return url;
	}

	public void setBaseUrl(String appaloosaUrl) {
		this.appaloosaUrl = appaloosaUrl;
	}

	public void setPort(int port) {
		appaloosaPort = port;
	}
	
}
