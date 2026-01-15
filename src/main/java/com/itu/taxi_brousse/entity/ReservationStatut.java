package com.itu.taxi_brousse.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "reservationstatut")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationStatut {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reservationstatut")
    private Integer id;
    
    @Column(name = "date_annulation")
    private LocalDate dateAnnulation;
    
    @ManyToOne
    @JoinColumn(name = "id_reservation", nullable = false)
    private Reservation reservation;
    
    public boolean isAnnule() {
        return dateAnnulation != null;
    }
    
    public String getStatutLibelle() {
        return isAnnule() ? "Annulé" : "Actif";
    }
}