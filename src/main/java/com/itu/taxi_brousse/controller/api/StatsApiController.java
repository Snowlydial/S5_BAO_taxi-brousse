package com.itu.taxi_brousse.controller.api;

import com.itu.taxi_brousse.dto.stats.DashboardStatsDTO;
import com.itu.taxi_brousse.service.StatService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
public class StatsApiController {

    private final StatService statService;

    //?=== API endpoint for fetching stats with toggle for CA vs Montant Encaissé
    @GetMapping("/api/stats")
    @ResponseBody
    public ResponseEntity<DashboardStatsDTO> getStats(
            @RequestParam String periodType, 
            @RequestParam(required = false, defaultValue = "false") Boolean usePaidAmounts,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer yearMin, 
            @RequestParam(required = false) Integer yearMax) {
        
        DashboardStatsDTO stats;
        LocalDate referenceDate = usePaidAmounts ? null : LocalDate.now();
        
        if ("monthly".equals(periodType)) {
            if (year == null) {
                year = LocalDate.now().getYear();
            }
            
            stats = DashboardStatsDTO.builder()
                    .periodType("monthly")
                    .year(year)
                    .globalRevenue(usePaidAmounts ? 
                        statService.getMonthlyPaidRevenue(year) : 
                        statService.getMonthlyGlobalRevenue(year))
                    .revenueByCaisse(usePaidAmounts ?
                        statService.getMonthlyPaidRevenueByCaisse(year) :
                        statService.getMonthlyRevenueByCaisse(year, referenceDate))
                    .topVoyages(statService.getMostReservedVoyages(year, year, 5))
                    .topClients(usePaidAmounts ?
                        statService.getMostPayingClients(year, year, 10) :
                        statService.getMostLucrativeClients(year, year, 10))
                    .genderUsage(statService.getUsageByGender(year, year))
                    .ageGroupUsage(statService.getUsageByAgeGroup(year, year))
                    .build();
        } else {
            if (yearMin == null) yearMin = LocalDate.now().getYear() - 5;
            if (yearMax == null) yearMax = LocalDate.now().getYear();
            
            stats = DashboardStatsDTO.builder()
                    .periodType("yearly")
                    .yearMin(yearMin)
                    .yearMax(yearMax)
                    .globalRevenue(usePaidAmounts ?
                        statService.getYearlyPaidRevenue(yearMin, yearMax) :
                        statService.getYearlyGlobalRevenue(yearMin, yearMax))
                    .revenueByCaisse(usePaidAmounts ?
                        statService.getYearlyPaidRevenueByCaisse(yearMin, yearMax) :
                        statService.getYearlyRevenueByCaisse(yearMin, yearMax))
                    .topVoyages(statService.getMostReservedVoyages(yearMin, yearMax, 5))
                    .topClients(usePaidAmounts ?
                        statService.getMostPayingClients(yearMin, yearMax, 10) :
                        statService.getMostLucrativeClients(yearMin, yearMax, 10))
                    .genderUsage(statService.getUsageByGender(yearMin, yearMax))
                    .ageGroupUsage(statService.getUsageByAgeGroup(yearMin, yearMax))
                    .build();
        }
        
        return ResponseEntity.ok(stats);
    }
}