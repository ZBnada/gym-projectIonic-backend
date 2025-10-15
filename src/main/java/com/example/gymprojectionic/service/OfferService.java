package com.example.gymprojectionic.service;

import com.example.gymprojectionic.model.Offer;
import com.example.gymprojectionic.repository.OfferRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class OfferService {

    @Autowired
    private OfferRepository offerRepo;

    public Offer createOffer(Offer offer) {
        return offerRepo.save(offer);
    }

    public List<Offer> getAllOffers() {
        return offerRepo.findAll();
    }

    public Offer getOfferById(Long id) {
        return offerRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Offre introuvable"));
    }

    // ---------------- UPDATE ----------------
    public Offer updateOffer(Long id, Offer offerDetails) {
        Offer existingOffer = getOfferById(id);
        existingOffer.setTitre(offerDetails.getTitre());
        existingOffer.setDescription(offerDetails.getDescription());
        existingOffer.setPrix(offerDetails.getPrix());
        existingOffer.setDureeMois(offerDetails.getDureeMois());
        return offerRepo.save(existingOffer);
    }

    // ---------------- DELETE ----------------
    public void deleteOffer(Long id) {
        Offer existingOffer = getOfferById(id);
        offerRepo.delete(existingOffer);
    }


}
