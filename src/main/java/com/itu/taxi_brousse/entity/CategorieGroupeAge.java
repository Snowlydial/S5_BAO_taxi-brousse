package com.itu.taxi_brousse.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

@Entity
@Table(name = "categoriegroupeage")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CategorieGroupeAge {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categorieGroupeAge")  // Match database column name
    private Integer id;
    
    @Column(name = "libelle", nullable = false)
    private String libelle;
    
}