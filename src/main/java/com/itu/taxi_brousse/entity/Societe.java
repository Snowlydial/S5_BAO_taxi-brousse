package com.itu.taxi_brousse.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Societe")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Societe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_societe")
    private Integer id;
    
    @Column(name = "nom_societe", length = 100, nullable = false)
    private String nomSociete;
}