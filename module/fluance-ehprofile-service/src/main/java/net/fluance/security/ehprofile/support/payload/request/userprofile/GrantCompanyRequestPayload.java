/**
 * 
 */
package net.fluance.security.ehprofile.support.payload.request.userprofile;

import java.util.List;

/**
 *
 */
public class GrantCompanyRequestPayload extends BasicUserProfileUpdatePayload {
	private List<CompanyPayload> companies;
	
	/**
	 * @return the companies
	 */
	public List<CompanyPayload> getCompanies() {
		return companies;
	}

	/**
	 * @param companies the companies to set
	 */
	public void setCompanies(List<CompanyPayload> companies) {
		this.companies = companies;
	}

}
