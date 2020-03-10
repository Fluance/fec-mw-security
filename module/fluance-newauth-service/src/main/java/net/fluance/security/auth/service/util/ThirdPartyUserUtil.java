package net.fluance.security.auth.service.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import net.fluance.app.data.model.identity.ThirdPartyUserReference;
import net.fluance.app.data.model.identity.User;
import net.fluance.security.auth.config.helper.jwt.JWTValues;

/**
 * Utils for work with the Third Party User data
 */
public class ThirdPartyUserUtil {
	private static final Logger logger = LogManager.getLogger(ThirdPartyUserUtil.class);
	
	/**
	 * Maps the given {@link JsonNode} to an objet {@link ThirdPartyUserReference}
	 * 
	 * @param node
	 * @return
	 */
	public static ThirdPartyUserReference parseFromJson(JsonNode node) {
		ThirdPartyUserReference thirdPartyUser = null;
		
		if(node !=null) {
			ObjectMapper mapper = new ObjectMapper();
			
			try {
				thirdPartyUser = mapper.treeToValue(node, ThirdPartyUserReference.class);
			} catch (JsonProcessingException e) {
				logger.warn("Unparseable value to ThirdPartyUserReference object");
			}
		}
		
		return thirdPartyUser;
	}
	
	/**
	 * Adds to the given payload the third party information from the user
	 * 
	 * @param user
	 * @param payload
	 */
	public static void addThirdPartyUserToPayload(User user, ObjectNode payload) {
		if(user.getThirdPartyUser() != null && user.getThirdPartyUser().getActualUserName() != null) {
			ObjectMapper mapper = new ObjectMapper();
			ObjectNode thirdPartyUser = mapper.createObjectNode();
			
			thirdPartyUser.put(JWTValues.ACTUAL_USERNAME, user.getThirdPartyUser().getActualUserName());
			thirdPartyUser.put(JWTValues.ACTUAL_FIRSTNAME, user.getThirdPartyUser().getActualFirstName());
			thirdPartyUser.put(JWTValues.ACTUAL_LASTNAME, user.getThirdPartyUser().getActualLastName());
			thirdPartyUser.put(JWTValues.ACTUAL_EMAIL, user.getThirdPartyUser().getActualEmail());
			
			payload.set(JWTValues.THIRD_PARTY_FIELD, thirdPartyUser);
		}
	}
}
