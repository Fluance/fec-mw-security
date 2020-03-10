/**
 * 
 */
package net.fluance.security.permission.config;

import org.springframework.beans.factory.annotation.Value;

public class WebConfig {

	@Value("${permission.management.user}")
	private String permissionManagementUsername;
	@Value("${permission.management.password}")
	private String permissionManagementUserPassword;
	
	/**
	 * @return the permissionManagementUsername
	 */
	public String getPermissionManagementUsername() {
		return permissionManagementUsername;
	}

	/**
	 * @return the permissionManagementUserPassword
	 */
	public String getPermissionManagementUserPassword() {
		return permissionManagementUserPassword;
	}
	
}