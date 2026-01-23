package com.itu.taxi_brousse.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class BulkDiffusionRequest {
    private Integer societeId;
    private Integer busVoyageId;
    private LocalDate dateDiffusion;
    private LocalTime heureDiffusion;
    private Integer quantity;
    private Double paymentAmount;

    public Integer getSocieteId() { return societeId; }
    public void setSocieteId(Integer societeId) { this.societeId = societeId; }
    public Integer getBusVoyageId() { return busVoyageId; }
    public void setBusVoyageId(Integer busVoyageId) { this.busVoyageId = busVoyageId; }
    public LocalDate getDateDiffusion() { return dateDiffusion; }
    public void setDateDiffusion(LocalDate dateDiffusion) { this.dateDiffusion = dateDiffusion; }
    public LocalTime getHeureDiffusion() { return heureDiffusion; }
    public void setHeureDiffusion(LocalTime heureDiffusion) { this.heureDiffusion = heureDiffusion; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Double getPaymentAmount() { return paymentAmount; }
    public void setPaymentAmount(Double paymentAmount) { this.paymentAmount = paymentAmount; }
}
