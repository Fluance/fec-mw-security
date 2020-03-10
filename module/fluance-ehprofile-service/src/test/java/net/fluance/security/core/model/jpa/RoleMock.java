package net.fluance.security.core.model.jpa;


/**
 * Overwrites the pojo that represents the JPA to add the set method for the id property
 */
public class RoleMock extends Role {
	private static final long serialVersionUID = 6920983019055916831L;

	public void setId(Integer id) {
		super.id = id;
	}
}
