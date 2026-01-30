package com.itu.taxi_brousse.service;

import com.itu.taxi_brousse.entity.Diffusion;
import com.itu.taxi_brousse.entity.DiffusionConf;
import com.itu.taxi_brousse.repository.DiffusionConfRepository;
import com.itu.taxi_brousse.repository.DiffusionRepository;
import com.itu.taxi_brousse.repository.DiffusionPaiementRepository;
import com.itu.taxi_brousse.repository.SocieteRepository;
import com.itu.taxi_brousse.repository.BusVoyageRepository;
import com.itu.taxi_brousse.dto.BulkDiffusionRequest;
import com.itu.taxi_brousse.entity.DiffusionPaiement;
import com.itu.taxi_brousse.entity.Societe;
import com.itu.taxi_brousse.entity.BusVoyage;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.stream.Collectors;
import com.itu.taxi_brousse.entity.Facture;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Async;

@Service
@RequiredArgsConstructor

@Transactional(readOnly = true)
public class DiffusionService {
    private final DiffusionRepository diffusionRepository;
    private final DiffusionConfRepository diffusionConfRepository;
    private final DiffusionPaiementRepository diffusionPaiementRepository;
    private final SocieteRepository societeRepository;
    private final BusVoyageRepository busVoyageRepository;
    private final ApplicationContext applicationContext;

    public List<Diffusion> getListDiffusion() {
        return diffusionRepository.findAll();
    }

    public List<Diffusion> getListDiffusionByIdSociete(Integer idSociete) {
        return diffusionRepository.findBySocieteId(idSociete);
    }

    public List<Diffusion> filterDiffusionByHeure(LocalTime heureMin, LocalTime heureMax, List<Diffusion> diffs) {
        if (heureMin == null && heureMax == null) return diffs;
        return diffs.stream()
                .filter(d -> {
                    LocalTime h = d.getHeureDiffusion();
                    if (h == null) return false;
                    if (heureMin != null && h.isBefore(heureMin)) return false;
                    if (heureMax != null && h.isAfter(heureMax)) return false;
                    return true;
                })
                .collect(Collectors.toList());
    }

    public List<Diffusion> filterDiffusionByDate(LocalDate dateMin, LocalDate dateMax, List<Diffusion> diffs) {
        if (dateMin == null && dateMax == null) return diffs;
        return diffs.stream()
                .filter(d -> {
                    LocalDate dt = d.getDateDiffusion();
                    if (dt == null) return false;
                    if (dateMin != null && dt.isBefore(dateMin)) return false;
                    if (dateMax != null && dt.isAfter(dateMax)) return false;
                    return true;
                })
                .collect(Collectors.toList());
    }

    public Double getPrixDiffusion(LocalDate date) {
        if (date == null) return 0.0;
        Optional<DiffusionConf> conf = diffusionConfRepository.findTopByDateDebutLessThanEqualAndDateFinGreaterThanEqualOrderByDateDebutDesc(date, date);
        return conf.map(DiffusionConf::getPrix).orElse(0.0);
    }

    public Double getChiffreAffaireDiffusion(List<Diffusion> filteredDiffs) {
        if (filteredDiffs == null || filteredDiffs.isEmpty()) return 0.0;
        double sum = 0.0;
        for (Diffusion d : filteredDiffs) {
            LocalDate date = d.getDateDiffusion();
            sum += getPrixDiffusion(date);
        }
        return sum;
    }

