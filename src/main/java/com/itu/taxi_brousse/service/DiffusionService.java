package com.itu.taxi_brousse.service;

import com.itu.taxi_brousse.entity.Diffusion;
import com.itu.taxi_brousse.entity.BusVoyage;
import com.itu.taxi_brousse.entity.Societe;
import com.itu.taxi_brousse.repository.DiffusionRepository;
import com.itu.taxi_brousse.repository.BusVoyageRepository;
import com.itu.taxi_brousse.repository.SocieteRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class DiffusionService {
    
    private final DiffusionRepository diffusionRepository;
    private final BusVoyageRepository busVoyageRepository;
    private final SocieteRepository societeRepository;
   

}

