package com.itu.taxi_brousse.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "client")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_client")
    private Integer id;
    
    @Column(name = "nom", length = 50)
    private String nom;
    
    @Column(name = "prenom", length = 50)
    private String prenom;
    
    @ManyToOne
    @JoinColumn(name = "id_categoriegenre", nullable = false)
    private CategorieGenre categorieGenre;
    
    @ManyToOne
    @JoinColumn(name = "id_categoriegroupeage", nullable = false)
    private CategorieGroupeAge categorieGroupeAge;
    
    @OneToMany(mappedBy = "client")
    @Builder.Default
    private List<Reservation> reservations = new ArrayList<>();
}