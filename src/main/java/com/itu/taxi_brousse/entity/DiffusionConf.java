package com.itu.taxi_brousse.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "DiffusionConf")
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
}