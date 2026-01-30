package com.itu.taxi_brousse.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "produit_societe", uniqueConstraints = @UniqueConstraint(columnNames = {"id_produit", "id_societe"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProduitSociete {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_produit_societe")
    private Integer id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_produit", nullable = false)
    private Produit produit;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_societe", nullable = false)
    private Societe societe;
    
    @Column(name = "prix_unitaire", nullable = false)
    private Double prixUnitaire;
}
