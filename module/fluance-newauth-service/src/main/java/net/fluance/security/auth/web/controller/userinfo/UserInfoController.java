package net.fluance.security.auth.web.controller.userinfo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import net.fluance.app.web.support.payload.response.GenericResponsePayload;
import net.fluance.security.auth.web.controller.AbstractAuthRestController;
import net.fluance.security.core.model.jdbc.UserInfo;
import net.fluance.security.core.repository.jdbc.UserInfoRepository;

@RestController
@RequestMapping("/userinfo")
public class UserInfoController extends AbstractAuthRestController{

	private static Logger LOGGER = LogManager.getLogger(UserInfoController.class);

	@Autowired
	private UserInfoRepository userInfoRepository;

	@ApiOperation(value = "Get User Info", response = GenericResponsePayload.class, tags = "User Info API")
	@RequestMapping(value = "", method = RequestMethod.GET)
	public ResponseEntity<?> getUserInfo(@RequestParam String username, @RequestParam String domain, HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			String subjectId = domain + "/" + username;
			UserInfo userInfo = userInfoRepository.findOne(subjectId);
			return new ResponseEntity<>(userInfo, HttpStatus.OK);
		} catch (Exception e) {
			return handleException(e);
		}
	}

	@Override
	public Logger getLogger() {
		return LOGGER;
	}

}
