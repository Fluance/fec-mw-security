package net.fluance.security.ehprofile.support.payload.response.userprofile;

import org.apache.http.client.methods.CloseableHttpResponse;

import net.fluance.app.web.support.payload.response.GenericResponsePayload;


public class UserProfileGetResponsePayload extends GenericResponsePayload {
	private CloseableHttpResponse httpResponse;

	/**
	 * @return the httpResponse
	 */
	public CloseableHttpResponse getHttpResponse() {
		return httpResponse;
	}

	/**
	 * @param httpResponse the httpResponse to set
	 */
	public void setHttpResponse(CloseableHttpResponse httpResponse) {
		this.httpResponse = httpResponse;
	}
}
