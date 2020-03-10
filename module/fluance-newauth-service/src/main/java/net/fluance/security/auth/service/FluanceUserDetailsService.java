package net.fluance.security.auth.service;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import net.fluance.security.core.repository.jdbc.UserInfoRepository;

@Service
public class FluanceUserDetailsService implements UserDetailsService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UserDetailsService.class);

	private List<UserDetailsService> userDetailServices = new LinkedList<>();

	public FluanceUserDetailsService() {
	}

	public void addService(UserDetailsService srv) {
		userDetailServices.add(srv);
	}

	@Autowired
	UserInfoRepository userInfoRepository;

	@Override
	public UserDetails loadUserByUsername(String login) {
		LOGGER.debug("{}Trying to validate user: {}", "loadUserByUsername", login);
		if (userDetailServices != null && !userDetailServices.isEmpty() ) {
			try {
				if (userInfoRepository.findOne(login) != null) {
					return new User(login, login, AuthorityUtils.NO_AUTHORITIES);
				} else {
					LOGGER.debug("{}User not found in DB {}", "loadUserByUsername", login);
					throw new UsernameNotFoundException("Unknown user : " + login);
				}
			} catch (Exception ex) {
				LOGGER.debug("{}Error searching user in DB {}", "loadUserByUsername", login);
				ex.printStackTrace();
				throw ex;
			}
		}
		LOGGER.debug("{}User not found {}", "loadUserByUsername", login);
		throw new UsernameNotFoundException("Unknown user : " + login);
	}

}