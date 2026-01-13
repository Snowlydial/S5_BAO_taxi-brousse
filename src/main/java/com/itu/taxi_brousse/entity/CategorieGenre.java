package com.itu.taxi_brousse.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categoriegenre")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategorieGenre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoriegenre")
    private Integer id;
    
    @Column(name = "libelle", length = 50)
    private String libelle;
    
    
}