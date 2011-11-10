/*
 * The MIT License
 *
 * Copyright (c) 2011 OCTO Technology
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.appaloosastore.client;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;


@JsonIgnoreProperties(ignoreUnknown = true)
public class MobileApplicationUpdate {
	private static ObjectMapper jsonMapper = new ObjectMapper();

	public Integer id;
	public Integer status;
	@JsonProperty(value = "status_message")
	public String statusMessage;
	@JsonProperty(value = "application_id")
	public String applicationId;

	public static MobileApplicationUpdate createFrom(String json)
			throws AppaloosaDeployException {
		try {
			return jsonMapper.readValue(json, MobileApplicationUpdate.class);
		} catch (Exception e) {
			throw new AppaloosaDeployException(
					"Impossible to parse mobileApplicationUpdate string: "
							+ json, e);
		}
	}

	public boolean isProcessed() {
		return (status != null && status > 4) || StringUtils.isNotBlank(applicationId);
	}

	public boolean hasError() {
		return (status != null) && status > 4;
	}
}
