package net.fluance.security.ehprofile.support.payload.response.userprofile;

public class LightUserProfile {
	private String userName;
	private String firstName;
	private String lastName;
	private String department;

	public String getUserName() {
		return userName;
	}
	
	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getDepartment() {
		return department;
	}

	public LightUserProfile withUserName(String userName) {
		this.userName = userName;
		return this;
	}

	public LightUserProfile withFirstName(String firstname) {
		this.firstName = firstname;
		return this;
	}

	public LightUserProfile withLastName(String lastname) {
		this.lastName = lastname;
		return this;
	}
	
	public LightUserProfile withDepartment(String department) {
		this.department = department;
		return this;
	}
}
