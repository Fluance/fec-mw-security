/**
 * 
 */
package net.fluance.security.ehprofile.support.helper;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import net.fluance.app.data.model.identity.HospService;
import net.fluance.app.data.model.identity.PatientUnit;

/**
 *
 */
@Component
public class UserProfileHelper {
	
	/**
	 * Get patientunits names from a list of patientunits
	 * @param units
	 * @return
	 */
	public List<String> patientUnitsNames(List<PatientUnit> units) {
		List<String> patientunitsNames = new ArrayList<>();
		if(units != null) {
			for (PatientUnit patientUnit : units) {
				patientunitsNames.add(patientUnit.getCode());
			}
		}
		return patientunitsNames;
	}
	
	/**
	 * Get hospservices names from a list of hospservices
	 * @param services
	 * @return
	 */
	public List<String> hospServicesNames(List<HospService> services) {
		List<String> hospServicesNames = new ArrayList<>();
		if(services != null) {
			for(HospService hospService : services) {
				hospServicesNames.add(hospService.getCode());
			}
		}
		return hospServicesNames;
	}
	
}