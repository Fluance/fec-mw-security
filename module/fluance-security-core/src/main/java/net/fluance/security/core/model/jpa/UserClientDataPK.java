package net.fluance.security.core.model.jpa;

import java.io.Serializable;
import java.util.UUID;

@SuppressWarnings("serial")
public class UserClientDataPK implements Serializable {
	
	@org.hibernate.annotations.Type(type="org.hibernate.type.PostgresUUIDType")
	private UUID clientId;
	private Integer profileId;
	
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

}