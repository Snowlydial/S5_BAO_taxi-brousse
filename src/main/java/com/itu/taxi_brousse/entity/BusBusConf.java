package com.itu.taxi_brousse.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bus_busconf")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusBusConf {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_bus_busconf")
    private Integer id;
    
    @ManyToOne
    @JoinColumn(name = "id_bus")
    private Bus bus;
    
    @ManyToOne
    @JoinColumn(name = "id_busconf")
    private BusConf busConf;
}