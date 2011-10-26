package com.appaloosastore.client;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
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

/**
 * Client for appaloosa, http://www.appaloosa-store.com. Usage : <br>
 * <code>
 * 	AppaloosaClient client = new AppaloosaClient("my_organisation_token"); <br>
 *  try {                                                                  <br>
 *    client.deployFile("/path/to/archive");                               <br>
 *    System.out.println("Archive deployed");                              <br>
 *  } catch (AppaloosaDeployException e) {                                 <br>
 *  	System.err.println("Something went wrong");                        <br>
 *  }                                                                      <br>
 * </code> Organisation token is available on settings page.
 * 
 * @author Benoit Lafontaine
 */
public class AppaloosaClient {
	private final String organisationToken;
	private PrintStream logger = System.out;
	private HttpClient httpClient = new DefaultHttpClient();
	private String appaloosaUrl = "http://www.appaloosa-store.com";
	private int appaloosaPort = 80;
	private int waitDuration = 1000;

	public AppaloosaClient(String organisationToken) {
		this.organisationToken = organisationToken;
	}

	/**
	 * @param filePath
	 *            physical path of the file to upload
	 * @throws AppaloosaDeployException
	 *             when something went wrong
	 * */
	public void deployFile(String filePath) throws AppaloosaDeployException {
		logger.println("== Deploy file " + filePath + " to Appaloosa");

		// Retrieve details from Appaloosa to do the upload
		logger.println("==   Ask for upload information");
		UploadBinaryForm uploadForm = getUploadForm();

		// Upload the file on Amazon
		logger.println("==   Upload file " + filePath);
		uploadFile(filePath, uploadForm);

		// Notify Appaloosa that the file is available
		logger.println("==   Start remote processing file");
		MobileApplicationUpdate update = notifyAppaloosaForFile(filePath,
				uploadForm);

		// Wait for Appaloosa to process the file
		while (!update.isProcessed()) {
			smallWait();
			logger.println("==   Check for application informations");
			update = getMobileApplicationUpdateDetails(update.id);
		}

		// publish update
		if (!update.hasError()) {
			logger.println("==   Publish uploaded file");
			publish(update);
			logger.println("== File deployed and published successfully");
		} else {
			logger.println("== Impossible to publish file: " + update.statusMessage);
		}
	}

	protected MobileApplicationUpdate publish(MobileApplicationUpdate update)
			throws AppaloosaDeployException {
		HttpPost httpPost = new HttpPost(publishUpdateUrl());

		List<NameValuePair> parameters = new ArrayList<NameValuePair>();
		parameters.add(new BasicNameValuePair("token", organisationToken));
		parameters.add(new BasicNameValuePair("id", update.id.toString()));

		try {
			httpPost.setEntity(new UrlEncodedFormEntity(parameters));
			HttpResponse response = httpClient.execute(httpPost);
			String json = readBodyResponse(response);

			return MobileApplicationUpdate.createFrom(json);
		} catch (AppaloosaDeployException e) {
			throw e;
		} catch (Exception e) {
			throw new AppaloosaDeployException(
					"Error during publishing update (id=" + update.id + ")", e);
		} finally {
			releaseHttpConnection();
		}
	}

	protected String readBodyResponse(HttpResponse response) throws ParseException, IOException {
		return EntityUtils.toString(response.getEntity(), "UTF-8");
	}

	protected String publishUpdateUrl() {
		return getAppaloosaBaseUrl() + "api/publish_update.json";
	}

	protected MobileApplicationUpdate getMobileApplicationUpdateDetails(
			Integer id) throws AppaloosaDeployException {
		HttpGet httpGet = new HttpGet(updateUrl(id));

		HttpResponse response;
		try {
			response = httpClient.execute(httpGet);
			if (response.getStatusLine().getStatusCode() == 200) {
				String json = readBodyResponse(response);
				return MobileApplicationUpdate.createFrom(json);
			} else {
				throw createExceptionWithAppaloosaErrorResponse(response,
						"Impossible to get details for application update "
								+ id + ", cause: ");
			}
		} catch (AppaloosaDeployException e) {
			throw e;
		} catch (Exception e) {
			throw new AppaloosaDeployException(
					"Error while get details for update id = " + id, e);
		} finally {
			releaseHttpConnection();
		}
	}

	protected AppaloosaDeployException createExceptionWithAppaloosaErrorResponse(
			HttpResponse response, String prefix) throws ParseException,
			IOException {
		int statusCode = response.getStatusLine().getStatusCode();
		String cause = "";
		switch (statusCode) {
		case 404:
			cause = "resource not found (404)";
			break;
		case 422:
			String json;
			json = readBodyResponse(response);
			try {
				AppaloosaErrors errors = AppaloosaErrors.createFromJson(json);
				cause = errors.toString();
			} catch (Exception e) {
				cause = json;
			}
			break;
		default:
			break;
		}
		return new AppaloosaDeployException(prefix + cause);
	}

	protected String updateUrl(Integer id) {
		return getAppaloosaBaseUrl() + "mobile_application_updates/" + id
				+ ".json?token=" + organisationToken;
	}

