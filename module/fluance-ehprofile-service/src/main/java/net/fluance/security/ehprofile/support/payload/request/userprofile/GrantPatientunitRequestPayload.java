/**
 * 
 */
package net.fluance.security.ehprofile.support.payload.request.userprofile;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 */
public class GrantPatientunitRequestPayload extends BasicUserProfileUpdatePayload {
	@JsonProperty("companyid")
	private Integer companyId;
	@JsonProperty("patientunits")
	private List<String> patientUnits;

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

}
