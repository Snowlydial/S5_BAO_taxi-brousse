package com.itu.taxi_brousse.service;

import com.itu.taxi_brousse.dto.stats.*;
import com.itu.taxi_brousse.entity.*;
import com.itu.taxi_brousse.repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatService {
    
    private final PaiementRepository paiementRepository;
    private final ReservationRepository reservationRepository;
    private final BusVoyageRepository busVoyageRepository;
    private final ClientRepository clientRepository;
    private final CaisseRepository caisseRepository;
    
    //?=== Get revenue by caisse for monthly stats
    public List<RevenueByCaisseDTO> getMonthlyRevenueByCaisse(int year) {
        LocalDateTime startDate = LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(year, 12, 31, 23, 59, 59);
        
        List<Caisse> caisses = caisseRepository.findAll();
        List<RevenueByCaisseDTO> results = new ArrayList<>();
        
        for (Caisse caisse : caisses) {
            List<Paiement> paiements = paiementRepository.findByCaisseAndDatePaiementBetween(
                caisse, startDate, endDate
            );
            
            // Group by month
            Map<Integer, Double> monthlyRevenue = new TreeMap<>();
            for (int month = 1; month <= 12; month++) {
                monthlyRevenue.put(month, 0.0);
            }
            
            for (Paiement p : paiements) {
                int month = p.getDatePaiement().getMonthValue();
                monthlyRevenue.merge(month, p.getMontantPaye(), Double::sum);
            }
            
            results.add(new RevenueByCaisseDTO(
                caisse.getLibelle(),
                monthlyRevenue
            ));
        }
        
        return results;
    }
    
    //?=== Get revenue by caisse for yearly stats
    public List<RevenueByCaisseDTO> getYearlyRevenueByCaisse(int yearMin, int yearMax) {
        LocalDateTime startDate = LocalDateTime.of(yearMin, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(yearMax, 12, 31, 23, 59, 59);
        
        List<Caisse> caisses = caisseRepository.findAll();
        List<RevenueByCaisseDTO> results = new ArrayList<>();
        
        for (Caisse caisse : caisses) {
            List<Paiement> paiements = paiementRepository.findByCaisseAndDatePaiementBetween(
                caisse, startDate, endDate
            );
            
            // Group by year
            Map<Integer, Double> yearlyRevenue = new TreeMap<>();
            for (int year = yearMin; year <= yearMax; year++) {
                yearlyRevenue.put(year, 0.0);
            }
            
            for (Paiement p : paiements) {
                int year = p.getDatePaiement().getYear();
                yearlyRevenue.merge(year, p.getMontantPaye(), Double::sum);
            }
            
            results.add(new RevenueByCaisseDTO(
                caisse.getLibelle(),
                yearlyRevenue
            ));
        }
        
        return results;
    }
    
    //?=== Get global revenue evolution (monthly)
    public Map<Integer, Double> getMonthlyGlobalRevenue(int year) {
        LocalDateTime startDate = LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(year, 12, 31, 23, 59, 59);
        
        List<Paiement> paiements = paiementRepository.findByDatePaiementBetween(startDate, endDate);
        
        Map<Integer, Double> monthlyRevenue = new TreeMap<>();
        for (int month = 1; month <= 12; month++) {
            monthlyRevenue.put(month, 0.0);
        }
        
        for (Paiement p : paiements) {
            int month = p.getDatePaiement().getMonthValue();
            monthlyRevenue.merge(month, p.getMontantPaye(), Double::sum);
        }
        
        return monthlyRevenue;
    }
    
    //?=== Get global revenue evolution (yearly)
    public Map<Integer, Double> getYearlyGlobalRevenue(int yearMin, int yearMax) {
        LocalDateTime startDate = LocalDateTime.of(yearMin, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(yearMax, 12, 31, 23, 59, 59);
        
        List<Paiement> paiements = paiementRepository.findByDatePaiementBetween(startDate, endDate);
        
        Map<Integer, Double> yearlyRevenue = new TreeMap<>();
        for (int year = yearMin; year <= yearMax; year++) {
            yearlyRevenue.put(year, 0.0);
        }
        
        for (Paiement p : paiements) {
            int year = p.getDatePaiement().getYear();
            yearlyRevenue.merge(year, p.getMontantPaye(), Double::sum);
        }
        
        return yearlyRevenue;
    }
    
    //?=== Get most reserved voyages
    public List<VoyageStatsDTO> getMostReservedVoyages(int yearMin, int yearMax, int limit) {
        LocalDate startDate = LocalDate.of(yearMin, 1, 1);
        LocalDate endDate = LocalDate.of(yearMax, 12, 31);
        
        List<BusVoyage> busVoyages = busVoyageRepository.findByDateDepartBetween(startDate, endDate);
        
        Map<String, VoyageStatsDTO> voyageStats = new HashMap<>();
        
        for (BusVoyage bv : busVoyages) {
            String voyageKey = bv.getVoyage().getGareDepart().getLibelle() + 
                             " → " + 
                             bv.getVoyage().getGareArrivee().getLibelle();
            
            Long reservationCount = reservationRepository.countByBusVoyage(bv);
            
            voyageStats.computeIfAbsent(voyageKey, k -> new VoyageStatsDTO(voyageKey, 0L));
            voyageStats.get(voyageKey).addReservations(reservationCount);
        }
        
        return voyageStats.values().stream()
                .sorted(Comparator.comparing(VoyageStatsDTO::getTotalReservations).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    //?=== Get most lucrative clients
    public List<ClientRevenueDTO> getMostLucrativeClients(int yearMin, int yearMax, int limit) {
        LocalDateTime startDate = LocalDateTime.of(yearMin, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(yearMax, 12, 31, 23, 59, 59);
        
        List<Client> clients = clientRepository.findAll();
        List<ClientRevenueDTO> results = new ArrayList<>();
        
        for (Client client : clients) {
            List<Reservation> reservations = reservationRepository.findByClient(client);
            
            double totalRevenue = 0.0;
            for (Reservation res : reservations) {
                List<Paiement> paiements = paiementRepository.findByReservationAndDatePaiementBetween(
                    res, startDate, endDate
                );
                totalRevenue += paiements.stream()
                        .mapToDouble(Paiement::getMontantPaye)
                        .sum();
            }
            
            if (totalRevenue > 0) {
                results.add(new ClientRevenueDTO(
                    client.getNom() + " " + client.getPrenom(),
                    totalRevenue
                ));
            }
        }
        
        return results.stream()
                .sorted(Comparator.comparing(ClientRevenueDTO::getTotalRevenue).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    //?=== Get usage by gender
    public Map<String, Long> getUsageByGender(int yearMin, int yearMax) {
        LocalDate startDate = LocalDate.of(yearMin, 1, 1);
        LocalDate endDate = LocalDate.of(yearMax, 12, 31);
        
        List<BusVoyage> busVoyages = busVoyageRepository.findByDateDepartBetween(startDate, endDate);
        List<Reservation> allReservations = new ArrayList<>();
        
        for (BusVoyage bv : busVoyages) {
            allReservations.addAll(reservationRepository.findByBusVoyage(bv));
        }
        
        Map<String, Long> genderStats = allReservations.stream()
                .collect(Collectors.groupingBy(
                    r -> r.getClient().getCategorieGenre().getLibelle(),
                    Collectors.counting()
                ));
        
        return genderStats;
    }
    
    //?=== Get usage by age group
    public Map<String, Long> getUsageByAgeGroup(int yearMin, int yearMax) {
        LocalDate startDate = LocalDate.of(yearMin, 1, 1);
        LocalDate endDate = LocalDate.of(yearMax, 12, 31);
        
        List<BusVoyage> busVoyages = busVoyageRepository.findByDateDepartBetween(startDate, endDate);
        List<Reservation> allReservations = new ArrayList<>();
        
        for (BusVoyage bv : busVoyages) {
            allReservations.addAll(reservationRepository.findByBusVoyage(bv));
        }
        
        Map<String, Long> ageGroupStats = allReservations.stream()
                .collect(Collectors.groupingBy(
                    r -> r.getClient().getCategorieGroupeAge().getLibelle(),
                    Collectors.counting()
                ));
        
        return ageGroupStats;
    }
}