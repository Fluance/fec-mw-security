package net.fluance.security.ehprofile.test.utils;

import net.fluance.security.core.model.jpa.UserType;

public class UserTypeMocks {
	private UserTypeMocks() {}
	
	public static UserType getUserType() {
		UserType userType = new UserType();
		userType.setDescription("Aplication User");
		userType.setId(1);
		userType.setName("application");
		
		return userType;
	}
}
