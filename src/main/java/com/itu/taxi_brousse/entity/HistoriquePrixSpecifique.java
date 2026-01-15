package com.itu.taxi_brousse.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "historiqueprixspecifique")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoriquePrixSpecifique {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_histoprixspecifique")
    private Integer id;
    
    @Column(name = "date_ecriture")
    private LocalDateTime dateEcriture;

    @Column(name = "prix_specifique")
    private Double prixSpecifique;
    
    @ManyToOne
    @JoinColumn(name = "id_bus_voyage", nullable = false)
    private BusVoyage busVoyage;
}