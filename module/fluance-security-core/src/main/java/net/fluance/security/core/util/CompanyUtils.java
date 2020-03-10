/**
 * 
 */
package net.fluance.security.core.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.IteratorUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import net.fluance.app.data.model.identity.HospService;
import net.fluance.app.data.model.identity.PatientUnit;
import net.fluance.commons.json.JacksonUtils;
import net.fluance.security.core.model.jpa.UserCompany;
import net.fluance.security.core.model.jpa.UserCompanyIdentity;

public class CompanyUtils {

	public static final String PATIENTUNIT_JSON_PROPERTY = "patientunit";
	public static final String HOSPSERVICE_JSON_PROPERTY = "hospservice";

	/**
	 * 
	 * @param units
	 * @return
	 */
	public static List<String> patientUnitsNames(List<PatientUnit> units) {
		List<String> patientunitsNames = new ArrayList<>();
		if (units != null) {
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
		if (services != null) {
			for (HospService hospService : services) {
				hospServicesNames.add(hospService.getCode());
			}
		}
		return hospServicesNames;
	}

	/**
	 * 
	 * @param json
	 * @return
	 * @throws JsonProcessingException
	 */
	public static List<PatientUnit> patientUnits(JsonNode json) throws JsonProcessingException {
		List<PatientUnit> patientUnits = new ArrayList<>();

		if (json == null) {
			return null;
		}
		if (!json.has(PATIENTUNIT_JSON_PROPERTY) || !(json.get(PATIENTUNIT_JSON_PROPERTY) instanceof ArrayNode)) {
			String jsonStr = new ObjectMapper().writeValueAsString(json);
			throw new IllegalArgumentException(jsonStr + " is not a valid container for units and services");
		}

		ArrayNode unitsArray = (ArrayNode) json.get(PATIENTUNIT_JSON_PROPERTY);
		for (int i = 0; i < unitsArray.size(); i++) {
			PatientUnit patientUnit = new PatientUnit(unitsArray.get(i).textValue());
			patientUnits.add(patientUnit);
		}

		return patientUnits;
	}

	/**
	 * 
	 * @param json
	 * @return
	 * @throws JsonProcessingException
	 */
	public static List<HospService> hospServices(JsonNode json) throws JsonProcessingException {
		List<HospService> hospServices = new ArrayList<>();

		if (json == null) {
			return null;
		}
		if (!json.has(HOSPSERVICE_JSON_PROPERTY) || !(json.get(HOSPSERVICE_JSON_PROPERTY) instanceof ArrayNode)) {
			String jsonStr = new ObjectMapper().writeValueAsString(json);
			throw new IllegalArgumentException(jsonStr + " is not a valid container for units and services");
		}

		ArrayNode servicesArray = (ArrayNode) json.get(HOSPSERVICE_JSON_PROPERTY);
		for (int i = 0; i < servicesArray.size(); i++) {
			HospService hospService = new HospService(servicesArray.get(i).textValue());
			hospServices.add(hospService);
		}

		return hospServices;
	}

	/**
	 * 
	 * @param json
	 * @return
	 * @throws JsonProcessingException
	 */
	public static List<String> patientUnitNames(JsonNode json) throws JsonProcessingException {
		List<String> patientUnits = new ArrayList<>();

		if (json == null) {
			return null;
		}
		if (!json.has(PATIENTUNIT_JSON_PROPERTY) || !(json.get(PATIENTUNIT_JSON_PROPERTY) instanceof ArrayNode)) {
			String jsonStr = new ObjectMapper().writeValueAsString(json);
			throw new IllegalArgumentException(jsonStr + " is not a valid container for units and services");
		}

		ArrayNode unitsArray = (ArrayNode) json.get(PATIENTUNIT_JSON_PROPERTY);
		for (int i = 0; i < unitsArray.size(); i++) {
			patientUnits.add(unitsArray.get(i).textValue());
		}

		return patientUnits;
	}

	/**
	 * 
	 * @param json
	 * @return
	 * @throws JsonProcessingException
	 */
	public static List<String> hospServiceNames(JsonNode json) throws JsonProcessingException {
		List<String> hospServices = new ArrayList<>();

		if (json == null) {
			return null;
		}
		if (!json.has(HOSPSERVICE_JSON_PROPERTY) || !(json.get(HOSPSERVICE_JSON_PROPERTY) instanceof ArrayNode)) {
			String jsonStr = new ObjectMapper().writeValueAsString(json);
			throw new IllegalArgumentException(jsonStr + " is not a valid container for units and services");
		}

		ArrayNode servicesArray = (ArrayNode) json.get(HOSPSERVICE_JSON_PROPERTY);
		for (int i = 0; i < servicesArray.size(); i++) {
			hospServices.add(servicesArray.get(i).textValue());
		}

		return hospServices;
	}

	/**
	 * 
	 * @param patientUnits
	 * @param hospServices
	 * @return
	 */
	public static ObjectNode unitsAndServicesJson(String[] patientUnits, String[] hospServices) {
		return unitsAndServicesJson(Arrays.asList(patientUnits), Arrays.asList(hospServices));
	}

	/**
	 * 
	 * @param patientUnits
	 * @param hospServices
	 * @return
	 */
	public static ObjectNode unitsAndServicesJson(List<String> patientUnits, List<String> hospServices) {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode unitsAndServicesNode = mapper.createObjectNode();

		// Populate the hospservices and patient units arrays with data from
		// arguments
		ArrayNode patientUnitsArrayNode = mapper.createArrayNode();
		for (String patientUnit : patientUnits) {
			patientUnitsArrayNode.add(patientUnit);
		}
		ArrayNode hospServicesArrayNode = mapper.createArrayNode();
		for (String hospService : hospServices) {
			hospServicesArrayNode.add(hospService);
		}

		unitsAndServicesNode.set(PATIENTUNIT_JSON_PROPERTY, patientUnitsArrayNode);
		unitsAndServicesNode.set(HOSPSERVICE_JSON_PROPERTY, hospServicesArrayNode);

		return unitsAndServicesNode;
	}

	/**
	 * 
	 * @param initialJson
	 * @param toAdd
	 * @param arrayFieldName
	 * @return
	 */
	public static JsonNode addUnitsOrServices(JsonNode initialJson, List<String> toAdd, String arrayFieldName) {
		ArrayNode currentArrayNode = (ArrayNode) initialJson.get(arrayFieldName);

		Iterator<JsonNode> unitsIterator = currentArrayNode.elements();
		@SuppressWarnings("unchecked")
		List<String> currentElements = JacksonUtils.textNodesToStringsList(IteratorUtils.toList(unitsIterator));

		// Intersection of the 2 collections
		@SuppressWarnings("unchecked")
		List<String> alreadyExistingUnits = (List<String>) CollectionUtils.intersection(currentElements, toAdd);

		@SuppressWarnings("unchecked")
		List<String> actualElementsToAdd = (List<String>) CollectionUtils.subtract(toAdd, alreadyExistingUnits);

		for (String unit : actualElementsToAdd) {
			currentArrayNode.add(unit);
		}

		@SuppressWarnings({ "unchecked" })
		List<String> currentElementsAfterAdd = JacksonUtils.textNodesToStringsList(
				IteratorUtils.toList(currentArrayNode.elements()));

		boolean rightNbElts = currentArrayNode.size() == (currentElements.size() + actualElementsToAdd.size());
		boolean allPreviousEltsPresent = currentElementsAfterAdd.containsAll(currentElements);
		boolean allNewEltsPresent = currentElementsAfterAdd.containsAll(actualElementsToAdd);

		if ((currentArrayNode.size() < 0) || !rightNbElts || !allPreviousEltsPresent || !allNewEltsPresent) {
			throw new IllegalStateException(
					"New array must contain " + CollectionUtils.union(currentElements, actualElementsToAdd)
							+ " but contains " + currentElementsAfterAdd);
		}

		return initialJson;
	}

	/**
	 * 
	 * @param initialJson
	 * @param newElements
	 * @param arrayFieldName
	 * @return
	 */
	public static JsonNode replaceUnitsOrServices(JsonNode initialJson, List<String> newElements,
			String arrayFieldName) {

		if (newElements == null) {
			throw new IllegalArgumentException("New set of elements cannot be null");
		}

		ArrayNode currentArrayNode = (ArrayNode) initialJson.get(arrayFieldName);

		currentArrayNode.removeAll();
		for (String unit : newElements) {
			currentArrayNode.add(unit);
		}

		@SuppressWarnings({ "unchecked" })
		List<String> currentElementsAfterReplace = JacksonUtils.textNodesToStringsList(
				IteratorUtils.toList(currentArrayNode.elements()));
		boolean allNewEltsPresent = currentElementsAfterReplace.containsAll(newElements);
		if (currentArrayNode.size() < 0 || currentArrayNode.size() != newElements.size() || !allNewEltsPresent) {
			throw new IllegalStateException(
					"New array must contain " + newElements + " but contains " + currentElementsAfterReplace);
		}

		return initialJson;
	}

	/**
	 * 
	 * @param initialJson
	 * @param toRemove
	 * @param arrayFieldName
	 * @return
	 * @throws JsonProcessingException
	 */
	public static JsonNode removeUnitsOrServices(JsonNode initialJson, List<String> toRemove, String arrayFieldName)
			throws JsonProcessingException {
		ArrayNode currentArrayNode = (ArrayNode) initialJson.get(arrayFieldName);

		ArrayNode newArrayNode = JacksonUtils.removeFromArrayNode(currentArrayNode, toRemove);
		
		((ObjectNode)initialJson).set(arrayFieldName, newArrayNode);
		
		currentArrayNode = (ArrayNode) initialJson.get(arrayFieldName);

		return initialJson;
	}

	/**
	 * 
	 * @param companyId
	 * @param userCompanies
	 * @return
	 */
	public static UserCompany companyById(int companyId, List<UserCompany> userCompanies) {
		for(UserCompany userCompany : userCompanies) {
			if(companyId == userCompany.getCompanyId()) {
				return userCompany;
			}
		}
		return null;
	}

	/**
	 * 
	 * @param companyId
	 * @param userCompanies
	 * @return
	 */
	public static List<UserCompanyIdentity> companyIdentityByCompanyId(int companyId, List<UserCompanyIdentity> userCompanyIdentities) {
		List<UserCompanyIdentity> identities = new ArrayList<>();
		for(UserCompanyIdentity userCompanyIdentity : userCompanyIdentities) {
			if(companyId == userCompanyIdentity.getCompanyId()) {
				identities.add(userCompanyIdentity);
			}
		}
		return identities;
	}

	public static UserCompanyIdentity companyIdentityByCompanyIdAndProvider(int companyId, List<UserCompanyIdentity> userCompanyIdentities) {
		for(UserCompanyIdentity userCompanyIdentity : userCompanyIdentities) {
			if(companyId == userCompanyIdentity.getCompanyId()) {
				return userCompanyIdentity;
			}
		}
		return null;
	}
	
	/**
	 * Creates a {@link JsonNode} object for granting, setting o delete/revoke the HospService or PatientUnits
	 * 
	 * @param companyId
	 * @param units
	 * @return
	 * @throws JsonProcessingException
	 */
	public static JsonNode newUnitsOrServices(UserCompany userCompany, List<String> servicesOrUnits, String arrayName, CompanyModificationAction action) throws JsonProcessingException {
		if (!CompanyUtils.PATIENTUNIT_JSON_PROPERTY.equals(arrayName) && !CompanyUtils.HOSPSERVICE_JSON_PROPERTY.equals(arrayName)) {
			throw new IllegalArgumentException(arrayName + " is not a valid property name");
		}
		switch (action) {
			case GRANT:
				return CompanyUtils.addUnitsOrServices(userCompany.getUnitsAndServices(), servicesOrUnits, arrayName);
			case SET:
				return CompanyUtils.replaceUnitsOrServices(userCompany.getUnitsAndServices(), servicesOrUnits, arrayName);
			case REVOKE:
				return CompanyUtils.removeUnitsOrServices(userCompany.getUnitsAndServices(), servicesOrUnits, arrayName);
			default:
				throw new IllegalArgumentException(action.name() + " is not a valid action");
		}
	}
	
	/**
	 * Creates a {@link UserCompany} and set the HospServices or the PatientUnits depending of the arrayName
	 *
	 * @param companyId
	 * @param servicesOrUnits
	 * @param arrayName
	 * @return
	 */
	public static UserCompany newUserCompany(Integer companyId, List<String> servicesOrUnits, String arrayName) {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode services = mapper.createObjectNode();
		ArrayNode servicesOrUnitsNode = mapper.createArrayNode();
		
		if (servicesOrUnits != null) {
			for (String serviceOrUnit : servicesOrUnits) {
				servicesOrUnitsNode.add(serviceOrUnit);
			}
		}
		
		((ObjectNode) services).set(arrayName, servicesOrUnitsNode);
		UserCompany userCompany = new UserCompany(companyId, null, services);
		return userCompany;
	}
}
