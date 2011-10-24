package org.jenkins.plugins.appaloosa;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;

@JsonIgnoreProperties(ignoreUnknown=true)
public class AppaloosaUploadBinaryForm {
	private static ObjectMapper jsonMapper = new ObjectMapper();
	private String policy;
	@JsonProperty(value="success_action_status")
	private Integer successActionStatus;
	@JsonProperty(value="content_type")
	private String contentType;
	private String signature;
	private String url;
	@JsonProperty(value="access_key")
	private String accessKey;
	private String key;
	private String acl;
	
	public static AppaloosaUploadBinaryForm createFormJson(String json) throws AppaloosaDeployException {
		try {
			return jsonMapper.readValue(json, AppaloosaUploadBinaryForm.class);
		} catch (Exception e) {
			throw new AppaloosaDeployException("Impossible to read response form appaloosa", e);
		}
	}

	public String getPolicy() {
		return policy;
	}

	public void setPolicy(String policy) {
		this.policy = policy;
	}
	

	public int getSuccessActionStatus() {
		return successActionStatus;
	}

	public void setSuccessActionStatus(int successActionStatus) {
		this.successActionStatus = successActionStatus;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getAccessKey() {
		return accessKey;
	}

	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getAcl() {
		return acl;
	}

	public void setAcl(String acl) {
		this.acl = acl;
	}

	

}
