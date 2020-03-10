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
import net.fluance.security.ehprofile.service.UserProfileHospServiceService;

@Service
public class UserProfileHospServiceServiceImpl implements UserProfileHospServiceService {

	@Autowired
	private IUserCompanyRepository userCompanyRepository;
	
	/*
	 * (non-Javadoc)
	 * @see net.fluance.security.ehprofile.service.UserProfileHospServiceService#grantHospService(java.lang.String, java.lang.String, java.lang.Integer, java.lang.String)
	 */
	@Override
	@Transactional
	public boolean grantHospService(String username, String domainName, Integer companyId, String hospService) throws Exception {
		return grantHospServices(username, domainName, companyId, Arrays.asList(hospService));
	}

	/*
	 * (non-Javadoc)
	 * @see net.fluance.security.ehprofile.service.UserProfileHospServiceService#grantHospServices(java.lang.String, java.lang.String, java.lang.Integer, java.util.List)
	 */
	@Override
	@Transactional
	public boolean grantHospServices(String username, String domainName, Integer companyId, List<String> hospServices) throws Exception {
		return updateServices(username, domainName, companyId, hospServices, CompanyModificationAction.GRANT);
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.fluance.security.ehprofile.service.UserProfileHospServiceService#setHospServices(java.lang.String, java.lang.String, java.lang.Integer, java.util.List)
	 */
	@Override
	@Transactional
	public boolean setHospServices(String username, String domainName, Integer companyId, List<String> hospServices) throws Exception {
		return updateServices(username, domainName, companyId, hospServices, CompanyModificationAction.SET);
	}

	/*
	 * (non-Javadoc)
	 * @see net.fluance.security.ehprofile.service.UserProfileHospServiceService#revokeHospService(java.lang.String, java.lang.String, java.lang.Integer, java.lang.String)
	 */
	@Override
	@Transactional
	public boolean revokeHospService(String username, String domainName, Integer companyId, String hospService) throws Exception {
		return revokeHospServices(username, domainName, companyId, Arrays.asList(new String[] { hospService }));
	}

	/*
	 * (non-Javadoc)
	 * @see net.fluance.security.ehprofile.service.UserProfileHospServiceService#revokeHospServices(java.lang.String, java.lang.String, java.lang.Integer, java.util.List)
	 */
	@Override
	@Transactional
	public boolean revokeHospServices(String username, String domainName, Integer companyId, List<String> hospServices) throws Exception {
		return updateServices(username, domainName, companyId, hospServices, CompanyModificationAction.REVOKE);
	}
	
	/**
	 * Updates the HospServices for the user for the given company
	 * 
	 * @param username
	 * @param domainName
	 * @param companyId
	 * @param services {@link List} of {@link String}
	 * @param arrayName
	 * @param action
	 *            either GRANT, SET or REVOKE
	 * @return
	 * @throws JsonProcessingException
	 */
	@Transactional
	private boolean updateServices(String username, String domainName, Integer companyId, List<String> services, CompanyModificationAction action) throws JsonProcessingException {
		if (services == null) {
			throw new IllegalArgumentException(CompanyUtils.HOSPSERVICE_JSON_PROPERTY + " array cannot be null");
		}
		
		if(services.isEmpty()) {
			return true;
		}
		
		UserCompany userCompany = userCompanyRepository.findByUsernameAndDomainNameAndCompanyId(username, domainName, companyId);
		if (userCompany != null) {
			JsonNode newServices = CompanyUtils.newUnitsOrServices(userCompany, services, CompanyUtils.HOSPSERVICE_JSON_PROPERTY, action);
			userCompany.setUnitsAndServices(newServices);
		} else {
			userCompany = CompanyUtils.newUserCompany(companyId, services, CompanyUtils.HOSPSERVICE_JSON_PROPERTY);
		}
		userCompany = userCompanyRepository.save(userCompany);
		return (userCompany != null);
	}

}
