package com.itu.taxi_brousse.entity;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "diffusionconf")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiffusionConf {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_diffusionConf")
    private Integer id;
    
    @Column(name = "prix")
    private Double prix;

    @Column(name= "date_debut")
    private LocalDate dateDebut;

    @Column(name= "date_fin")
    private LocalDate dateFin;

}