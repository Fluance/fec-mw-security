package net.fluance.security.core.model.jpa;

import java.io.Serializable;

/**
 * The persistent class for the user_company database table.
 * 
 */
@SuppressWarnings("serial")
public class UserCompanyPK implements Serializable {

	private Integer companyId;
	private Integer profileId;
	
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

}