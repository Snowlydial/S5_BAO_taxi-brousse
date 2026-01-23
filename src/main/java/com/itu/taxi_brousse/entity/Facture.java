package com.itu.taxi_brousse.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "facture")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Facture {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_facture")
    private Integer id;
    
    @Column(name = "numero_facture", unique = true, nullable = false, length = 50)
    private String numeroFacture;
    
    @Column(name = "date_emission", nullable = false)
    private LocalDate dateEmission;
    
    @Column(name = "ca_reservations")
    private Double caReservations;
    
    @Column(name = "ca_diffusions")
    private Double caDiffusions;
    
    @Column(name = "montant_total")
    private Double montantTotal;
    
    @ManyToOne
    @JoinColumn(name = "id_bus_voyage", nullable = false)
    private BusVoyage busVoyage;
    
    @OneToMany(mappedBy = "facture", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<FactureLigne> lignes = new ArrayList<>();
    
    //?=== Helper methods
    public void calculateTotals() {
        this.caReservations = lignes.stream()
            .filter(l -> "RESERVATION".equals(l.getTypeLigne()))
            .mapToDouble(FactureLigne::getMontant)
            .sum();
        
        this.caDiffusions = lignes.stream()
            .filter(l -> "DIFFUSION".equals(l.getTypeLigne()))
            .mapToDouble(FactureLigne::getMontant)
            .sum();
        
        this.montantTotal = caReservations + caDiffusions;
    }
}