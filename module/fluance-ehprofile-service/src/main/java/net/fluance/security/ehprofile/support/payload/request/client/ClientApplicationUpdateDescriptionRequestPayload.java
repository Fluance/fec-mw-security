/**
 * 
 */
package net.fluance.security.ehprofile.support.payload.request.client;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 */
public class ClientApplicationUpdateDescriptionRequestPayload {

	@JsonProperty("client_id")
	private String clientId;
	private String name;
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return the clientId
	 */
	public String getClientId() {
		return clientId;
	}
	
	/**
	 * @param clientId the clientId to set
	 */
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	
}
