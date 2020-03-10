package net.fluance.security.ehprofile.test.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import net.fluance.security.core.model.jpa.UserCompanyIdentity;

public class UserCompanyIdentityMocks {
	private UserCompanyIdentityMocks() {}
	
	public static UserCompanyIdentity getUserCompanyIdentity(Integer comapanyId, Integer profileId){
		UserCompanyIdentity userCompanyIdentity = new UserCompanyIdentity();
		userCompanyIdentity.setCompanyId(comapanyId);
		userCompanyIdentity.setProfileId(profileId);
		userCompanyIdentity.setProviderId(20);
		userCompanyIdentity.setStaffId("100");
		
		return userCompanyIdentity;
	}
	
	public static List<UserCompanyIdentity> getListOfUserCompanyIdentities(Integer profileId){
		List<UserCompanyIdentity> userCompanyIdentities = new ArrayList<>();
		
		IntStream.rangeClosed(1, 5)
    	.forEach(id -> {
    		userCompanyIdentities.add(getUserCompanyIdentity(id, profileId));
    	});
		
		return userCompanyIdentities;
	}
}
