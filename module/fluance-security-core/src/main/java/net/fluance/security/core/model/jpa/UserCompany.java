package net.fluance.security.core.model.jpa;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import com.fasterxml.jackson.databind.JsonNode;


/**
 * The persistent class for the profile_identity database table.
 * 
 */
@SuppressWarnings("serial")
@Entity
@Table(name="profile_permission")
@IdClass(UserCompanyPK.class)
public class UserCompany implements Serializable {

	@Id
	@Column(name = "company_id")
	private Integer companyId;
	
	@Id
	@Column(name = "profile_id")
	private Integer profileId;

	@Type(type = "JpaJsonObject")
	@Column(name = "location")
	private JsonNode unitsAndServices;
	
	public UserCompany() {}

	/**
	 * @param companyId
	 * @param profileId
	 * @param unitsAndServices
	 */
	public UserCompany(Integer companyId, Integer profileId, JsonNode unitsAndServices) {
		super();
		this.companyId = companyId;
		this.profileId = profileId;
		this.unitsAndServices = unitsAndServices;
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
	 * @return the unitsAndServices
	 */
	public JsonNode getUnitsAndServices() {
		return unitsAndServices;
	}

	/**
	 * @param unitsAndServices the unitsAndServices to set
	 */
	public void setUnitsAndServices(JsonNode unitsAndServices) {
		this.unitsAndServices = unitsAndServices;
	}

}