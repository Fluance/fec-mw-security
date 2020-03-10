package net.fluance.security.ehprofile.test.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import net.fluance.security.core.model.jpa.UserCompany;
import net.fluance.security.core.util.CompanyUtils;

public class UserCompanyMocks {
	private UserCompanyMocks() {}
	
	public static UserCompany getUserCompany(Integer companyId, Integer profileId) {
		UserCompany userCompany = new UserCompany();
		
		userCompany.setCompanyId(companyId);
		userCompany.setProfileId(profileId);
		
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode unitsAndServices = mapper.createObjectNode();
		ArrayNode patientUnitsArrayNode = mapper.createArrayNode();
		ArrayNode hospServicesArrayNode = mapper.createArrayNode();
		
		for (String patientUnit : PatientUnitMocks.getListOfPatientUnitsAsListOfStrings()) {
			patientUnitsArrayNode.add(patientUnit);
		}
		
		for (String hospService : HospServiceMocks.getListOfHospServiceAsListOfStrings()) {
			hospServicesArrayNode.add(hospService);
		}
		
		((ObjectNode) unitsAndServices).set(CompanyUtils.PATIENTUNIT_JSON_PROPERTY, patientUnitsArrayNode);
		((ObjectNode) unitsAndServices).set(CompanyUtils.HOSPSERVICE_JSON_PROPERTY, hospServicesArrayNode);
		
		userCompany.setUnitsAndServices(unitsAndServices);
		
		return userCompany;
	}
	
	public static List<UserCompany> getUserCompanies(Integer profileId) {
		List<UserCompany> userCompanies = new ArrayList<>();
		
		IntStream.rangeClosed(1, 5)
    	.forEach(companyId -> {
    		userCompanies.add(getUserCompany(companyId, profileId));
    	});
		
		return userCompanies;
	}
}
