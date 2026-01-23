package com.itu.taxi_brousse.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "factureligne")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FactureLigne {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_factureligne")
    private Integer id;
    
    @Column(name = "type_ligne", nullable = false, length = 20)
    private String typeLigne; // "RESERVATION" or "DIFFUSION"
    
    @Column(name = "montant", nullable = false)
    private Double montant;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @ManyToOne
    @JoinColumn(name = "id_facture", nullable = false)
    private Facture facture;
    
    @ManyToOne
    @JoinColumn(name = "id_reservation")
    private Reservation reservation;
    
    @ManyToOne
    @JoinColumn(name = "id_diffusion")
    private Diffusion diffusion;
}