    //?==== Alea Week 3
    @Transactional
    public List<Diffusion> createBulkDiffusions(BulkDiffusionRequest req) {
        if (req.getQuantity() == null || req.getQuantity() <= 0) throw new IllegalArgumentException("Quantity must be > 0");
        Societe societe = societeRepository.findById(req.getSocieteId())
                .orElseThrow(() -> new IllegalArgumentException("Societe not found"));
        BusVoyage busVoyage = busVoyageRepository.findById(req.getBusVoyageId())
                .orElseThrow(() -> new IllegalArgumentException("BusVoyage not found"));

        // create N diffusions (same date/time as requested)
        List<Diffusion> created = new ArrayList<>();
        for (int i = 0; i < req.getQuantity(); i++) {
            Diffusion d = Diffusion.builder()
                    .dateDiffusion(req.getDateDiffusion())
                    .heureDiffusion(req.getHeureDiffusion())
                    .description("Bulk: " + (req.getQuantity()) + " diffusions")
                    .societe(societe)
                    .busVoyage(busVoyage)
                    .build();
            diffusionRepository.save(d);
            created.add(d);
        }

        // If payment provided, allocate using Prorata Proportionnel
        Double payment = req.getPaymentAmount();
        if (payment != null && payment > 0) {
            //*-- Calculate INITIAL total due for prorata base
            double initialTotalDue = 0.0;
            Map<Diffusion, Double> diffusionPrices = new LinkedHashMap<>();
            
            for (Diffusion d : created) {
                double price = getPrixDiffusion(d.getDateDiffusion());
                diffusionPrices.put(d, price);
                initialTotalDue += price;
            }
            
            if (payment > initialTotalDue) {
                throw new IllegalArgumentException("Payment cannot exceed total due");
            }

            //*-- Apply prorata proportionnel using INITIAL total
            for (Map.Entry<Diffusion, Double> entry : diffusionPrices.entrySet()) {
                Diffusion d = entry.getKey();
                Double price = entry.getValue();
                
                //*-- Prorata: (individual_price / initial_total) * payment
                Double proportionalPayment = (price / initialTotalDue) * payment;
                
                DiffusionPaiement p = DiffusionPaiement.builder()
                        .montantPaye(proportionalPayment)
                        .datePaiement(LocalDate.now())
                        .diffusion(d)
                        .societe(societe)
                        .build();
                diffusionPaiementRepository.save(p);
            }
        }

        return created;
    }

    public double getPaidAmountForDiffusion(Diffusion d) {
        if (d == null || d.getId() == null) return 0.0;
        return diffusionPaiementRepository.sumPaidAmountByDiffusionId(d.getId());
    }
    
    public double getRemainingForSociety(Integer societeId) {
        if (societeId == null) return 0.0;

        //?=== Single query to get total due
        double totalDue = diffusionRepository.findBySocieteId(societeId).stream()
            .mapToDouble(d -> getPrixDiffusion(d.getDateDiffusion()))
            .sum();

        //?=== Single query to get all payments (both diffusion-specific AND society-level)
        double totalPaid = diffusionPaiementRepository
            .findBySocieteId(societeId)
            .stream()
            .mapToDouble(p -> p.getMontantPaye() != null ? p.getMontantPaye() : 0.0)
            .sum();

        return totalDue - totalPaid;
    }

