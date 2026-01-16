package com.itu.taxi_brousse.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "categoriegroupeage_classeplace_override")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategorieGroupeAgeClassePlaceOverride {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "prix_override", nullable = false)
    private Double prixOverride;
    
    @ManyToOne
    @JoinColumn(name = "id_categorieGroupeAge", nullable = false)
    private CategorieGroupeAge categorieGroupeAge;
    
    @ManyToOne
    @JoinColumn(name = "id_classePlace", nullable = false)
    private ClassePlace classePlace;
}