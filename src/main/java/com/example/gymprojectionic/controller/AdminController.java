package com.example.gymprojectionic.controller;

import com.example.gymprojectionic.model.Admin;
import com.example.gymprojectionic.model.Enum.Role;
import com.example.gymprojectionic.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/admins")
public class AdminController {

    @Autowired
    private AdminService adminService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    // Ajouter un admin
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Admin addAdmin(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("email") String email,
            @RequestParam("pwd") String pwd,
            @RequestParam("phone") Long phone,
            @RequestParam("role") String role,
            @RequestParam(value = "photo", required = false) MultipartFile photo
    ) {
        Admin admin = new Admin();
        admin.setFirstName(firstName);
        admin.setLastName(lastName);
        admin.setEmail(email);

        // ✅ Hash du mot de passe
        admin.setPwd(passwordEncoder.encode(pwd));

        admin.setPhone(phone);
        admin.setRole(Role.valueOf(role.toUpperCase()));

        // 📸 Gestion de la photo si présente
        if (photo != null && !photo.isEmpty()) {
            try {
                File uploadDir = new File(UPLOAD_DIR);
                if (!uploadDir.exists()) uploadDir.mkdirs();

                String fileName = System.currentTimeMillis() + "_" + photo.getOriginalFilename();
                Path filePath = Paths.get(UPLOAD_DIR, fileName);
                Files.write(filePath, photo.getBytes());

                admin.setPhoto(fileName);
            } catch (IOException e) {
                throw new RuntimeException("Erreur lors de l'enregistrement de la photo : " + e.getMessage());
            }
        }

        return adminService.addAdmin(admin);
    }


    // Obtenir tous les admins
    @GetMapping
    public List<Admin> getAllAdmins() {
        return adminService.getAllAdmins();
    }

    // Obtenir un admin par ID
    @GetMapping("/{id}")
    public ResponseEntity<Admin> getAdminById(@PathVariable Long id) {
        return adminService.getAdminById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Supprimer un admin
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdmin(@PathVariable Long id) {
        adminService.deleteAdmin(id);
        return ResponseEntity.noContent().build();
    }

    // Modifier un admin
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Admin> updateAdmin(
            @PathVariable Long id,
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("email") String email,
            @RequestParam("pwd") String pwd,
            @RequestParam("phone") Long phone,
            @RequestParam("role") String role,
            @RequestParam(value = "photo", required = false) MultipartFile photo
    ) {
        Admin admin = adminService.getAdminById(id)
                .orElseThrow(() -> new RuntimeException("Admin non trouvé"));

        admin.setFirstName(firstName);
        admin.setLastName(lastName);
        admin.setEmail(email);
        admin.setPwd(passwordEncoder.encode(pwd)); // 🔐 Hash du mot de passe
        admin.setPhone(phone);
        admin.setRole(Role.valueOf(role.toUpperCase()));

        // 📸 Gestion de la photo
        if (photo != null && !photo.isEmpty()) {
            try {
                File uploadDir = new File(UPLOAD_DIR);
                if (!uploadDir.exists()) uploadDir.mkdirs();

                String fileName = System.currentTimeMillis() + "_" + photo.getOriginalFilename();
                Path filePath = Paths.get(UPLOAD_DIR, fileName);
                Files.write(filePath, photo.getBytes());

                admin.setPhoto(fileName);
            } catch (IOException e) {
                throw new RuntimeException("Erreur lors de l'enregistrement de la photo : " + e.getMessage());
            }
        }

        Admin updatedAdmin = adminService.updateAdminEntity(admin);
        return ResponseEntity.ok(updatedAdmin);
    }

}
