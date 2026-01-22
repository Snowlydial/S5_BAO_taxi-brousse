package com.itu.taxi_brousse.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;

@Entity
@Table(name = "classeage_conf")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClasseAgeConf {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_classeage_conf")
  private Integer id;

  @Column(name = "valeur_override", nullable = false)
  private Double valeurOverride;

  @Column(name = "est_pourcentage", nullable = false)
  private Boolean estPourcentage;

  @Column(name = "date_debut")
  private LocalDate dateDebut;

  @Column(name = "date_fin")
  private LocalDate dateFin;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "id_categoriegroupeage", nullable = false)
  private CategorieGroupeAge categorieGroupeAge;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "id_classeplace", nullable = false)
  private ClassePlace classePlace;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "id_voyage", nullable = false)
  private Voyage Voyage;

  //?== Check if this configuration is active for a given date
  public boolean isActiveOn(LocalDate date) {
    if (date == null) {
      date = LocalDate.now();
    }

    boolean afterStart = (dateDebut == null) || !date.isBefore(dateDebut);
    boolean beforeEnd = (dateFin == null) || !date.isAfter(dateFin);

    return afterStart && beforeEnd;
  }

  //?== Calculate the effective price based on base price
  // basePrice The reference price (usually adult price for this seat class)
  public Double calculatePrice(Double basePrice) {
    if (estPourcentage) {
      return basePrice * (1 + valeurOverride);
    } else {
      return valeurOverride;
    }
  }
}
