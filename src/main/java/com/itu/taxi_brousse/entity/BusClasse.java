package com.itu.taxi_brousse.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "busclasse")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusClasse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_busclasse")
    private Integer id;
    
    @Column(name = "libelle", length = 50)
    private String libelle;
    
    @Column(name = "prix_classe")
    private Double prixClasse;
    
    @OneToMany(mappedBy = "busClasse")
    @Builder.Default
    private List<Bus> buses = new ArrayList<>();

}
