package net.fluance.security.ehprofile.test.utils;

import net.fluance.app.data.model.identity.User;

public class UserMocks {
	private UserMocks() {}
	
	public static User getUser(String username, String domain) {
		User user = new User();
		user.setUsername(username);
		user.setDomain(domain);
		
		
		return user;
	}
}
