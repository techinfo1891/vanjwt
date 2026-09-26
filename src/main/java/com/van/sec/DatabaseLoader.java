package com.van.sec;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.van.sec.entity.Role;
import com.van.sec.repository.RoleRepository;

@Configuration
public class DatabaseLoader {
	
	@Bean
	CommandLineRunner initDatabase(RoleRepository roleRepository) {
		
		return ars -> {
			roleRepository.save(new Role("USER"));
			roleRepository.save(new Role("ADMIN"));
			
			System.out.println("-------------------Initial data successfully inserted!-----------------");

		}; 
	}

}
