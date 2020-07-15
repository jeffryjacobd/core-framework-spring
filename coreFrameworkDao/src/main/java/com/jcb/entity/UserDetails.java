package com.jcb.entity;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

import com.jcb.dto.UserAccountDto;

import lombok.Getter;
import lombok.Setter;

public class UserDetails implements org.springframework.security.core.userdetails.UserDetails {

	private static final long serialVersionUID = -985457253884385006L;

	public static final class GrantedAuth implements GrantedAuthority {
		private static final long serialVersionUID = 6363464388069866115L;
		@Getter
		@Setter
		private String authority;

	}

	private static transient final Argon2PasswordEncoder encoder = new Argon2PasswordEncoder();

	@Getter
	@Setter
	private List<GrantedAuth> authorities = new ArrayList<>();

	@Getter
	@Setter
	private String password;

	@Getter
	@Setter
	private String username;

	@Getter
	@Setter
	private boolean accountNonExpired;

	@Getter
	@Setter
	private boolean accountNonLocked;

	@Getter
	@Setter
	private boolean credentialsNonExpired;

	@Getter
	@Setter
	private boolean enabled;

	public static UserDetails convertToEntity(UserAccountDto userAccountDto) {
		UserDetails userDetails = new UserDetails();
		userDetails.setUsername(userAccountDto.getUserName());
		userDetails.setPassword(userAccountDto.getPassword());
		userDetails.setAccountNonExpired(!userAccountDto.isAccountExpired());
		userDetails.setAccountNonLocked(!userAccountDto.isAccountLocked());
		userDetails.setCredentialsNonExpired(!userAccountDto.isCredentialsExpired());
		userDetails.setEnabled(userAccountDto.isEnabled());
		userAccountDto.getRoles().stream().forEach(role -> {
			GrantedAuth auth = new GrantedAuth();
			auth.setAuthority(role);
			userDetails.getAuthorities().add(auth);
		});
		return userDetails;
	}

	public UserAccountDto convertToDto(boolean encodePassword) {
		UserAccountDto userAccountDto = new UserAccountDto();
		userAccountDto.setUserName(this.getUsername());
		if (encodePassword) {
			userAccountDto.setPassword(encoder.encode(this.getPassword()));
		} else {
			userAccountDto.setPassword(this.getPassword());
		}
		userAccountDto.setAccountExpired(!this.isAccountNonExpired());
		userAccountDto.setAccountLocked(!this.isAccountNonLocked());
		userAccountDto.setEnabled(this.isEnabled());
		userAccountDto.setRoles(new ArrayList<>());
		this.getAuthorities().stream().forEach(authority -> {
			userAccountDto.getRoles().add(authority.getAuthority());
		});
		return userAccountDto;
	}
}
