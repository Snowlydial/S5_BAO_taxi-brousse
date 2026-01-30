package com.itu.taxi_brousse.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "produit")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Produit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_produit")
    private Integer id;
    
    @Column(name = "libelle", nullable = false)
    private String libelle;
    
    @Column(name = "description")
    private String description;
    
    //*-- One product can be sold by multiple societies
    @OneToMany(mappedBy = "produit", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProduitSociete> produitSocietes;
}
