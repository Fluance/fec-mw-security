package net.fluance.security.ehprofile.test.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import net.fluance.app.data.model.identity.CompanyStaffId;

public class CompanyStaffIdMocks {
	private CompanyStaffIdMocks() {}
	
	public static List<CompanyStaffId> getListOfCompanyStaffId(){
		List<CompanyStaffId> companyStaffIds = new ArrayList<>();
		
		IntStream.rangeClosed(1, 10)
        	.forEach(providerId -> {
        		CompanyStaffId companyStaffId = new CompanyStaffId();
        		companyStaffId.setProviderId(providerId);
        		companyStaffId.setStaffId("Staff"+providerId);
        		
        		companyStaffIds.add(companyStaffId);
        	});
		return companyStaffIds;
	}

}