	protected void smallWait() {
		try {
			Thread.sleep(waitDuration);
		} catch (InterruptedException e) {
		}
	}

	protected MobileApplicationUpdate notifyAppaloosaForFile(String filePath,
			UploadBinaryForm uploadForm)
			throws AppaloosaDeployException {

		HttpPost httpPost = new HttpPost(onBinaryUploadUrl());

		List<NameValuePair> parameters = new ArrayList<NameValuePair>();
		parameters.add(new BasicNameValuePair("token", organisationToken));
		String key = constructKey(uploadForm.getKey(), filePath);
		parameters.add(new BasicNameValuePair("key", key));

		try {
			httpPost.setEntity(new UrlEncodedFormEntity(parameters));
			HttpResponse response = httpClient.execute(httpPost);
			String json = readBodyResponse(response);

			return MobileApplicationUpdate.createFrom(json);
		} catch (AppaloosaDeployException e) {
			throw e;
		} catch (Exception e) {
			throw new AppaloosaDeployException(
					"Error during appaloosa notification", e);
		} finally {
			releaseHttpConnection();
		}
	}

	protected String constructKey(String key, String filePath) {
		String filename = new File(filePath).getName();
		return StringUtils.replace(key, "${filename}", filename);
	}

	protected void uploadFile(String filePath, UploadBinaryForm uploadForm) throws AppaloosaDeployException {
		try {
			File file = new File(filePath);
			HttpPost httppost = createHttpPost(uploadForm, file);
			HttpResponse response = httpClient.execute(httppost);

			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != uploadForm.getSuccessActionStatus()) {
				String message = readErrorFormAmazon(IOUtils.toString(response
						.getEntity().getContent()));
				throw new AppaloosaDeployException("Impossible to upload file "
						+ filePath + ": " + message);
			}
		} catch (AppaloosaDeployException e) {
			throw e;
		} catch (Exception e) {
			throw new AppaloosaDeployException("Error while uploading "
					+ filePath, e);
		} finally {
			releaseHttpConnection();
		}
	}

	protected HttpPost createHttpPost(UploadBinaryForm uploadForm,
			File file) throws UnsupportedEncodingException {
		MultipartEntity entity = new MultipartEntity();
		ContentBody cbFile = new FileBody(file);
		addParam(entity, "policy", uploadForm.getPolicy());

		addParam(entity, "success_action_status", uploadForm.getSuccessActionStatus().toString());
		addParam(entity, "Content-Type", uploadForm.getContentType());
		addParam(entity, "signature", uploadForm.getSignature());
		addParam(entity, "AWSAccessKeyId", uploadForm.getAccessKey());
		addParam(entity, "key", uploadForm.getKey());
		addParam(entity, "acl", uploadForm.getAcl());

		entity.addPart("file", cbFile);

		HttpPost httppost = new HttpPost(uploadForm.getUrl());
		httppost.setEntity(entity);
		return httppost;
	}

	protected void addParam(MultipartEntity entity, String paramName,
			String paramValue) throws UnsupportedEncodingException {
		entity.addPart(paramName, new StringBody(paramValue, "text/plain",
				Charset.forName("UTF-8")));
	}

	protected String readErrorFormAmazon(String body) {
		int start = body.indexOf("<Message>") + 9;
		int end = body.indexOf("</Message>");
		return body.substring(start, end);
	}

	protected UploadBinaryForm getUploadForm()
			throws AppaloosaDeployException {
		HttpGet httpGet = new HttpGet(newBinaryUrl());
		try {
			HttpResponse response = httpClient.execute(httpGet);
			String json = IOUtils.toString(response.getEntity().getContent());
			UploadBinaryForm uploadForm = UploadBinaryForm
					.createFormJson(json);
			return uploadForm;
		} catch (Exception e) {
			throw new AppaloosaDeployException(
					"impossible to retrieve upload information from "
							+ appaloosaUrl, e);
		} finally {
			releaseHttpConnection();
		}
	}

	protected void releaseHttpConnection() {
		httpClient.getConnectionManager().shutdown();
		httpClient = new DefaultHttpClient();
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
		url = url + "api/upload_binary_form.json?token=" + organisationToken;
		return url;
	}

	protected String getAppaloosaBaseUrl() {
		String url = appaloosaUrl;
		if (appaloosaPort != 80) {
			url = url + ":" + appaloosaPort;
		}
		if (!url.endsWith("/")) {
			url = url + "/";
		}
		return url;
	}

	/**
	 * To change the url of appaloosa server. Mostly for tests usage or for
	 * future evolutions.
	 * 
	 * @param appaloosaUrl
	 */
	public void setBaseUrl(String appaloosaUrl) {
		this.appaloosaUrl = appaloosaUrl;
	}

	/**
	 * To change port of appaloosa server. Mostly for tests usage or for future
	 * evolutions.
	 * 
	 * @param appaloosaUrl
	 */
	public void setPort(int port) {
		appaloosaPort = port;
	}

	protected void setWaitDuration(int waitDuration) {
		this.waitDuration = waitDuration;
	}

}
