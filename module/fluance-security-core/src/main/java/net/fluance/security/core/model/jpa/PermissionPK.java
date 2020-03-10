/**
 * 
 */
package net.fluance.security.core.model.jpa;

import java.io.Serializable;

@SuppressWarnings("serial")
public class PermissionPK implements Serializable {

	private Long actionId;
	private Long resourceId;
	private Long roleId;
	/**
	 * @return the accessId
	 */
	public Long getActionId() {
		return actionId;
	}
	/**
	 * @param accessId the accessId to set
	 */
	public void setActionId(Long actionId) {
		this.actionId = actionId;
	}
	/**
	 * @return the resourceId
	 */
	public Long getResourceId() {
		return resourceId;
	}
	/**
	 * @param resourceId the resourceId to set
	 */
	public void setResourceId(Long resourceId) {
		this.resourceId = resourceId;
	}
	/**
	 * @return the roleId
	 */
	public Long getRoleId() {
		return roleId;
	}
	/**
	 * @param roleId the roleId to set
	 */
	public void setRoleId(Long roleId) {
		this.roleId = roleId;
	}

}
