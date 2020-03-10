package net.fluance.security.core.model.jpa;

import java.io.Serializable;

/**
 * The persistent class for the user_company database table.
 * 
 */
@SuppressWarnings("serial")
public class UserCompanyIdentityPK implements Serializable {

	private Integer companyId;
	private Integer profileId;
	private Integer providerId;
	
	/**
	 * @return the companyId
	 */
	public Integer getCompanyId() {
		return companyId;
	}
	
	/**
	 * @param companyId the companyId to set
	 */
	public void setCompanyId(Integer companyId) {
		this.companyId = companyId;
	}
	
	/**
	 * @return the profileId
	 */
	public Integer getProfileId() {
		return profileId;
	}

	/**
	 * @param profileId the profileId to set
	 */
	public void setProfileId(Integer profileId) {
		this.profileId = profileId;
	}

	/**
	 * @return the providerId
	 */
	public Integer getProviderId() {
		return providerId;
	}

	/**
	 * @param providerId the providerId to set
	 */
	public void setProviderId(Integer providerId) {
		this.providerId = providerId;
	}
	
}