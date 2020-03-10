package net.fluance.security.ehprofile.service.impl;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import net.fluance.security.core.model.jpa.UserCompany;
import net.fluance.security.core.repository.jpa.IUserCompanyRepository;
import net.fluance.security.core.util.CompanyModificationAction;
import net.fluance.security.core.util.CompanyUtils;
import net.fluance.security.ehprofile.service.UserProfilePatientUnitService;

@Service
public class UserProfilePatientUnitServiceImpl implements UserProfilePatientUnitService {

	@Autowired
	private IUserCompanyRepository userCompanyRepository;
	
	/*
	 * (non-Javadoc)
	 * @see net.fluance.security.ehprofile.service.UserProfilePatientUnitService#grantPatientUnit(java.lang.String, java.lang.String, java.lang.Integer, java.lang.String)
	 */
	@Override
	@Transactional
	public boolean grantPatientUnit(String username, String domainName, Integer companyId, String patientUnit) throws Exception {
		return grantPatientUnits(username, domainName, companyId, Arrays.asList(patientUnit));
	}

	/*
	 * (non-Javadoc)
	 * @see net.fluance.security.ehprofile.service.UserProfilePatientUnitService#grantPatientUnits(java.lang.String, java.lang.String, java.lang.Integer, java.util.List)
	 */
	@Override
	@Transactional
	public boolean grantPatientUnits(String username, String domainName, Integer companyId, List<String> patientUnits) throws Exception {
		return updateUnits(username, domainName, companyId, patientUnits, CompanyModificationAction.GRANT);
	}

	/*
	 * (non-Javadoc)
	 * @see net.fluance.security.ehprofile.service.UserProfilePatientUnitService#setPatientUnits(java.lang.String, java.lang.String, java.lang.Integer, java.util.List)
	 */
	@Override
	@Transactional
	public boolean setPatientUnits(String username, String domainName, Integer companyId, List<String> patientUnits) throws Exception {
		return updateUnits(username, domainName, companyId, patientUnits, CompanyModificationAction.GRANT);
	}

	/*
	 * (non-Javadoc)
	 * @see net.fluance.security.ehprofile.service.UserProfilePatientUnitService#revokePatientUnit(java.lang.String, java.lang.String, java.lang.Integer, java.lang.String)
	 */
	@Override
	@Transactional
	public boolean revokePatientUnit(String username, String domainName, Integer companyId, String patientUnit) throws Exception {
		return revokePatientUnits(username, domainName, companyId, Arrays.asList(new String[] { patientUnit }));
	}

	/*
	 * (non-Javadoc)
	 * @see net.fluance.security.ehprofile.service.UserProfilePatientUnitService#revokePatientUnits(java.lang.String, java.lang.String, java.lang.Integer, java.util.List)
	 */
	@Override
	@Transactional
	public boolean revokePatientUnits(String username, String domainName, Integer companyId, List<String> patientUnits) throws Exception {
		return updateUnits(username, domainName, companyId, patientUnits, CompanyModificationAction.REVOKE);
	}
	
	/**
	 * Updates the PatientUnits for the user for the given company
	 * 
	 * @param username
	 * @param domainName
	 * @param companyId
	 * @param units {@link List} of {@link String}
	 * @param arrayName
	 * @param action
	 *            either GRANT, SET or REVOKE
	 * @return
	 * @throws JsonProcessingException
	 */
	@Transactional
	private boolean updateUnits(String username, String domainName, Integer companyId, List<String> units, CompanyModificationAction action) throws JsonProcessingException {
		if (units == null) {
			throw new IllegalArgumentException(CompanyUtils.PATIENTUNIT_JSON_PROPERTY + " array cannot be null");
				
		}
		
		if (units.isEmpty()) {
			return true;
		}
		
		UserCompany userCompany = userCompanyRepository.findByUsernameAndDomainNameAndCompanyId(username, domainName, companyId);
		if (userCompany != null) {
			JsonNode newServices = CompanyUtils.newUnitsOrServices(userCompany, units, CompanyUtils.PATIENTUNIT_JSON_PROPERTY, action);
			userCompany.setUnitsAndServices(newServices);
		} else {
			userCompany = CompanyUtils.newUserCompany(companyId, units, CompanyUtils.PATIENTUNIT_JSON_PROPERTY);
		}
		userCompany = userCompanyRepository.save(userCompany);
		return (userCompany != null);
	}

}
