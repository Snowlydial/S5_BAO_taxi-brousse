package com.itu.taxi_brousse.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bus_voyage")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusVoyage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_bus_voyage")
    private Integer id;
    
    @Column(name = "heure_depart")
    private LocalTime heureDepart;
    
    @Column(name = "date_depart")
    private LocalDate dateDepart;
    
    @ManyToOne
    @JoinColumn(name = "id_bus", nullable = false)
    private Bus bus;
    
    @ManyToOne
    @JoinColumn(name = "id_voyage", nullable = false)
    private Voyage voyage;
    
    @OneToMany(mappedBy = "busVoyage")
    @Builder.Default
    private List<Reservation> reservations = new ArrayList<>();
}