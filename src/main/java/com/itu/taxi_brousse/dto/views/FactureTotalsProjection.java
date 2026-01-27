package com.itu.taxi_brousse.dto.views;

public interface FactureTotalsProjection {
    Integer getFactureId();
    Double getTotalReservations();
    Double getTotalDueDiffusions();
    Double getTotalPaidDiffusions();
    Double getTotalRemainingDiffusions();
    Double getMontantTotal();
}
