package net.fluance.security.ehprofile.util;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import net.fluance.app.data.model.identity.CompanyStaffId;
import net.fluance.app.data.model.identity.EhProfile;
import net.fluance.app.data.model.identity.GrantedCompany;

public class EhProfileUtils {

	/**
	 * Given a {@link List} of {@link EhProfile}, returns the element which contains the username given as parameter or null if the username does not exist
	 * @param profiles
	 * @param username
	 * @return
	 */
	public static EhProfile getProfileByUsername(List<EhProfile> profiles, String username) {
		if(StringUtils.isEmpty(username) || profiles == null){
			return null;
		}
		return profiles.stream().filter(profile -> username.equals(profile.getUsername())).findFirst().orElse(null);
	}
	
	/**
	 * Given a {@link EhProfile}, returns the {@link GrantedCompany} which contains the Company Code given as parameter or null in any other situation
	 * 
	 * @param profile
	 * @param companyCode
	 * @return
	 */
	public static GrantedCompany getGrantedCompany(EhProfile profile, String companyCode) {
		if (StringUtils.isEmpty(companyCode) || profile == null){
			return null;
		}
		if (profile.getGrants() == null || profile.getGrants().getGrantedCompanies()==null) {
			return null;
		}
		return profile.getGrants().getGrantedCompanies().stream().filter(grantedCompany -> companyCode.equals(grantedCompany.getCode())).findFirst().orElse(null);
	}

	/**
	 * Given a {@link GrantedCompany}, returns the {@link CompanyStaffId} based in its Provider Id
	 * @param grantedCompany
	 * @param providerId
	 * @return
	 */
	public static CompanyStaffId getCompanyStaffId(GrantedCompany grantedCompany, Integer providerId) {
		if(providerId == null){
			return null;
		}
		if(grantedCompany==null || grantedCompany.getStaffIds()== null || grantedCompany.getStaffIds().isEmpty()) {
			return null;
		}
		return grantedCompany.getStaffIds().stream().filter(staff -> providerId.equals(staff.getProviderId())).findFirst().orElse(null);
	}
	
}