    //?=== Alea Week 3 Suite - Prorata Proportionnel
    @Transactional
    public void applyPaymentToSociety(Integer societeId, Double amount) {
        //*-- Validation
        if (societeId == null) throw new IllegalArgumentException("Societe required");
        if (amount == null || amount <= 0) throw new IllegalArgumentException("Amount must be > 0");

        //*-- Get all diffusions for this society
        List<Diffusion> allDiffs = diffusionRepository.findBySocieteIdOrderByIdAsc(societeId);
        
        if (allDiffs.isEmpty()) {
            throw new IllegalArgumentException("Aucune diffusion trouvée pour cette société");
        }

        //*-- Fetch society once
        Societe societe = societeRepository.findById(societeId)
            .orElseThrow(() -> new IllegalArgumentException("Societe not found"));

        // Build remaining map per diffusion (price - alreadyPaid)
        Map<Diffusion, BigDecimal> remainingMap = new LinkedHashMap<>();
        BigDecimal sumRemaining = BigDecimal.ZERO;
        for (Diffusion d : allDiffs) {
            BigDecimal price = BigDecimal.valueOf(getPrixDiffusion(d.getDateDiffusion()));
            BigDecimal paid = BigDecimal.valueOf(getPaidAmountForDiffusion(d));
            BigDecimal remainingForThis = price.subtract(paid);
            if (remainingForThis.compareTo(BigDecimal.ZERO) < 0) remainingForThis = BigDecimal.ZERO;
            remainingMap.put(d, remainingForThis);
            sumRemaining = sumRemaining.add(remainingForThis);
        }

        if (sumRemaining.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Cette société n'a aucune diffusion à régulariser");
        }

        BigDecimal amountBD = BigDecimal.valueOf(amount);
        if (amountBD.compareTo(sumRemaining) > 0) {
            throw new IllegalArgumentException("Payment cannot exceed remaining total: " + sumRemaining);
        }

        // Allocate proportionally to remaining amounts (safe against overpay)
        BigDecimal allocatedSum = BigDecimal.ZERO;
        Diffusion lastAllocatedDiffusion = null;

        Set<Integer> affectedBusVoyageIds = new HashSet<>();
        for (Map.Entry<Diffusion, BigDecimal> e : remainingMap.entrySet()) {
            Diffusion d = e.getKey();
            BigDecimal rem = e.getValue();
            if (rem.compareTo(BigDecimal.ZERO) <= 0) continue; // skip already settled

            // proportional share = (rem / sumRemaining) * amount
            BigDecimal percentage = rem.divide(sumRemaining, 10, RoundingMode.HALF_UP);
            BigDecimal share = percentage
                    .multiply(amountBD)
                    .setScale(2, RoundingMode.HALF_UP);

            // Do not exceed the diffusion's remaining due (cap if rounding pushed it slightly over)
            if (share.compareTo(rem) > 0) {
                share = rem.setScale(2, RoundingMode.HALF_UP);
            }

            if (share.compareTo(BigDecimal.ZERO) > 0) {
                DiffusionPaiement p = DiffusionPaiement.builder()
                        .montantPaye(share.doubleValue())
                        .datePaiement(LocalDate.now())
                        .diffusion(d)
                        .societe(societe)
                        .build();
                diffusionPaiementRepository.save(p);
                allocatedSum = allocatedSum.add(share);
                lastAllocatedDiffusion = d;
                if (d.getBusVoyage() != null && d.getBusVoyage().getId() != null) {
                    affectedBusVoyageIds.add(d.getBusVoyage().getId());
                }
            }
        }

        // Handle small rounding residue by adding it to the last allocated diffusion
        BigDecimal residue = amountBD.subtract(allocatedSum).setScale(2, RoundingMode.HALF_UP);
        if (residue.compareTo(BigDecimal.ZERO) != 0) {
            if (lastAllocatedDiffusion != null) {
                DiffusionPaiement p = DiffusionPaiement.builder()
                        .montantPaye(residue.doubleValue())
                        .datePaiement(LocalDate.now())
                        .diffusion(lastAllocatedDiffusion)
                        .societe(societe)
                        .build();
                diffusionPaiementRepository.save(p);
                residue = BigDecimal.ZERO;
                if (lastAllocatedDiffusion.getBusVoyage() != null && lastAllocatedDiffusion.getBusVoyage().getId() != null) {
                    affectedBusVoyageIds.add(lastAllocatedDiffusion.getBusVoyage().getId());
                }
            } else {
                throw new IllegalStateException("Unable to allocate payment residue");
            }
        }

        // if (!affectedBusVoyageIds.isEmpty()) {
        //     refreshFacturesForBusVoyageIds(affectedBusVoyageIds);
        // }
    }

    //?=== Helper: refresh (generate if needed) factures for a set of BusVoyage ids
    @Async
    private void refreshFacturesForBusVoyageIds(Set<Integer> busVoyageIds) {
        FactureService factureService = applicationContext.getBean(FactureService.class);
        for (Integer busVoyageId : busVoyageIds) {
            BusVoyage bv = busVoyageRepository.findById(busVoyageId).orElse(null);
            if (bv == null) continue;

            Facture facture = factureService.generateFacture(bv);
            if (facture != null && facture.getId() != null) {
                try {
                    factureService.refreshFacture(facture.getId());
                } catch (Exception ex) {
                    System.err.println("Failed to refresh facture for busVoyageId=" + busVoyageId + ": " + ex.getMessage());
                }
            }
        }
    }
}