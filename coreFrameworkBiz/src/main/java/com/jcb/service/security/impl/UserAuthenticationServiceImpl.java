/**
 * 
 */
package com.jcb.service.security.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

import com.jcb.dao.RoleDao;
import com.jcb.dao.UserAccountDao;
import com.jcb.dto.RoleDto;
import com.jcb.entity.UserDetails.GrantedAuth;
import com.jcb.service.security.UserAuthenticationService;

import reactor.core.publisher.Mono;

/**
 * @author jeffry
 *
 */
public class UserAuthenticationServiceImpl implements UserAuthenticationService {

	@Autowired
	private UserAccountDao userAccountDao;

	@Autowired
	private RoleDao roleDao;

	@Override
	public Mono<UserDetails> findByUsername(String userName) {
		Mono<List<RoleDto>> roles = roleDao.getAll().collectList();
		return userAccountDao.get(userName).map(userAccountDto -> {
			return com.jcb.entity.UserDetails.convertToEntity(userAccountDto);
		}).zipWith(roles, (userDetails, role) -> {
			mapRolesToUserDetails(userDetails, role);
			return userDetails;
		});
	}

	private void mapRolesToUserDetails(com.jcb.entity.UserDetails userDetails, List<RoleDto> role) {
		List<String> finalRoles = new ArrayList<>();
		userDetails.getAuthorities().stream().forEach(authority -> {
			role.stream().filter(roleString -> {
				return roleString.getRole() == authority.getAuthority();
			}).forEach(roleString -> {
				finalRoles.addAll(roleString.getRelatedRoles());
			});
		});
		userDetails.setAuthorities(new ArrayList<>());
		finalRoles.forEach(roleString -> {
			GrantedAuth auth = new GrantedAuth();
			auth.setAuthority(roleString);
			userDetails.getAuthorities().add(auth);
		});
	}

	@Override
	public Mono<UserDetails> updatePassword(UserDetails user, String newPassword) {
		com.jcb.entity.UserDetails currentUserDetails = (com.jcb.entity.UserDetails) user;
		currentUserDetails.setPassword(newPassword);
		return userAccountDao.insert(currentUserDetails.convertToDto(true)).then(Mono.just(currentUserDetails));
	}

}
