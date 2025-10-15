package com.example.gymprojectionic.controller;

import com.example.gymprojectionic.model.Client;
import com.example.gymprojectionic.model.Enum.Role;
import com.example.gymprojectionic.service.ClientService;
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
@RequestMapping("/api/clients")
public class ClientController {

    @Autowired
    private ClientService clientService;

    @Autowired
    private PasswordEncoder passwordEncoder;
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    // Ajouter un client
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Client addClient(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("email") String email,
            @RequestParam("pwd") String pwd,
            @RequestParam("phone") Long phone,
            @RequestParam("offerId") Long offerId,
            @RequestParam(value = "photo", required = false) MultipartFile photo
    ) {
        Client client = new Client();
        client.setFirstName(firstName);
        client.setLastName(lastName);
        client.setEmail(email);

        // ✅ Hash du mot de passe avant sauvegarde
        client.setPwd(passwordEncoder.encode(pwd));

        client.setPhone(phone);
        client.setRole(Role.CLIENT); // par défaut, le rôle du client

        // 📸 Upload de photo si présente
        if (photo != null && !photo.isEmpty()) {
            try {
                File uploadDir = new File(UPLOAD_DIR);
                if (!uploadDir.exists()) uploadDir.mkdirs();

                String fileName = System.currentTimeMillis() + "_" + photo.getOriginalFilename();
                Path filePath = Paths.get(UPLOAD_DIR, fileName);
                Files.write(filePath, photo.getBytes());

                client.setPhoto(fileName);
            } catch (IOException e) {
                throw new RuntimeException("Erreur lors de l'enregistrement de la photo : " + e.getMessage());
            }
        }

        //  Appel du service avec offerId
        return clientService.addClient(client, offerId);
    }


    // Obtenir tous les clients
    @GetMapping
    public List<Client> getAllClients() {
        return clientService.getAllClients();
    }

    // Obtenir un client par ID
    @GetMapping("/{id}")
    public ResponseEntity<Client> getClientById(@PathVariable Long id) {
        return clientService.getClientById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Supprimer un client
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        clientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }

    //modifier

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Client> updateClient(
            @PathVariable Long id,
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("email") String email,
            @RequestParam(value = "pwd", required = false) String pwd,
            @RequestParam("phone") Long phone,
            @RequestParam(value = "offerId", required = false) Long offerId,
            @RequestParam(value = "photo", required = false) MultipartFile photo
    ) {
        Client clientDetails = new Client();
        clientDetails.setFirstName(firstName);
        clientDetails.setLastName(lastName);
        clientDetails.setEmail(email);
        clientDetails.setPhone(phone);

        //  Hash du mot de passe si fourni
        if (pwd != null && !pwd.isEmpty()) {
            clientDetails.setPwd(passwordEncoder.encode(pwd));
        }

        //  Gérer la photo si présente
        if (photo != null && !photo.isEmpty()) {
            try {
                File uploadDir = new File(UPLOAD_DIR);
                if (!uploadDir.exists()) uploadDir.mkdirs();

                String fileName = System.currentTimeMillis() + "_" + photo.getOriginalFilename();
                Path filePath = Paths.get(UPLOAD_DIR, fileName);
                Files.write(filePath, photo.getBytes());

                clientDetails.setPhoto(fileName);
            } catch (IOException e) {
                throw new RuntimeException("Erreur lors de l'enregistrement de la photo : " + e.getMessage());
            }
        }

        Client updatedClient = clientService.updateClient(id, clientDetails, offerId);
        return ResponseEntity.ok(updatedClient);
    }


}
