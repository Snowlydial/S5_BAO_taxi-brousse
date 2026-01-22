package com.itu.taxi_brousse.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "Diffusion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Diffusion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_diffusion")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_societe")
    private Societe societe;

    @ManyToOne
    @JoinColumn(name = "id_bus_voyage")
    private BusVoyage busVoyage;

    @Column(name = "date_diffusion")
    private LocalDate dateDiffusion;

    @Column(name = "heure_diffusion")
    private LocalTime heureDiffusion;

    @Column(name = "description")
    private String description;
}