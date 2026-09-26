package com.van.sec.service;

import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.van.sec.entity.User;
import com.van.sec.repository.UserRepository;

@Service
public class CustomerUserDetailsService implements UserDetailsService {

	private UserRepository userRepository;

	public CustomerUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		User user= userRepository.findByUsername(username).orElseThrow(
				()->  new UsernameNotFoundException("User is not found by : "+username));

		UserDetails userDetails = new org.springframework.security.core.userdetails.User(user.getUsername(), 
				user.getPassword(), 
				user.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.getName())).collect(Collectors.toList()));
		return userDetails;
	}

}
