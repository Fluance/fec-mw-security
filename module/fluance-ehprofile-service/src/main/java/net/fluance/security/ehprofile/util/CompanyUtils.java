/**
 * 
 */
package net.fluance.security.ehprofile.util;

import java.util.ArrayList;
import java.util.List;

import net.fluance.app.data.model.identity.HospService;
import net.fluance.app.data.model.identity.PatientUnit;

/**
 *
 */
public class CompanyUtils {

	public static List<String> patientUnitsNames(List<PatientUnit> units) {
		List<String> patientunitsNames = new ArrayList<>();
		if(units != null) {
			for (PatientUnit patientUnit : units) {
				patientunitsNames.add(patientUnit.getCode());
			}
		}
		return patientunitsNames;
	}
	
	/**
	 * 
	 * @param services
	 * @return
	 */
	public static List<String> hospServicesNames(List<HospService> services) {
		List<String> hospServicesNames = new ArrayList<>();
		if(services != null) {
			for(HospService hospService : services) {
				hospServicesNames.add(hospService.getCode());
			}
		}
		return hospServicesNames;
	}
	
}
