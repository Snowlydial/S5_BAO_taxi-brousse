package com.itu.taxi_brousse.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "historiqueprixclasse")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoriquePrixClasse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_histoprixclasse")
    private Integer id;
    
    @Column(name = "date_ecriture")
    private LocalDateTime dateEcriture;
    
    @ManyToOne
    @JoinColumn(name = "id_busclasse", nullable = false)
    private BusClasse busClasse;
}