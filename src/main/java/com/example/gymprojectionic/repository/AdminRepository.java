package com.example.gymprojectionic.repository;

import com.example.gymprojectionic.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, Long> {
}