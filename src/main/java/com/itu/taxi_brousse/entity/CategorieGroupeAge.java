package com.itu.taxi_brousse.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categoriegroupeage")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategorieGroupeAge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoriegroupeage")
    private Integer id;
    
    @Column(name = "libelle", length = 50)
    private String libelle;
    
    @Column(name = "prix_standard_override")
    private Double prixStandardOverride;
}