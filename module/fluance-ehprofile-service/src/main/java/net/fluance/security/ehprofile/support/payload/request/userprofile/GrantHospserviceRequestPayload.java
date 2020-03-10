/**
 * 
 */
package net.fluance.security.ehprofile.support.payload.request.userprofile;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 */
public class GrantHospserviceRequestPayload extends BasicUserProfileUpdatePayload {
	@JsonProperty("companyid")
	private Integer companyId;
	@JsonProperty("hospservices")
	private List<String> hospServices;

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

}
