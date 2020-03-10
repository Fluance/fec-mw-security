package net.fluance.security.core.model.jpa;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;


/**
 * The persistent class for the profile_identity database table.
 * 
 */
@SuppressWarnings("serial")
@Entity
@Table(name="profile_identity")
@IdClass(UserCompanyIdentityPK.class)
public class UserCompanyIdentity implements Serializable {

	@Id
	@Column(name = "company_id")
	private Integer companyId;
	
	@Id
	@Column(name = "profile_id")
	private Integer profileId;

	@Id
	@Column(name = "provider_id")
	private Integer providerId;
	
	@Column(name = "staffid")
	private String staffId;
	
	
	
	/**
	 * @param profileId
	 * @param providerId
	 * @param staffId
	 */
	public UserCompanyIdentity() {}

	public UserCompanyIdentity(Integer profileId, Integer providerId, String staffId) {
		super();
		this.profileId = profileId;
		this.providerId = providerId;
		this.staffId = staffId;
	}

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
	 * @return the staffId
	 */
	public String getStaffId() {
		return staffId;
	}

	/**
	 * @param staffId the staffId to set
	 */
	public void setStaffId(String staffId) {
		this.staffId = staffId;
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