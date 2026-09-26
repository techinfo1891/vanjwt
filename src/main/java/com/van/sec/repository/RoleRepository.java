package com.van.sec.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.van.sec.entity.Role;

import java.util.Optional;


public interface RoleRepository extends JpaRepository<Role, Long>{
	
	Optional<Role> findByName(String name);

}
