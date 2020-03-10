package net.fluance.security.ehprofile.test.utils;

import net.fluance.app.data.model.identity.AccessControl;
import net.fluance.app.data.model.identity.EhProfile;

public class EhProfileMocks {
	private EhProfileMocks() {}
	
	public static EhProfile getEhProfile(String username, String domain) {
		EhProfile ehProfile = new EhProfile();
		ehProfile.setUsername(username);
		ehProfile.setDomain(domain);
		 
		AccessControl accessControl = new AccessControl();
		accessControl.setGrantedCompanies(GrantedCompanyMocks.getListOfGrantedCompany());
		accessControl.setRoles(RolesMocks.getListOfRolesAsListOfStirngs());		
		ehProfile.setGrants(accessControl);
		
		ehProfile.setLanguage("en");
		ehProfile.setScimId(null);
		ehProfile.setUsertype(UserTypeMocks.getUserType().getName());
		
		return ehProfile;
	}
}
