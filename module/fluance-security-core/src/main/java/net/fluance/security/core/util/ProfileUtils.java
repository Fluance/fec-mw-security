package net.fluance.security.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.fluance.security.core.model.jpa.Profile;
import net.fluance.security.core.model.jpa.Role;

public class ProfileUtils {

	/**
	 * Checks if a profile  is assigned a role
	 * @param profile
	 * @param roleName the role to check the assignment to the profile
	 * @return
	 */
	public static final boolean hasRole(Profile profile, String roleName) {
		if (profile == null || profile.getRoles() == null) {
			return false;
		}

		for (Role role : profile.getRoles()) {
			if (role.getName() != null && role.getName().equals(roleName)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * 
	 * @param roles
	 * @return
	 */
	public static final List<String> rolesNames(Collection<Role> roles) {

		List<String> rolesNames = new ArrayList<>();
		
		if(roles != null) {
			for (Role role : roles) {
				rolesNames.add(role.getName());
			}
		}
		
		return rolesNames;
	}
	
	/**
	 * Retrieve the role corresponding to the given role name from a profile
	 * @param profile
	 * @param roleName The role with the name to be searched
	 * @return
	 */
	public static Role getRole(Profile profile, String roleName) {
		if (profile == null || profile.getRoles() == null) {
			return null;
		}

		for (Role role : profile.getRoles()) {
			if (role.getName() != null && role.getName().equals(roleName)) {
				return role;
			}
		}

		return null;
	}
	
}
