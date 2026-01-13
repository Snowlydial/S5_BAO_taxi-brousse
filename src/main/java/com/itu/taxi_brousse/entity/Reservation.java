package com.itu.taxi_brousse.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reservation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reservation")
    private Integer id;
    
    @Column(name = "numero_place")
    private Integer numeroPlace;
    
    @ManyToOne
    @JoinColumn(name = "id_client", nullable = false)
    private Client client;
    
    @ManyToOne
    @JoinColumn(name = "id_bus_voyage", nullable = false)
    private BusVoyage busVoyage;
    
    @OneToMany(mappedBy = "reservation")
    @Builder.Default
    private List<Paiement> paiements = new ArrayList<>();
}