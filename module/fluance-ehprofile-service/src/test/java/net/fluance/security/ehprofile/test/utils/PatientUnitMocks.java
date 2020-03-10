package net.fluance.security.ehprofile.test.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import net.fluance.app.data.model.identity.PatientUnit;

public class PatientUnitMocks {
	private PatientUnitMocks() {}
	
	public static List<PatientUnit>getListOfPatientUnits(){
		List<PatientUnit> patientUnits = new ArrayList<>();
		
		IntStream.rangeClosed(1, 10)
    	.forEach(id -> {
    		PatientUnit patientUnit = new PatientUnit();
    		patientUnit.setCode("UNIT"+id);
    		
    		patientUnits.add(patientUnit);
    	});
		
		return patientUnits;
	}
	
	public static List<String>getListOfPatientUnitsAsListOfStrings(){
		List<String> patientUnits = new ArrayList<>();
		
		IntStream.rangeClosed(1, 10)
    	.forEach(id -> {
    		patientUnits.add("UNIT"+id);
    	});
		
		return patientUnits;
	}
}
