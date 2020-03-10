/**
 * 
 */
package net.fluance.security.core.util;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import net.fluance.app.test.AbstractTest;
import net.fluance.security.core.Application;

@ComponentScan("net.fluance.security.core")
@SpringBootTest(classes = Application.class)
public class CompanyHelperTest extends AbstractTest {
	
	private static final Logger LOGGER = LogManager.getLogger(CompanyHelperTest.class);

	private static final String[] COMPANY1_UNITS = new String[] { "01", "02", "03", "04", "05", "06", "07", "08", "09",
			"10", "CH", "DI", "HY", "LA", "OC", "RT", "RU" };
	private static final String[] COMPANY2_UNITS = new String[] { "1", "2", "2R", "IP", "P3", "P4" };
	private static final String[] COMPANY3_UNITS = new String[] { "01", "02", "03" };

	private static final String[] COMPANY1_SERVICES = new String[] { "1ANG", "1CAR", "1DER", "1INF", "1GYN", "1MGE",
			"1MIN", "1NEP", "1ORT", "1PED", "1PNE", "1DIA", "2CHI", "2DIE", "1HYP", "2HYP", "2LAB", "2ONC", "ONCO",
			"2RTH", "RTH", "2RHU" };
	private static final String[] COMPANY2_SERVICES = new String[] { "S1", "S2", "TAKL", "RES", "IPS", "PWIT", "PBAU",
			"AMBI", "THER" };
	private static final String[] COMPANY3_SERVICES = new String[] { "GALD", "GOD1", "GOD2", "GOD3", "GOFI", "INF",
			"PHY", "RAD", "PS" };

	private JsonNode company1UnitsAndServices;
	private JsonNode company2UnitsAndServices;
	private JsonNode company3UnitsAndServices;
	
	@PostConstruct
	public void init() {
		company1UnitsAndServices = CompanyUtils.unitsAndServicesJson(COMPANY1_UNITS, COMPANY1_SERVICES);
		company2UnitsAndServices = CompanyUtils.unitsAndServicesJson(COMPANY2_UNITS, COMPANY2_SERVICES);
		company3UnitsAndServices = CompanyUtils.unitsAndServicesJson(COMPANY3_UNITS, COMPANY3_SERVICES);
	}

	@Before
	public void setup() throws JsonProcessingException, IOException {
	}

	@After
	public void tearDown() {
	}

	@Test
	public void mustGenerateUnitsAndServicesJson() throws JsonProcessingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode company1UnitsAndServicesJson = (ObjectNode) mapper.readTree("{\"" + CompanyUtils.PATIENTUNIT_JSON_PROPERTY
				+ "\": [\"01\",\"02\",\"03\",\"04\",\"05\",\"06\",\"07\",\"08\",\"09\",\"10\",\"CH\",\"DI\",\"HY\",\"LA\",\"OC\",\"RT\",\"RU\"], \""
				+ CompanyUtils.HOSPSERVICE_JSON_PROPERTY
				+ "\" : [\"1ANG\",\"1CAR\",\"1DER\",\"1INF\",\"1GYN\",\"1MGE\",\"1MIN\",\"1NEP\",\"1ORT\",\"1PED\",\"1PNE\",\"1DIA\",\"2CHI\",\"2DIE\",\"1HYP\",\"2HYP\",\"2LAB\",\"2ONC\",\"ONCO\",\"2RTH\",\"RTH\",\"2RHU\"]}");
		JsonNode company2UnitsAndServicesJson = (ObjectNode) mapper.readTree("{\"" + CompanyUtils.PATIENTUNIT_JSON_PROPERTY
				+ "\": [\"1\",\"2\",\"2R\",\"IP\",\"P3\",\"P4\"], \""
				+ CompanyUtils.HOSPSERVICE_JSON_PROPERTY
				+ "\" : [\"S1\",\"S2\",\"TAKL\",\"RES\",\"IPS\",\"PWIT\",\"PBAU\",\"AMBI\",\"THER\"]}");
		JsonNode company3UnitsAndServicesJson = (ObjectNode) mapper.readTree("{\"" + CompanyUtils.PATIENTUNIT_JSON_PROPERTY
				+ "\": [\"01\",\"02\",\"03\"], \""
				+ CompanyUtils.HOSPSERVICE_JSON_PROPERTY
				+ "\" : [\"GALD\",\"GOD1\",\"GOD2\",\"GOD3\",\"GOFI\",\"INF\",\"PHY\",\"RAD\",\"PS\"]}");
		
