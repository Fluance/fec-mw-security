package net.fluance.security.ehprofile.model.userclientdata;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public class UserClientDataSetHistoryRequestPayload {

	private String username;
	@JsonProperty("domain")
	private String domainName;
	private JsonNode history;
	
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
	
	/**
	 * @return the history
	 */
	public JsonNode getHistory() {
		return history;
	}
	
	/**
	 * @param history the history to set
	 */
	public void setHistory(JsonNode history) {
		this.history = history;
	}

}
