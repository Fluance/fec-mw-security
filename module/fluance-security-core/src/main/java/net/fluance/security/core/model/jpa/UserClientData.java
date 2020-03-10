package net.fluance.security.core.model.jpa;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;


/**
 * The persistent class for the client database table.
 * 
 */
@SuppressWarnings("serial")
@Entity
@Table(name="user_client_data")
@IdClass(value=UserClientDataPK.class)
public class UserClientData implements Serializable {

	@Id
	@org.hibernate.annotations.Type(type="org.hibernate.type.PostgresUUIDType")
	private UUID clientId;
	@Id
	@JsonIgnore
	private Integer profileId;
	@Type(type = "JpaJsonObject")
	private JsonNode history;
	@Type(type = "JpaJsonObject")
	private JsonNode preferences;

	/**
	 * @return the clientId
	 */
	public UUID getClientId() {
		return clientId;
	}

	/**
	 * @param clientId the clientId to set
	 */
	public void setClientId(UUID clientId) {
		this.clientId = clientId;
	}

	/**
	 * @return the userId
	 */
	public Integer getProfileId() {
		return profileId;
	}

	/**
	 * @param userId the userId to set
	 */
	public void setProfileId(Integer profileId) {
		this.profileId = profileId;
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