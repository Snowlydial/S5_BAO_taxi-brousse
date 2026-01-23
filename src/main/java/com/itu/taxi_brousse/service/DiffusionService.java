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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor

@Transactional(readOnly = true)
public class DiffusionService {
    private final DiffusionRepository diffusionRepository;
    private final DiffusionConfRepository diffusionConfRepository;
    private final DiffusionPaiementRepository diffusionPaiementRepository;
    private final SocieteRepository societeRepository;
    private final BusVoyageRepository busVoyageRepository;

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

    @Transactional
    public List<Diffusion> createBulkDiffusions(BulkDiffusionRequest req) {
        if (req.getQuantity() == null || req.getQuantity() <= 0) throw new IllegalArgumentException("Quantity must be > 0");
        Societe societe = societeRepository.findById(req.getSocieteId())
                .orElseThrow(() -> new IllegalArgumentException("Societe not found"));
        BusVoyage busVoyage = busVoyageRepository.findById(req.getBusVoyageId())
                .orElseThrow(() -> new IllegalArgumentException("BusVoyage not found"));

        // create N diffusions (same date/time as requested)
        List<Diffusion> created = new java.util.ArrayList<>();
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

        // If payment provided, allocate using FIFO across created diffusions
        Double payment = req.getPaymentAmount();
        if (payment != null && payment > 0) {
            // compute total due
            double totalDue = 0.0;
            for (Diffusion d : created) totalDue += getPrixDiffusion(d.getDateDiffusion());
            if (payment > totalDue) throw new IllegalArgumentException("Payment cannot exceed total due");

            double remaining = payment;
            for (Diffusion d : created) {
                if (remaining <= 0) break;
                double price = getPrixDiffusion(d.getDateDiffusion());
                double toPay = Math.min(price, remaining);
                DiffusionPaiement p = DiffusionPaiement.builder()
                        .montantPaye(toPay)
                        .datePaiement(java.time.LocalDate.now())
                        .diffusion(d)
                        .societe(societe)
                        .build();
                diffusionPaiementRepository.save(p);
                remaining -= toPay;
            }
        }

        return created;
    }
}
