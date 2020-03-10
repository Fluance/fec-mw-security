package net.fluance.security.ehprofile.test.utils;

import java.util.Arrays;
import java.util.List;

import net.fluance.security.core.model.jpa.Profile;
import net.fluance.security.core.model.jpa.ProfileMock;

public class ProfileMocks {
	private ProfileMocks() {}
	
	public static Profile getDefaultProfile(String username, String domain) {
		ProfileMock profile = new ProfileMock();
		profile.setId(1);
		profile.setUsername(username);
		profile.setDomainName(domain);		
		profile.setLanguage("en");
		profile.setUserType(UserTypeMocks.getUserType());
		
		profile.setRoles(RolesMocks.getListOfRoles());
		
		return profile;
	}
	
	public static List<Profile> getDefaultProfileInList(String username, String domain) {
		return Arrays.asList(getDefaultProfile(username, domain));
	}
}
