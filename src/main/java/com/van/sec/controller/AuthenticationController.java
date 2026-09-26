package com.van.sec.controller;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.van.sec.dto.RegisterRequest;
import com.van.sec.entity.Role;
import com.van.sec.entity.User;
import com.van.sec.jwt.JwtUtil;
import com.van.sec.repository.RoleRepository;
import com.van.sec.repository.UserRepository;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

	private final AuthenticationManager authenticationManager;
	private final JwtUtil jwtUtil;
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;

	@Value("${role.admin}")
	private String roleAdmin;

	@Value("${role.user}")
	private String roleUser;

	public AuthenticationController(AuthenticationManager authenticationManager, JwtUtil jwtUtil,
			UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
		super();
		this.authenticationManager = authenticationManager;
		this.jwtUtil = jwtUtil;
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@PostMapping(name = "/register")
	public ResponseEntity<String> register(@RequestBody RegisterRequest request) {

//		http://localhost:8086/auth/register

//		{
//		    "username": "van",
//		    "password": "van123",
//		    "roles": [
//		        "USER"
//		    ]
//		}

		if (userRepository.findByUsername(request.getUsername()).isPresent()) {
			return ResponseEntity.badRequest().body("Username is already taken");
		}
		User user = new User();
		user.setUsername(request.getUsername());

		String encodedPwd = passwordEncoder.encode(request.getPassword());
		user.setPassword(encodedPwd);
		System.out.println("encoded password .. " + encodedPwd);

		Set<Role> roles = new HashSet<>();
		for (String roleName : request.getRoles()) {

			Role role = roleRepository.findByName(roleName)
					.orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
			roles.add(role);
		}
		user.setRoles(roles);
		userRepository.save(user);

		return ResponseEntity.ok("Successfully registered");
	}

	@GetMapping("/login/{uname}/{pwd}")
	public ResponseEntity<String> login(@PathVariable String uname, @PathVariable String pwd) {
		String authToken = null;
		try {
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(uname, pwd));
		} catch (Exception e) {
			System.out.println(" Exception : " + e);
		}

		authToken = jwtUtil.generateToken(uname);
		return ResponseEntity.ok(authToken);
	}

	// End point to access user protected resources

	@GetMapping("/protected-data")
	public ResponseEntity<String> getProtectedData(@RequestHeader("Authorization") String token) {

		if (token != null && token.startsWith("Bearer ")) {
			String jwtToken = token.substring(7);
			try {
				String username = jwtUtil.extractUsername(jwtToken); // extract username from jwt token

				Set<String> roles = jwtUtil.extractRoles(jwtToken); // extract roles from the JWT token

				if (roles.contains(roleAdmin)) {

					return ResponseEntity.ok("Welcome " + username + " here is the " + roles);
				} else if (roles.contains(roleUser)) {
					return ResponseEntity.ok("Welcome " + username + " here is the " + roles);
				} else {
					return ResponseEntity.status(403).body("Access denied: you don't have the necessarry role");
				}
			} catch (Exception ex) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid token");
			}

		}
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorization header missing or invalid");
	}

}
