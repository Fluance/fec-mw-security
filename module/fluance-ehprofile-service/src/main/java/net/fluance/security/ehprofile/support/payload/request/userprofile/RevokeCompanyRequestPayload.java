/**
 * 
 */
package net.fluance.security.ehprofile.support.payload.request.userprofile;

import java.util.List;

/**
 *
 */
public class RevokeCompanyRequestPayload extends BasicUserProfileUpdatePayload {
	
	private List<Integer> companies;
	
	/**
	 * @return the companies
	 */
	public List<Integer> getCompanies() {
		return companies;
	}

	/**
	 * @param companies the companies to set
	 */
	public void setCompanies(List<Integer> companies) {
		this.companies = companies;
	}

}
