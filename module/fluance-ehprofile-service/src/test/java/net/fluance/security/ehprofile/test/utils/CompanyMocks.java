package net.fluance.security.ehprofile.test.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import net.fluance.security.core.model.jdbc.Company;

public class CompanyMocks {
	private CompanyMocks() {}
	
	public static Company getCompany(Integer companyId) {
		Company company = new Company();
		
		company.setId(companyId);
		company.setCode("HOSP" + companyId);
		
		return company;
	}
	
	public static List<Company> getCompanies() {
		List<Company> companies = new ArrayList<>();
		
		IntStream.rangeClosed(1, 5)
    	.forEach(companyId -> {
    		companies.add(getCompany(companyId));
    	});
		
		return companies;
	}
}
