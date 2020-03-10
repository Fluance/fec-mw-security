/**
 * 
 */
package net.fluance.security.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;


@Configuration
@ComponentScan(basePackages = {"net.fluance.security.core", "net.fluance.security.client"})
@EnableAutoConfiguration
@PropertySource({"classpath:test.properties","classpath:webapps/conf/security.properties"})
public class UserProfileTestConfig {
	
	@Value("${swagger.specs.location}")
	private String specsLocation;
	@Value("${swagger.userprofile.spec.file}")
	private String userProfileSpecFile;
	@Value("${user.profile.expected.username}")
	private String expectedUsername;
	@Value("${user.profile.expected.domain}")
	private String expectedDomain;
	@Value("${user.profile.unexpected.username}")
	private String unexpectedUsername;
	@Value("${user.profile.unexpected.domain}")
	private String unexpectedDomain;
	@Value("${user.profile.create.request.payload}")
	private String createProfileRequestPayload;
	@Value("${user.profile.create.nonadmin.request.payload}")
	private String createNonAdminProfileRequestPayload;
	@Value("${user.profile.create.response.message}")
	private String createProfileResponseMessage;
	@Value("${user.profile.create.response.invalidusertype.message}")
	private String createProfileInvalidUserTypeResponseMessage;
	@Value("${user.profile.delete.response.message}")
	private String deleteProfileResponseMessage;
	@Value("${user.profile.companies.grant.request.payload}")
	private String grantCompaniesRequestPayload;
	@Value("${user.profile.companies.grant.response.message}")
	private String grantCompaniesResponseMessage;
	@Value("${user.profile.companies.set.request.payload}")
	private String setCompaniesRequestPayload;
	@Value("${user.profile.companies.set.response.message}")
	private String setCompaniesResponseMessage;
	@Value("${user.profile.companies.revoke.request.payload}")
	private String revokeCompaniesRequestPayload;
	@Value("${user.profile.companies.revoke.response.message}")
	private String revokeCompaniesResponseMessage;
	@Value("${user.profile.patientunits.grant.request.payload}")
	private String grantPatientUnitsRequestPayload;
	@Value("${user.profile.patientunits.grant.response.message}")
	private String grantPatientUnitsResponseMessage;
	@Value("${user.profile.patientunits.set.request.payload}")
	private String setPatientUnitsRequestPayload;
	@Value("${user.profile.patientunits.set.response.message}")
	private String setPatientUnitsResponseMessage;
	@Value("${user.profile.patientunits.revoke.request.payload}")
	private String revokePatientUnitsRequestPayload;
	@Value("${user.profile.patientunits.revoke.response.message}")
	private String revokePatientUnitsResponseMessage;
	@Value("${user.profile.hospservices.grant.request.payload}")
	private String grantHospServicesRequestPayload;
	@Value("${user.profile.hospservices.grant.response.message}")
	private String grantHospServicesResponseMessage;
	@Value("${user.profile.hospservices.set.request.payload}")
	private String setHospServicesRequestPayload;
	@Value("${user.profile.hospservices.set.response.message}")
	private String setHospServicesResponseMessage;
	@Value("${user.profile.hospservices.revoke.request.payload}")
	private String revokeHospServicesRequestPayload;
	@Value("${user.profile.hospservices.revoke.response.message}")
	private String revokeHospServicesResponseMessage;
	@Value("${user.profile.roles.grant.request.payload}")
	private String grantRolesRequestPayload;
	@Value("${user.profile.roles.grant.response.message}")
	private String grantRolesResponseMessage;
	@Value("${user.profile.roles.set.request.payload}")
	private String setRolesRequestPayload;
	@Value("${user.profile.roles.set.response.message}")
	private String setRolesResponseMessage;
	@Value("${user.profile.roles.revoke.request.payload}")
	private String revokeRolesRequestPayload;
	@Value("${user.profile.roles.revoke.response.message}")
	private String revoRolesResponseMessage;
	@Value("${user.profile.request.content.type}")
	private String requestContentType;
	@Value("${user.profile.request.charset}")
	private String requestCharset;	
	
	/**
	 * @return the specsLocation
	 */
	public String getSpecsLocation() {
		return specsLocation;
	}

	/**
	 * @return the userProfileSpecFile
	 */
	public String getUserProfileSpecFile() {
		return userProfileSpecFile;
	}

	/**
	 * @return the expectedUsername
	 */
	public String getExpectedUsername() {
		return expectedUsername;
	}

	/**
	 * @return the expectedDomain
	 */
	public String getExpectedDomain() {
		return expectedDomain;
	}

	/**
	 * @return the unexpectedUsername
	 */
	public String getUnexpectedUsername() {
		return unexpectedUsername;
	}

	/**
	 * @return the unexpectedDomain
	 */
	public String getUnexpectedDomain() {
		return unexpectedDomain;
	}

	public String getCreateProfileInvalidUserTypeResponseMessage() {
		return createProfileInvalidUserTypeResponseMessage;
	}

