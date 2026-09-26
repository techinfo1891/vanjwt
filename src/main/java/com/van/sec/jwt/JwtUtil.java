package com.van.sec.jwt;

import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import com.van.sec.entity.Role;
import com.van.sec.entity.User;
import com.van.sec.repository.UserRepository;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
	
	// secret key
	private static final SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
	
	// expiration time
	private final int jwtExpirationMs = 86400000;
	

	private UserRepository userRepository;

	public JwtUtil(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	// Generate Token
	public String generateToken(String username) {

		Optional<User> user = userRepository.findByUsername(username);
		Set<Role> roles = user.get().getRoles();

		// Add roles to the token

		return Jwts.builder().setSubject(username)
				.claim("roles", roles.stream().map(role -> role.getName()).collect(Collectors.joining(",")))
				.setIssuedAt(new Date()).setExpiration(new Date(new Date().getTime() + jwtExpirationMs))
				.signWith(secretKey).compact();
	}

	// Extract username
	public String extractUsername(String token) {
		return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody().getSubject();
	}

	// Extract Roles
	public Set<String> extractRoles(String token) {
		String rolesString = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody()
				.get("roles", String.class);

		return Set.of(rolesString);
	}
	

	//Token validation
	public boolean isTokenValid(String toekn) {
		try {
			Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(toekn);
			return true;
		} catch (JwtException  | IllegalArgumentException e) {
			return false;
		}

	}
}
