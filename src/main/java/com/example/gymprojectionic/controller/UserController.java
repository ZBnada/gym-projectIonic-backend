package com.example.gymprojectionic.controller;

import com.example.gymprojectionic.DTO.LoginRequest;
import com.example.gymprojectionic.auth.JwtUtil;
import com.example.gymprojectionic.model.Admin;
import com.example.gymprojectionic.model.Client;
import com.example.gymprojectionic.model.Enum.Role;
import com.example.gymprojectionic.model.User;
import com.example.gymprojectionic.repository.UserRepository;
import com.example.gymprojectionic.service.AdminService;
import com.example.gymprojectionic.service.ClientService;
import com.example.gymprojectionic.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private ClientService clientService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;


    @Autowired
    private PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil = new JwtUtil();
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    // ---------------- SIGNUP ----------------
    @PostMapping("/signup")
    public ResponseEntity<?> signup(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("email") String email,
            @RequestParam("pwd") String pwd,
            @RequestParam("phone") Long phone,
            @RequestParam("role") String roleStr,
            @RequestParam(value = "offerId", required = false) Long offerId,
            @RequestParam(value = "photo", required = false) MultipartFile photoFile) {

        // 1️⃣ Valider le rôle
        Role role;
        try {
            role = Role.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Rôle non valide !");
        }

        // 2️⃣ Gérer la photo
        String photoFileName = null;
        if (photoFile != null && !photoFile.isEmpty()) {
            try {
                File uploadDir = new File(UPLOAD_DIR);
                if (!uploadDir.exists()) uploadDir.mkdirs();

                photoFileName = System.currentTimeMillis() + "_" + photoFile.getOriginalFilename();
                Path filePath = Paths.get(UPLOAD_DIR, photoFileName);
                Files.write(filePath, photoFile.getBytes());
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Erreur lors de l'upload de la photo.");
            }
        }

        // 3️⃣ Construire un utilisateur de base
        User user = new User(firstName, lastName, email, passwordEncoder.encode(pwd), phone, role, photoFileName);

        // 4️⃣ Créer selon le rôle
        switch (role) {
            case ADMIN -> {
                Admin admin = new Admin();
                copyCommonFields(user, admin);
                adminService.addAdmin(admin);
            }

            case CLIENT -> {
                if (offerId == null) {
                    return ResponseEntity.badRequest().body("Veuillez sélectionner une offre !");
                }

                Client client = new Client();
                copyCommonFields(user, client);
                clientService.addClient(client, offerId);
            }
        }

        return ResponseEntity.ok("Utilisateur " + role + " créé avec succès !");
    }

    // ---------------- LOGIN ----------------
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        Map<String, Object> response = new HashMap<>();

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPwd()));

            if (auth.isAuthenticated()) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());
                User user = userService.getUserByEmail(loginRequest.getEmail());
                String token = jwtUtil.createToken1(userDetails, user);

                response.put("status", HttpStatus.OK.value());
                response.put("message", "Authentification réussie");
                response.put("token", token);
                return ResponseEntity.ok(response);
            }

        } catch (BadCredentialsException ex) {
            response.put("message", "Email ou mot de passe incorrect");
        } catch (AuthenticationException ex) {
            response.put("message", "Erreur d’authentification");
        }

        response.put("status", HttpStatus.UNAUTHORIZED.value());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    // ---------------- GET USER BY EMAIL ----------------
    // ---------------- GET USER BY EMAIL ----------------
    @GetMapping("/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        User user = userService.getUserByEmail(email);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(user);
    }

    // ---------------- UPLOAD PHOTO SÉPARÉ ----------------
    @PostMapping("/upload")
    public ResponseEntity<String> uploadPhoto(@RequestParam("photo") MultipartFile photoFile) {
        if (photoFile == null || photoFile.isEmpty()) {
            return ResponseEntity.badRequest().body("Veuillez sélectionner un fichier !");
        }

        try {
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) uploadDir.mkdirs();

            String fileName = System.currentTimeMillis() + "_" + photoFile.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR, fileName);
            Files.write(filePath, photoFile.getBytes());

            return ResponseEntity.ok("Image uploadée avec succès : " + fileName);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de l'upload : " + e.getMessage());
        }
    }

    // ---------------- UTILITAIRES ----------------
    private void copyCommonFields(User source, User target) {
        target.setFirstName(source.getFirstName());
        target.setLastName(source.getLastName());
        target.setEmail(source.getEmail());
        target.setPwd(source.getPwd());
        target.setPhone(source.getPhone());
        target.setRole(source.getRole());
        target.setPhoto(source.getPhoto());
    }

    //  addUser
    @PostMapping(value = "/addUser", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addUser(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("email") String email,
            @RequestParam("pwd") String pwd,
            @RequestParam("phone") Long phone,
            @RequestParam("role") String roleStr,
            @RequestParam(value = "offerId", required = false) Long offerId,
            @RequestParam(value = "photo", required = false) MultipartFile photoFile
    ) {
        // 1 Valider le rôle
        Role role;
        try {
            role = Role.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Rôle non valide !");
        }

        // 2️ Gérer la photo
        String photoFileName = null;
        if (photoFile != null && !photoFile.isEmpty()) {
            try {
                File uploadDir = new File(UPLOAD_DIR);
                if (!uploadDir.exists()) uploadDir.mkdirs();

                photoFileName = System.currentTimeMillis() + "_" + photoFile.getOriginalFilename();
                Path filePath = Paths.get(UPLOAD_DIR, photoFileName);
                Files.write(filePath, photoFile.getBytes());
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Erreur lors de l'upload de la photo.");
            }
        }

        // 3️ Hash du mot de passe
        String encodedPwd = passwordEncoder.encode(pwd);

        // 4️ Créer selon le rôle
        if (role == Role.ADMIN) {
            Admin admin = new Admin();
            admin.setFirstName(firstName);
            admin.setLastName(lastName);
            admin.setEmail(email);
            admin.setPwd(encodedPwd);
            admin.setPhone(phone);
            admin.setRole(Role.ADMIN);
            admin.setPhoto(photoFileName);

            adminService.addAdmin(admin);
            return ResponseEntity.ok("Administrateur créé avec succès !");

        } else if (role == Role.CLIENT) {
            if (offerId == null) {
                return ResponseEntity.badRequest().body("Veuillez sélectionner une offre !");
            }

            Client client = new Client();
            client.setFirstName(firstName);
            client.setLastName(lastName);
            client.setEmail(email);
            client.setPwd(encodedPwd);
            client.setPhone(phone);
            client.setRole(Role.CLIENT);
            client.setPhoto(photoFileName);

            clientService.addClient(client, offerId);
            return ResponseEntity.ok("Client créé avec succès !");
        }

        return ResponseEntity.badRequest().body("Rôle non reconnu !");
    }

    // ---------------- GET ALL USERS ----------------
    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers() {
        try {
            var users = userService.getAllUsers();
            if (users.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Aucun utilisateur trouvé.");
            }
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la récupération des utilisateurs : " + e.getMessage());
        }
    }

    // ---------------- DELETE USER ----------------
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur non trouvé");
        }
        userRepository.deleteById(id);
        return ResponseEntity.ok("Utilisateur supprimé avec succès");
    }

    // ---------------- EDIT USER ----------------
    @PutMapping("/{id}")
    public ResponseEntity<User> editUser(
            @PathVariable Long id,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam(required = false) String pwd,  // ← Rendre optionnel
            @RequestParam(required = false) Long phone,
            @RequestParam Role role,
            @RequestParam(required = false) MultipartFile photo
    ) throws IOException {

        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        User user = optionalUser.get();

        // ✅ Mise à jour des champs simples
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setRole(role);

        // ✅ Mise à jour du mot de passe SEULEMENT si fourni
        if (pwd != null && !pwd.trim().isEmpty()) {
            user.setPwd(pwd); // Hash le mot de passe si nécessaire
        }

        // ✅ Mise à jour de la photo si fournie
        if (photo != null && !photo.isEmpty()) {
            String fileName = UUID.randomUUID() + "_" + photo.getOriginalFilename();
            Path uploadPath = Paths.get("uploads");

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            photo.transferTo(uploadPath.resolve(fileName));
            user.setPhoto(fileName);
        }

        // ✅ Sauvegarde finale
        userRepository.save(user);
        return ResponseEntity.ok(user);
    }

    // ---------------- CHANGE PASSWORD ----------------
    @PutMapping("/{id}/change-password")
    public ResponseEntity<?> changePassword(
            @PathVariable Long id,
            @RequestParam String currentPassword,
            @RequestParam String newPassword) {

        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur non trouvé");
        }

        User user = optionalUser.get();

        // 🔹 Vérifier le mot de passe actuel
        if (!passwordEncoder.matches(currentPassword, user.getPwd())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Mot de passe actuel incorrect");
        }

        // 🔹 Mettre à jour avec le nouveau mot de passe hashé
        user.setPwd(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return ResponseEntity.ok("Mot de passe changé avec succès");
    }

    // ---------------- CHANGE PASSWORD BY EMAIL ----------------
    @PutMapping("/change-password")
    public ResponseEntity<?> changePasswordByEmail(
            @RequestParam String email,
            @RequestParam String currentPassword,
            @RequestParam String newPassword) {

        User user = userService.getUserByEmail(email);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur non trouvé");
        }

        // 🔹 Vérifier le mot de passe actuel
        if (!passwordEncoder.matches(currentPassword, user.getPwd())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Mot de passe actuel incorrect");
        }

        // 🔹 Mettre à jour avec le nouveau mot de passe hashé
        user.setPwd(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return ResponseEntity.ok("Mot de passe changé avec succès");
    }

}