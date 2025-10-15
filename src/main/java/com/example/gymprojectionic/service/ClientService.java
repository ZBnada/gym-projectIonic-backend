package com.example.gymprojectionic.service;

import com.example.gymprojectionic.model.Client;
import com.example.gymprojectionic.model.Offer;
import com.example.gymprojectionic.repository.ClientRepository;
import com.example.gymprojectionic.repository.OfferRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private OfferRepository offerRepository;

    public Client addClient(Client client, Long offerId) {
        if (offerId != null) {
            Offer offer = offerRepository.findById(offerId)
                    .orElseThrow(() -> new RuntimeException("Offre introuvable avec l'id : " + offerId));
            client.setOffer(offer);
        }
        return clientRepository.save(client);
    }


    // Les autres méthodes restent identiques
    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    public Optional<Client> getClientById(Long id) {
        return clientRepository.findById(id);
    }

    public void deleteClient(Long id) {
        clientRepository.deleteById(id);
    }


    public Client updateClient(Long id, Client clientDetails, Long offerId) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client introuvable avec l'id : " + id));

        client.setFirstName(clientDetails.getFirstName());
        client.setLastName(clientDetails.getLastName());
        client.setEmail(clientDetails.getEmail());
        client.setPhone(clientDetails.getPhone());

        if (clientDetails.getPwd() != null && !clientDetails.getPwd().isEmpty()) {
            client.setPwd(clientDetails.getPwd()); // déjà hashé dans le controller
        }

        if (offerId != null) {
            Offer offer = offerRepository.findById(offerId)
                    .orElseThrow(() -> new RuntimeException("Offre introuvable avec l'id : " + offerId));
            client.setOffer(offer);
        }

        if (clientDetails.getPhoto() != null) {
            client.setPhoto(clientDetails.getPhoto());
        }

        return clientRepository.save(client);
    }

    public Client chooseOffer(Long clientId, Long offerId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client introuvable avec l'id : " + clientId));

        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offre introuvable avec l'id : " + offerId));

        client.setOffer(offer);
        return clientRepository.save(client);
    }

}
