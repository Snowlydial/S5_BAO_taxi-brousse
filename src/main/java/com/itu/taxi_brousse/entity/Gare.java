package com.itu.taxi_brousse.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "gare")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Gare {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_gare")
    private Integer id;
    
    @Column(name = "libelle", length = 50)
    private String libelle;
    
}
