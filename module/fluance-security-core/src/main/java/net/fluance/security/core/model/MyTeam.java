package net.fluance.security.core.model;

import java.util.List;

import org.keycloak.representations.idm.UserRepresentation;

public class MyTeam {
	
	private UserRepresentation me;
	private List<UserRepresentation> colleagues;
	private UserRepresentation manager;
	private List<UserRepresentation> subordinates;
	
	public UserRepresentation getMe() {
		return me;
	}
	
	public void setMe(UserRepresentation me) {
		this.me = me;
	}
	
	public List<UserRepresentation> getColleagues() {
		return colleagues;
	}
	
	public void setColleagues(List<UserRepresentation> colleagues) {
		this.colleagues = colleagues;
	}
	
	public UserRepresentation getManager() {
		return manager;
	}
	
	public void setManager(UserRepresentation manager) {
		this.manager = manager;
	}
	
	public List<UserRepresentation> getSubordinates() {
		return subordinates;
	}
	
	public void setSubordinates(List<UserRepresentation> subordinates) {
		this.subordinates = subordinates;
	}
	
	
	 
}
