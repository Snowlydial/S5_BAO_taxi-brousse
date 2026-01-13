package com.itu.taxi_brousse.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "voyage")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Voyage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_voyage")
    private Integer id;
    
    @Column(name = "duree")
    private Double duree;
    
    @Column(name = "prix_additif")
    private Double prixAdditif;
    
    @ManyToOne
    @JoinColumn(name = "id_gare_1", nullable = false)
    private Gare gareDepart;
    
    @ManyToOne
    @JoinColumn(name = "id_gare_2", nullable = false)
    private Gare gareArrivee;
    
    @OneToMany(mappedBy = "voyage")
    @Builder.Default
    private List<BusVoyage> busVoyages = new ArrayList<>();
}