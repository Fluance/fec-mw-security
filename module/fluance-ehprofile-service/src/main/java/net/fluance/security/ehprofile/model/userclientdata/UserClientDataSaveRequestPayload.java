package net.fluance.security.ehprofile.model.userclientdata;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * The persistent class for the client database table.
 * 
 */
public class UserClientDataSaveRequestPayload {

	@JsonProperty("client_id")
	private String clientId;
	private JsonNode history;
	private JsonNode preferences;
		
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

	/**
	 * @return the preferences
	 */
	public JsonNode getPreferences() {
		return preferences;
	}

	/**
	 * @param preferences the preferences to set
	 */
	public void setPreferences(JsonNode preferences) {
		this.preferences = preferences;
	}

}