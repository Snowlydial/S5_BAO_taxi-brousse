package com.itu.taxi_brousse.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "classeplace")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassePlace {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_classeplace")
    private Integer id;
    
    @Column(name = "libelle")
    private String libelle;
    
    @Column(name = "prix_place")
    private Double prixPlace;
}