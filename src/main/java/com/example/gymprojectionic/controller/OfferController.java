package com.example.gymprojectionic.controller;

import com.example.gymprojectionic.model.Offer;
import com.example.gymprojectionic.service.OfferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/offers")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class OfferController {

    @Autowired
    private OfferService offerService;

    // ---------------- CREATE OFFER ----------------
    @PostMapping("/add")
    public ResponseEntity<Offer> createOffer(@RequestBody Offer offer) {
        Offer savedOffer = offerService.createOffer(offer);
        return ResponseEntity.ok(savedOffer);
    }

    // ---------------- GET ALL OFFERS ----------------
    @GetMapping("/all")
    public ResponseEntity<List<Offer>> getAllOffers() {
        List<Offer> offers = offerService.getAllOffers();
        return ResponseEntity.ok(offers);
    }

    // ---------------- GET OFFER BY ID ----------------
    @GetMapping("/{id}")
    public ResponseEntity<Offer> getOfferById(@PathVariable Long id) {
        Offer offer = offerService.getOfferById(id);
        return ResponseEntity.ok(offer);
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<Offer> updateOffer(@PathVariable Long id, @RequestBody Offer offerDetails) {
        return ResponseEntity.ok(offerService.updateOffer(id, offerDetails));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOffer(@PathVariable Long id) {
        offerService.deleteOffer(id);
        return ResponseEntity.noContent().build();
    }
}