	/**
	 * @return the createProfileRequestPayload
	 */
	public String getCreateProfileRequestPayload() {
		return createProfileRequestPayload;
	}

	/**
	 * @return the createProfileResponseMessage
	 */
	public String getCreateProfileResponseMessage() {
		return createProfileResponseMessage;
	}

	/**
	 * @return the deleteProfileResponseMessage
	 */
	public String getDeleteProfileResponseMessage() {
		return deleteProfileResponseMessage;
	}

	/**
	 * @return the grantCompaniesRequestPayload
	 */
	public String getGrantCompaniesRequestPayload() {
		return grantCompaniesRequestPayload;
	}

	/**
	 * @return the grantCompaniesResponseMessage
	 */
	public String getGrantCompaniesResponseMessage() {
		return grantCompaniesResponseMessage;
	}

	/**
	 * @return the setCompaniesRequestPayload
	 */
	public String getSetCompaniesRequestPayload() {
		return setCompaniesRequestPayload;
	}

	/**
	 * @return the setCompaniesResponseMessage
	 */
	public String getSetCompaniesResponseMessage() {
		return setCompaniesResponseMessage;
	}

	/**
	 * @return the revokeCompaniesRequestPayload
	 */
	public String getRevokeCompaniesRequestPayload() {
		return revokeCompaniesRequestPayload;
	}

	/**
	 * @return the revokeCompaniesResponseMessage
	 */
	public String getRevokeCompaniesResponseMessage() {
		return revokeCompaniesResponseMessage;
	}

	/**
	 * @return the grantPatientUnitsRequestPayload
	 */
	public String getGrantPatientUnitsRequestPayload() {
		return grantPatientUnitsRequestPayload;
	}

	/**
	 * @return the grantPatientUnitsResponseMessage
	 */
	public String getGrantPatientUnitsResponseMessage() {
		return grantPatientUnitsResponseMessage;
	}

	/**
	 * @return the setPatientUnitsRequestPayload
	 */
	public String getSetPatientUnitsRequestPayload() {
		return setPatientUnitsRequestPayload;
	}

	/**
	 * @return the setPatientUnitsResponseMessage
	 */
	public String getSetPatientUnitsResponseMessage() {
		return setPatientUnitsResponseMessage;
	}

	/**
	 * @return the revokePatientUnitsRequestPayload
	 */
	public String getRevokePatientUnitsRequestPayload() {
		return revokePatientUnitsRequestPayload;
	}

	/**
	 * @return the revokePatientUnitsResponseMessage
	 */
	public String getRevokePatientUnitsResponseMessage() {
		return revokePatientUnitsResponseMessage;
	}

	/**
	 * @return the grantHospServicesRequestPayload
	 */
	public String getGrantHospServicesRequestPayload() {
		return grantHospServicesRequestPayload;
	}

	/**
	 * @return the grantHospServicesResponseMessage
	 */
	public String getGrantHospServicesResponseMessage() {
		return grantHospServicesResponseMessage;
	}

	/**
	 * @return the setHospServicesRequestPayload
	 */
	public String getSetHospServicesRequestPayload() {
		return setHospServicesRequestPayload;
	}

	/**
	 * @return the setHospServicesResponseMessage
	 */
	public String getSetHospServicesResponseMessage() {
		return setHospServicesResponseMessage;
	}

	/**
	 * @return the revokeHospServicesRequestPayload
	 */
	public String getRevokeHospServicesRequestPayload() {
		return revokeHospServicesRequestPayload;
	}

	/**
	 * @return the revokeHospServicesResponseMessage
	 */
	public String getRevokeHospServicesResponseMessage() {
		return revokeHospServicesResponseMessage;
	}

	/**
	 * @return the grantRolesRequestPayload
	 */
	public String getGrantRolesRequestPayload() {
		return grantRolesRequestPayload;
	}

	/**
	 * @return the grantRolesResponseMessage
	 */
	public String getGrantRolesResponseMessage() {
		return grantRolesResponseMessage;
	}

	/**
	 * @return the setRolesRequestPayload
	 */
	public String getSetRolesRequestPayload() {
		return setRolesRequestPayload;
	}

	/**
	 * @return the setRolesResponseMessage
	 */
	public String getSetRolesResponseMessage() {
		return setRolesResponseMessage;
	}

	/**
	 * @return the revokeRolesRequestPayload
	 */
	public String getRevokeRolesRequestPayload() {
		return revokeRolesRequestPayload;
	}

	/**
	 * @return the revoRolesResponseMessage
	 */
	public String getRevoRolesResponseMessage() {
		return revoRolesResponseMessage;
	}
	
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
	 * @return the createNonAdminProfileRequestPayload
	 */
	public String getCreateNonAdminProfileRequestPayload() {
		return createNonAdminProfileRequestPayload;
	}

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertyConfig() {
		return new PropertySourcesPlaceholderConfigurer();
	}
	
}
