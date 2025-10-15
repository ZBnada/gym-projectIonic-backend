package com.example.gymprojectionic.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "clients")
public class Client extends User {

    @ManyToOne
    @JoinColumn(name = "offer_id")
    private Offer offer; // L’offre choisie par le membre

    public Offer getOffer() {
        return offer;
    }

    public void setOffer(Offer offer) {
        this.offer = offer;
    }
}
