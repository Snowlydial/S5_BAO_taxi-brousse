package com.itu.taxi_brousse.repository;

import com.itu.taxi_brousse.entity.Facture;
import com.itu.taxi_brousse.dto.views.FactureTotalsProjection;
import com.itu.taxi_brousse.entity.BusVoyage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FactureRepository extends JpaRepository<Facture, Integer> {
    
    Optional<Facture> findByBusVoyage(BusVoyage busVoyage);
    
    List<Facture> findAllByOrderByDateEmissionDesc();
    
    List<Facture> findAllByOrderByIdDesc();
    
    Optional<Facture> findByNumeroFacture(String numeroFacture);

    @Query(value =
        "SELECT facture_id AS factureId, " +
        "       total_reservations AS totalReservations, " +
        "       total_due_diffusions AS totalDueDiffusions, " +
        "       total_paid_diffusions AS totalPaidDiffusions, " +
        "       total_remaining_diffusions AS totalRemainingDiffusions, " +
        "       montant_total AS montantTotal " +
        "FROM view_facture_totals",
        nativeQuery = true
    )
    List<FactureTotalsProjection> findAllFactureTotals();
}