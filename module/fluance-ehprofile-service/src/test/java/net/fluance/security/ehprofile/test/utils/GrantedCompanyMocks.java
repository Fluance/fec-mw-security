package net.fluance.security.ehprofile.test.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import net.fluance.app.data.model.identity.CompanyStaffId;
import net.fluance.app.data.model.identity.GrantedCompany;
import net.fluance.app.data.model.identity.HospService;
import net.fluance.app.data.model.identity.PatientUnit;

public class GrantedCompanyMocks {
	private GrantedCompanyMocks() {}
	
	public static List<GrantedCompany>getListOfGrantedCompany(){
		 List<GrantedCompany> grantedCompanies = new ArrayList<>();
		
		IntStream.rangeClosed(1, 10)
    	.forEach(companyId -> {
    		GrantedCompany grantedCompany = new GrantedCompany();
    		grantedCompany.setId(companyId);
    		grantedCompany.setCode("HOSP"+companyId);
    		
    		List<PatientUnit> patientUnits =PatientUnitMocks.getListOfPatientUnits();
    		List<HospService> hospServices = HospServiceMocks.getListOfHospService();
    		List<CompanyStaffId> staffIds = CompanyStaffIdMocks.getListOfCompanyStaffId();
    		
    		grantedCompany.setPatientunits(patientUnits);
    		grantedCompany.setHospservices(hospServices);
    		grantedCompany.setStaffIds(staffIds);
    		
    		grantedCompanies.add(grantedCompany);
    	});
		
		return grantedCompanies;
	}
}
