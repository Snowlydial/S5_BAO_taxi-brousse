package com.itu.taxi_brousse.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "diffusionpaiement")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiffusionPaiement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_diffusionpaiement")
    private Integer id;

    @Column(name = "montant_paye")
    private Double montantPaye;

    @Column(name = "date_paiement")
    private LocalDate datePaiement;

    @ManyToOne
    @JoinColumn(name = "id_diffusion")
    private Diffusion diffusion;

    @ManyToOne
    @JoinColumn(name = "id_societe")
    private Societe societe;
}