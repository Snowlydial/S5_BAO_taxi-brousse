package com.itu.taxi_brousse.service;

import com.itu.taxi_brousse.entity.Diffusion;
import com.itu.taxi_brousse.entity.DiffusionConf;
import com.itu.taxi_brousse.repository.DiffusionConfRepository;
import com.itu.taxi_brousse.repository.DiffusionRepository;

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
}
