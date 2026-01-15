package com.itu.taxi_brousse.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "caisse")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Caisse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_caisse")
    private Integer id;
    
    @Column(name = "libelle", length = 50)
    private String libelle;
    
    @OneToMany(mappedBy = "caisse")
    @Builder.Default
    private List<Paiement> paiements = new ArrayList<>();
}