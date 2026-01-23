package com.itu.taxi_brousse.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "societe")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Societe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_societe")
    private Integer id;
    
    @Column(name = "nom", nullable = false)
    private String nom;
}