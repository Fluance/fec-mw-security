package net.fluance.security.ehprofile.test.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import net.fluance.app.data.model.identity.HospService;

public class HospServiceMocks {
	private HospServiceMocks() {}
	
	public static List<HospService>getListOfHospService(){
		List<HospService> hospServices = new ArrayList<>();
		
		IntStream.rangeClosed(1, 10)
    	.forEach(id -> {
    		HospService hospService = new HospService();
    		hospService.setCode("HospServ"+id);
    		
    		hospServices.add(hospService);
    	});
		
		return hospServices;
	}
	
	public static List<String>getListOfHospServiceAsListOfStrings(){
		List<String> hospServices = new ArrayList<>();
		
		IntStream.rangeClosed(1, 10)
    	.forEach(id -> {
    		hospServices.add("HospServ"+id);
    	});
		
		return hospServices;
	}
}
