/**
 * 
 */
package net.fluance.security.core.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines common and re-usable mapping properties
 *
 */
public class MappingsConfig {

	// Maps a domain class name with the correponding table name in the database
	public static final Map<String, String> TABLE_NAMES = new HashMap<>();
	
	static {
		TABLE_NAMES.put("Company", "company");
		TABLE_NAMES.put("Profile", "profile");
		TABLE_NAMES.put("Domain", "um_domain");
		TABLE_NAMES.put("Physician", "physician");
		TABLE_NAMES.put("UserCompany", "user_company");
		TABLE_NAMES.put("UserCompanyPatientunit", "user_company_patientunit");
		TABLE_NAMES.put("UserCompanyHospservice", "user_company_hospservice");
		TABLE_NAMES.put("UserClientData", "user_client_data");
		TABLE_NAMES.put("Client", "client");
	}
	
}
