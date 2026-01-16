package com.itu.taxi_brousse.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

@Entity
@Table(name = "client")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Client {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "nom", nullable = false)
    private String nom;
    
    @Column(name = "prenom", nullable = false)
    private String prenom;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_categorieGenre", nullable = false)
    private CategorieGenre categorieGenre;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_categorieGroupeAge", nullable = false)
    private CategorieGroupeAge categorieGroupeAge;
}