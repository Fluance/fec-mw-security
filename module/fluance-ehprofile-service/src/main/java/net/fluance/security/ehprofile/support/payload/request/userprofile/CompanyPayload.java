/**
 * 
 */
package net.fluance.security.ehprofile.support.payload.request.userprofile;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import net.fluance.app.data.model.identity.CompanyStaffId;

/**
 *
 */
public class CompanyPayload {

	@JsonProperty("id")
	private Integer companyId;
	@JsonProperty("staffids")
	private List<CompanyStaffId> staffIds;
	@JsonProperty("hospservices")
	private List<String> hospServices;
	@JsonProperty("patientunits")
	private List<String> patientUnits;
	
	/**
	 * 
	 */
	public CompanyPayload() {
		this.staffIds = new ArrayList<>();
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
	 * @return the hospServices
	 */
	public List<String> getHospServices() {
		return hospServices;
	}
	
	/**
	 * @param hospServices the hospServices to set
	 */
	public void setHospServices(List<String> hospServices) {
		this.hospServices = hospServices;
	}
	
	/**
	 * @return the patientUnits
	 */
	public List<String> getPatientUnits() {
		return patientUnits;
	}
	
	/**
	 * @param patientUnits the patientUnits to set
	 */
	public void setPatientUnits(List<String> patientUnits) {
		this.patientUnits = patientUnits;
	}
	
	@Override
	public String toString() {
		return "{id=" + companyId + ",units=" + patientUnits + ",hospservices=" + hospServices + "}";
	}

	/**
	 * @return the staffIds
	 */
	public List<CompanyStaffId> getStaffIds() {
		return staffIds;
	}

	/**
	 * @param staffIds the staffIds to set
	 */
	public void setStaffIds(List<CompanyStaffId> staffIds) {
		this.staffIds = staffIds;
	}

}
