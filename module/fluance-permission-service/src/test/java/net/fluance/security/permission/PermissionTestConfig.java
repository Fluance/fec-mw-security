/**
 * 
 */
package net.fluance.security.permission;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan("net.fluance.security.permission")
@EnableJpaRepositories
@EnableAutoConfiguration
@PropertySource({"classpath:test.properties","classpath:webapps/conf/security.properties"})
public class PermissionTestConfig {
	
	@Value("${permitted.role1}")
	private String permittedRole1;
	@Value("${permitted.role2}")
	private String permittedRole2;
	@Value("${unpermitted.role1}")
	private String unpermittedRole1;
	@Value("${unpermitted.role2}")
	private String unpermittedRole2;
	@Value("${permitted.action}")
	private String permittedAction;
	@Value("${unpermitted.action1}")
	private String unpermittedAction1;
	@Value("${unpermitted.action2}")
	private String unpermittedAction2;
	@Value("${resource.not-applicable}")
	private String resourceNotApplicable;
	@Value("${resource.applicable}")
	private String resourceApplicable;
	@Value("${xacml.pdp.response.permit}")
	private String xacmlPdpResponsePermit;
	@Value("${xacml.pdp.response.deny}")
	private String xacmlPdpResponseDeny;
	@Value("${xacml.pdp.response.indeterminate}")
	private String xacmlPdpResponseIndeterminate;
	@Value("${xacml.pdp.response.not-applicable}")
	private String xacmlPdpResponseNotApplicable;

	
	@Value("${permission.request.content.type}")
	private String requestContentType;
	@Value("${permission.request.charset}")
	private String requestCharset;	
	
	@Value("${swagger.specs.location}")
	private String specsLocation;
	@Value("${swagger.permission.spec.file}")
	private String permissionSpecFile;	

	/**
	 * @return the requestContentType
	 */
	public String getRequestContentType() {
		return requestContentType;
	}

	/**
	 * @return the requestCharset
	 */
	public String getRequestCharset() {
		return requestCharset;
	}

	/**
	 * @return the permittedRole1
	 */
	public String getPermittedRole1() {
		return permittedRole1;
	}

	/**
	 * @return the permittedRole2
	 */
	public String getPermittedRole2() {
		return permittedRole2;
	}

	/**
	 * @return the unpermittedRole1
	 */
	public String getUnpermittedRole1() {
		return unpermittedRole1;
	}

	/**
	 * @return the unpermittedRole2
	 */
	public String getUnpermittedRole2() {
		return unpermittedRole2;
	}

	/**
	 * @return the permittedAction
	 */
	public String getPermittedAction() {
		return permittedAction;
	}

	/**
	 * @return the unpermittedAction1
	 */
	public String getUnpermittedAction1() {
		return unpermittedAction1;
	}

	/**
	 * @return the unpermittedAction2
	 */
	public String getUnpermittedAction2() {
		return unpermittedAction2;
	}

	/**
	 * @return the resourceNotApplicable
	 */
	public String getResourceNotApplicable() {
		return resourceNotApplicable;
	}

	/**
	 * @return the resourceApplicable
	 */
	public String getResourceApplicable() {
		return resourceApplicable;
	}

	/**
	 * @return the xacmlPdpResponsePermit
	 */
	public String getXacmlPdpResponsePermit() {
		return xacmlPdpResponsePermit;
	}

	/**
	 * @return the xacmlPdpResponseDeny
	 */
	public String getXacmlPdpResponseDeny() {
		return xacmlPdpResponseDeny;
	}

	/**
	 * @return the xacmlPdpResponseIndeterminate
	 */
	public String getXacmlPdpResponseIndeterminate() {
		return xacmlPdpResponseIndeterminate;
	}

	/**
	 * @return the xacmlPdpResponseNotApplicable
	 */
	public String getXacmlPdpResponseNotApplicable() {
		return xacmlPdpResponseNotApplicable;
	}

	/**
	 * @return the specsLocation
	 */
	public String getSpecsLocation() {
		return specsLocation;
	}

	/**
	 * @return the permissionSpecFile
	 */
	public String getPermissionSpecFile() {
		return permissionSpecFile;
	}

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertyConfig() {
		return new PropertySourcesPlaceholderConfigurer();
	}
	
}
