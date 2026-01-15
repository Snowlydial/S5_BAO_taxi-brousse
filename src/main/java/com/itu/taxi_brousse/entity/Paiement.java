package com.itu.taxi_brousse.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "paiement")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Paiement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_paiement")
    private Integer id;
    
    @Column(name = "date_paiement")
    private LocalDateTime datePaiement;
    
    @Column(name = "montant_paye")
    private Double montantPaye;
    
    @ManyToOne
    @JoinColumn(name = "id_caisse", nullable = false)
    private Caisse caisse;
    
    @ManyToOne
    @JoinColumn(name = "id_reservation", nullable = false)
    private Reservation reservation;
}