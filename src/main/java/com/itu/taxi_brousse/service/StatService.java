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
    private final PricingService pricingService;
    
    //? ========== CHIFFRE D'AFFAIRES (DYNAMIC) ==========
    
    //?=== Get revenue by caisse for monthly stats (DYNAMIC)
    public List<RevenueByCaisseDTO> getMonthlyRevenueByCaisse(int year, LocalDate referenceDate) {
        LocalDateTime startDate = LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(year, 12, 31, 23, 59, 59);
        
        List<Caisse> caisses = caisseRepository.findAll();
        List<RevenueByCaisseDTO> results = new ArrayList<>();
        
        for (Caisse caisse : caisses) {
            List<Paiement> paiements = paiementRepository.findByCaisseAndDatePaiementBetween(
                caisse, startDate, endDate
            );
            
            Map<Integer, Double> monthlyRevenue = new TreeMap<>();
            for (int month = 1; month <= 12; month++) {
                monthlyRevenue.put(month, 0.0);
            }
            
            for (Paiement p : paiements) {
                Reservation res = p.getReservation();
                int month = p.getDatePaiement().getMonthValue();
                Double dynamicPrice = pricingService.calculatePrice(res, referenceDate);
                monthlyRevenue.merge(month, dynamicPrice, Double::sum);
            }
            
            results.add(new RevenueByCaisseDTO(caisse.getLibelle(), monthlyRevenue));
        }
        
        return results;
    }
    
    //?=== Get revenue by caisse for yearly stats (DYNAMIC)
    public List<RevenueByCaisseDTO> getYearlyRevenueByCaisse(int yearMin, int yearMax) {
        LocalDateTime startDate = LocalDateTime.of(yearMin, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(yearMax, 12, 31, 23, 59, 59);
        
        List<Caisse> caisses = caisseRepository.findAll();
        List<RevenueByCaisseDTO> results = new ArrayList<>();
        
        for (Caisse caisse : caisses) {
            List<Paiement> paiements = paiementRepository.findByCaisseAndDatePaiementBetween(
                caisse, startDate, endDate
            );
            
            Map<Integer, Double> yearlyRevenue = new TreeMap<>();
            for (int year = yearMin; year <= yearMax; year++) {
                yearlyRevenue.put(year, 0.0);
            }
            
            for (Paiement p : paiements) {
                Reservation res = p.getReservation();
                int year = p.getDatePaiement().getYear();
                Double dynamicPrice = pricingService.calculatePrice(res, LocalDate.now());
                yearlyRevenue.merge(year, dynamicPrice, Double::sum);
            }
            
            results.add(new RevenueByCaisseDTO(caisse.getLibelle(), yearlyRevenue));
        }
        
        return results;
    }
    
    //?=== Get global revenue evolution (monthly) - DYNAMIC
    public Map<Integer, Double> getMonthlyGlobalRevenue(int year) {
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);
        
        List<BusVoyage> busVoyages = busVoyageRepository.findByDateDepartBetween(startDate, endDate);
        
        Map<Integer, Double> monthlyRevenue = new TreeMap<>();
        for (int month = 1; month <= 12; month++) {
            monthlyRevenue.put(month, 0.0);
        }
        
        for (BusVoyage bv : busVoyages) {
            List<Reservation> reservations = reservationRepository.findByBusVoyage(bv);
            int month = bv.getDateDepart().getMonthValue();
            
            for (Reservation res : reservations) {
                Double dynamicPrice = pricingService.calculatePrice(res, LocalDate.now());
                monthlyRevenue.merge(month, dynamicPrice, Double::sum);
            }
        }
        
        return monthlyRevenue;
    }
    
    //?=== Get global revenue evolution (yearly) - DYNAMIC
    public Map<Integer, Double> getYearlyGlobalRevenue(int yearMin, int yearMax) {
        LocalDate startDate = LocalDate.of(yearMin, 1, 1);
        LocalDate endDate = LocalDate.of(yearMax, 12, 31);
        
        List<BusVoyage> busVoyages = busVoyageRepository.findByDateDepartBetween(startDate, endDate);
        
        Map<Integer, Double> yearlyRevenue = new TreeMap<>();
        for (int year = yearMin; year <= yearMax; year++) {
            yearlyRevenue.put(year, 0.0);
        }
        
        for (BusVoyage bv : busVoyages) {
            List<Reservation> reservations = reservationRepository.findByBusVoyage(bv);
            int year = bv.getDateDepart().getYear();
            
            for (Reservation res : reservations) {
                Double dynamicPrice = pricingService.calculatePrice(res, LocalDate.now());
                yearlyRevenue.merge(year, dynamicPrice, Double::sum);
            }
        }
        
        return yearlyRevenue;
    }
    
    //?=== Get most lucrative clients (DYNAMIC)
    public List<ClientRevenueDTO> getMostLucrativeClients(int yearMin, int yearMax, int limit) {
        LocalDate startDate = LocalDate.of(yearMin, 1, 1);
        LocalDate endDate = LocalDate.of(yearMax, 12, 31);
        
        List<Client> clients = clientRepository.findAll();
        List<ClientRevenueDTO> results = new ArrayList<>();
        
        for (Client client : clients) {
            List<Reservation> reservations = reservationRepository.findByClient(client);
            
            double totalRevenue = 0.0;
            for (Reservation res : reservations) {
                if (!res.getBusVoyage().getDateDepart().isBefore(startDate) && 
                    !res.getBusVoyage().getDateDepart().isAfter(endDate)) {
                    totalRevenue += pricingService.calculatePrice(res, LocalDate.now());
                }
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
    
    //? ========== MONTANT ENCAISSÉ (RECORDED PAYMENTS) ==========
    
    //?=== Get PAID revenue by caisse (monthly)
    public List<RevenueByCaisseDTO> getMonthlyPaidRevenueByCaisse(int year) {
        LocalDateTime startDate = LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(year, 12, 31, 23, 59, 59);
        
        List<Caisse> caisses = caisseRepository.findAll();
        List<RevenueByCaisseDTO> results = new ArrayList<>();
        
        for (Caisse caisse : caisses) {
            List<Paiement> paiements = paiementRepository.findByCaisseAndDatePaiementBetween(
                caisse, startDate, endDate
            );
            
            Map<Integer, Double> monthlyRevenue = new TreeMap<>();
            for (int month = 1; month <= 12; month++) {
                monthlyRevenue.put(month, 0.0);
            }
            
            for (Paiement p : paiements) {
                int month = p.getDatePaiement().getMonthValue();
                monthlyRevenue.merge(month, p.getMontantPaye(), Double::sum);
            }
            
            results.add(new RevenueByCaisseDTO(caisse.getLibelle(), monthlyRevenue));
        }
        
        return results;
    }
    
    //?=== Get PAID revenue by caisse (yearly)
    public List<RevenueByCaisseDTO> getYearlyPaidRevenueByCaisse(int yearMin, int yearMax) {
        LocalDateTime startDate = LocalDateTime.of(yearMin, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(yearMax, 12, 31, 23, 59, 59);
        
        List<Caisse> caisses = caisseRepository.findAll();
        List<RevenueByCaisseDTO> results = new ArrayList<>();
        
        for (Caisse caisse : caisses) {
            List<Paiement> paiements = paiementRepository.findByCaisseAndDatePaiementBetween(
                caisse, startDate, endDate
            );
            
            Map<Integer, Double> yearlyRevenue = new TreeMap<>();
            for (int year = yearMin; year <= yearMax; year++) {
                yearlyRevenue.put(year, 0.0);
            }
            
            for (Paiement p : paiements) {
                int year = p.getDatePaiement().getYear();
                yearlyRevenue.merge(year, p.getMontantPaye(), Double::sum);
            }
            
            results.add(new RevenueByCaisseDTO(caisse.getLibelle(), yearlyRevenue));
        }
        
        return results;
    }
    
    //?=== Get PAID global revenue (monthly)
    public Map<Integer, Double> getMonthlyPaidRevenue(int year) {
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
    
    //?=== Get PAID global revenue (yearly)
    public Map<Integer, Double> getYearlyPaidRevenue(int yearMin, int yearMax) {
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
    
    //?=== Get most PAYING clients (based on actual payments)
    public List<ClientRevenueDTO> getMostPayingClients(int yearMin, int yearMax, int limit) {
        LocalDateTime startDate = LocalDateTime.of(yearMin, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(yearMax, 12, 31, 23, 59, 59);
        
        List<Client> clients = clientRepository.findAll();
        List<ClientRevenueDTO> results = new ArrayList<>();
        
        for (Client client : clients) {
            List<Reservation> reservations = reservationRepository.findByClient(client);
            
            double totalPaid = 0.0;
            for (Reservation res : reservations) {
                List<Paiement> paiements = paiementRepository.findByReservationAndDatePaiementBetween(
                    res, startDate, endDate
                );
                totalPaid += paiements.stream()
                        .mapToDouble(Paiement::getMontantPaye)
                        .sum();
            }
            
            if (totalPaid > 0) {
                results.add(new ClientRevenueDTO(
                    client.getNom() + " " + client.getPrenom(),
                    totalPaid
                ));
            }
        }
        
        return results.stream()
                .sorted(Comparator.comparing(ClientRevenueDTO::getTotalRevenue).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    //? ========== SHARED STATS (NOT REVENUE-DEPENDENT) ==========
    
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
    
    //?=== Get usage by gender
    public Map<String, Long> getUsageByGender(int yearMin, int yearMax) {
        LocalDate startDate = LocalDate.of(yearMin, 1, 1);
        LocalDate endDate = LocalDate.of(yearMax, 12, 31);
        
        List<BusVoyage> busVoyages = busVoyageRepository.findByDateDepartBetween(startDate, endDate);
        List<Reservation> allReservations = new ArrayList<>();
        
        for (BusVoyage bv : busVoyages) {
            allReservations.addAll(reservationRepository.findByBusVoyage(bv));
        }
        
        return allReservations.stream()
                .collect(Collectors.groupingBy(
                    r -> r.getClient().getCategorieGenre().getLibelle(),
                    Collectors.counting()
                ));
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
        
        return allReservations.stream()
                .collect(Collectors.groupingBy(
                    r -> r.getClient().getCategorieGroupeAge().getLibelle(),
                    Collectors.counting()
                ));
    }
}