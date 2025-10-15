package com.example.gymprojectionic.service;

import com.example.gymprojectionic.model.Admin;
import com.example.gymprojectionic.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    // Ajouter un admin
    public Admin addAdmin(Admin admin) {
        return adminRepository.save(admin);
    }

    // Obtenir tous les admins
    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    // Obtenir un admin par ID
    public Optional<Admin> getAdminById(Long id) {
        return adminRepository.findById(id);
    }

    // Supprimer un admin
    public void deleteAdmin(Long id) {
        adminRepository.deleteById(id);
    }

    // Modifier un admin
    public Admin updateAdminEntity(Admin admin) {
        return adminRepository.save(admin);
    }

}
