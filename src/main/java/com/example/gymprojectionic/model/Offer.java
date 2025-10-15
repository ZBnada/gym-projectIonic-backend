package com.example.gymprojectionic.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "offers")
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;
    private String description;
    private int dureeMois;
    private BigDecimal prix;

    public Offer() {}

    public Offer(String titre, String description, int dureeMois, BigDecimal prix) {
        this.titre = titre;
        this.description = description;
        this.dureeMois = dureeMois;
        this.prix = prix;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getDureeMois() { return dureeMois; }
    public void setDureeMois(int dureeMois) { this.dureeMois = dureeMois; }

    public BigDecimal getPrix() { return prix; }
    public void setPrix(BigDecimal prix) { this.prix = prix; }
}
