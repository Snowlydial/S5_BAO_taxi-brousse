package com.itu.taxi_brousse.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "produitcommande")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProduitCommande {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_produitcommande")
    private Integer id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_bus_voyage", nullable = false)
    private BusVoyage busVoyage;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_produit_societe", nullable = false)
    private ProduitSociete produitSociete;
    
    @Column(name = "quantite", nullable = false)
    private Integer quantite;
    
    @Column(name = "date_commande", nullable = false)
    private LocalDate dateCommande;
}
