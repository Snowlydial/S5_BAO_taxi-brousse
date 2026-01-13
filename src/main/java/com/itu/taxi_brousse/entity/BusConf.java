package com.itu.taxi_brousse.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "busconf")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusConf {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_busconf")
    private Integer id;
    
    @Column(name = "libelle", length = 50)
    private String libelle;
    
    @Column(name = "valeur", length = 255)
    private String valeur;
    
}