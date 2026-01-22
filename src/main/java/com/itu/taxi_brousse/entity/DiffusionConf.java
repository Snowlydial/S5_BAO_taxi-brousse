package com.itu.taxi_brousse.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "DiffusionConf")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiffusionConf {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_diffusionConf")
    private Integer id;
    
    @Column(name = "prix")
    private Double prix;
    
    //?=== Get prix diffusion for a date range (nombre de diffusions * prix)
    public Double getPrixDiffusion(long nbDiffusion) {
        if (this.prix == null) {
            return 0.0;
        }
        return nbDiffusion * this.prix;
    }
}