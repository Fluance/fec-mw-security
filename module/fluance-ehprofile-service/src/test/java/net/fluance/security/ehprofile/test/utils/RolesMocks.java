package net.fluance.security.ehprofile.test.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import net.fluance.security.core.model.jpa.Role;
import net.fluance.security.core.model.jpa.RoleMock;

public class RolesMocks {

	private RolesMocks() {}
	
	public static List<Role> getListOfRoles() {
		List<Role> roles = new ArrayList<>();

		IntStream.rangeClosed(1, 5).forEach(id -> {
			RoleMock role = new RoleMock();
			role.setName("ROLE" + id);
			role.setId(id);
			
			roles.add(role);
		});

		return roles;
	}

	public static List<String> getListOfRolesAsListOfStirngs() {
		List<String> roles = new ArrayList<>();

		IntStream.rangeClosed(1, 5).forEach(id -> {
			roles.add("ROLE" + id);
		});

		return roles;
	}
}
