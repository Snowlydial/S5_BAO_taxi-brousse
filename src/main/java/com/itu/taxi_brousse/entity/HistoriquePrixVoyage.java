package com.itu.taxi_brousse.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "historiqueprixvoyage")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoriquePrixVoyage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_histoprixvoyage")
    private Integer id;
    
    @Column(name = "date_ecriture")
    private LocalDateTime dateEcriture;
    
    @ManyToOne
    @JoinColumn(name = "id_voyage", nullable = false)
    private Voyage voyage;
}