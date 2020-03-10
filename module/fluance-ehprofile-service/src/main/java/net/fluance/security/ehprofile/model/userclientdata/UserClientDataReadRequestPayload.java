package net.fluance.security.ehprofile.model.userclientdata;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserClientDataReadRequestPayload {

	@JsonProperty("client_id")
	private String clientId;
	@JsonProperty("client_secret")
	private String clientSecret;
	private String username;
	@JsonProperty("domain")
	private String domainName;

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
	
	/**
	 * @return the clientSecret
	 */
	public String getClientSecret() {
		return clientSecret;
	}
	
	/**
	 * @param clientSecret the clientSecret to set
	 */
	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}
	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}
	
	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}
	
	/**
	 * @return the domainName
	 */
	public String getDomainName() {
		return domainName;
	}
	
	/**
	 * @param domainName the domainName to set
	 */
	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

}