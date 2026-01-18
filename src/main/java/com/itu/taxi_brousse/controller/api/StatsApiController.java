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

    //?=== API endpoint for fetching stats
    @GetMapping("/api/stats")
    @ResponseBody
    public ResponseEntity<DashboardStatsDTO> getStats(@RequestParam String periodType, @RequestParam(required = false) Integer year,
                                                      @RequestParam(required = false) Integer yearMin, @RequestParam(required = false) Integer yearMax) {
        
        DashboardStatsDTO stats;
        
        if ("monthly".equals(periodType)) {
            if (year == null) {
                year = LocalDate.now().getYear();
            }
            
            stats = DashboardStatsDTO.builder()
                    .periodType("monthly")
                    .year(year)
                    .globalRevenue(statService.getMonthlyGlobalRevenue(year))
                    .revenueByCaisse(statService.getMonthlyRevenueByCaisse(year))
                    .topVoyages(statService.getMostReservedVoyages(year, year, 5))
                    .topClients(statService.getMostLucrativeClients(year, year, 10))
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
                    .globalRevenue(statService.getYearlyGlobalRevenue(yearMin, yearMax))
                    .revenueByCaisse(statService.getYearlyRevenueByCaisse(yearMin, yearMax))
                    .topVoyages(statService.getMostReservedVoyages(yearMin, yearMax, 5))
                    .topClients(statService.getMostLucrativeClients(yearMin, yearMax, 10))
                    .genderUsage(statService.getUsageByGender(yearMin, yearMax))
                    .ageGroupUsage(statService.getUsageByAgeGroup(yearMin, yearMax))
                    .build();
        }
        
        return ResponseEntity.ok(stats);
    }
}