		assertTrue(company1UnitsAndServices.equals(company1UnitsAndServicesJson));
		assertTrue(company2UnitsAndServices.equals(company2UnitsAndServicesJson));
		assertTrue(company3UnitsAndServices.equals(company3UnitsAndServicesJson));
	}
	
	@Test
	public void mustAddUnitsAndServices() throws JsonProcessingException {
		
		List<String> newUnits = Arrays.asList("UNIT_1", "UNIT_2", "UNIT_3");
		@SuppressWarnings("unchecked")
		List<String> modifiedUnitsCollection = (List<String>) CollectionUtils.union(CompanyUtils.patientUnitNames(company1UnitsAndServices), newUnits);
		List<String> newServices = Arrays.asList("SERVICE_1", "SERVICE_2", "SERVICE_3");
		@SuppressWarnings("unchecked")
		List<String> modifiedServicesCollection = (List<String>) CollectionUtils.union(CompanyUtils.hospServiceNames(company1UnitsAndServices), newServices);
		
		JsonNode newJsonNodeUnits = CompanyUtils.addUnitsOrServices(company1UnitsAndServices, newUnits, CompanyUtils.PATIENTUNIT_JSON_PROPERTY);
		JsonNode newUnitsJsonNodeServices = CompanyUtils.addUnitsOrServices(company1UnitsAndServices, newServices, CompanyUtils.HOSPSERVICE_JSON_PROPERTY);
		
		List<String> newJsonNodeUnitsList = CompanyUtils.patientUnitNames(newJsonNodeUnits); 
		List<String> newJsonNodeServicesList = CompanyUtils.hospServiceNames(newUnitsJsonNodeServices); 
		
		assertTrue(newJsonNodeUnitsList.containsAll(modifiedUnitsCollection) && modifiedUnitsCollection.containsAll(newJsonNodeUnitsList));
		assertTrue(newJsonNodeServicesList.containsAll(modifiedServicesCollection) && modifiedServicesCollection.containsAll(newJsonNodeServicesList));
		
	}

	@Test
	public void mustReplaceUnitsAndServices() throws JsonProcessingException {
		List<String> newUnits = Arrays.asList(new String[] {"LA", "OC", "RU"});
		List<String> newServices = Arrays.asList(new String[] {"1MIN", "1ORT"});
		
		JsonNode newJsonNodeUnits = CompanyUtils.replaceUnitsOrServices(company1UnitsAndServices, newUnits, CompanyUtils.PATIENTUNIT_JSON_PROPERTY);
		JsonNode newUnitsJsonNodeServices = CompanyUtils.replaceUnitsOrServices(company1UnitsAndServices, newServices, CompanyUtils.HOSPSERVICE_JSON_PROPERTY);
		
		List<String> newJsonNodeUnitsList = CompanyUtils.patientUnitNames(newJsonNodeUnits); 
		List<String> newJsonNodeServicesList = CompanyUtils.hospServiceNames(newUnitsJsonNodeServices); 
		
		assertTrue(newJsonNodeUnitsList.containsAll(newUnits) && newUnits.containsAll(newJsonNodeUnitsList));
		assertTrue(newJsonNodeServicesList.containsAll(newServices) && newServices.containsAll(newJsonNodeServicesList));
	}

	@Test
	public void mustRemoveUnitsAndServices() throws JsonProcessingException {
		
		List<String> removedUnits = Arrays.asList(new String[] {"LA", "OC", "RU"});
		@SuppressWarnings("unchecked")
		List<String> modifiedUnitsCollection = (List<String>) CollectionUtils.subtract(CompanyUtils.patientUnitNames(company1UnitsAndServices), removedUnits);
		List<String> removedServices = Arrays.asList(new String[] {"1MIN", "1ORT"});
		@SuppressWarnings("unchecked")
		List<String> modifiedServicesCollection = (List<String>) CollectionUtils.subtract(CompanyUtils.hospServiceNames(company1UnitsAndServices), removedServices);
		
		JsonNode newJsonNodeUnits = CompanyUtils.removeUnitsOrServices(company1UnitsAndServices, removedUnits, CompanyUtils.PATIENTUNIT_JSON_PROPERTY);
		JsonNode newUnitsJsonNodeServices = CompanyUtils.removeUnitsOrServices(company1UnitsAndServices, removedServices, CompanyUtils.HOSPSERVICE_JSON_PROPERTY);
		
		List<String> newJsonNodeUnitsList = CompanyUtils.patientUnitNames(newJsonNodeUnits); 
		List<String> newJsonNodeServicesList = CompanyUtils.hospServiceNames(newUnitsJsonNodeServices); 
		
		assertTrue(newJsonNodeUnitsList.containsAll(modifiedUnitsCollection) && modifiedUnitsCollection.containsAll(newJsonNodeUnitsList));
		assertTrue(newJsonNodeServicesList.containsAll(modifiedServicesCollection) && modifiedServicesCollection.containsAll(newJsonNodeServicesList));
		
	}
	
	
}
