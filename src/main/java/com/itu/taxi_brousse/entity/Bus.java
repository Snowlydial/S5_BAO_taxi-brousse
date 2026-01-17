package com.itu.taxi_brousse.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bus")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_bus")
    private Integer id;
    
    @Column(name = "immatriculation", length = 50, unique = true)
    private String immatriculation;
    
}