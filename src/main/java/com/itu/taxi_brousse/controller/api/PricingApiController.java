package com.itu.taxi_brousse.controller.api;

import com.itu.taxi_brousse.entity.CategorieGroupeAge;
import com.itu.taxi_brousse.entity.ClassePlace;
import com.itu.taxi_brousse.repository.CategorieGroupeAgeRepository;
import com.itu.taxi_brousse.repository.ClassePlaceRepository;
import com.itu.taxi_brousse.service.PricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pricing")
@RequiredArgsConstructor
public class PricingApiController {
    
    private final PricingService pricingService;
    private final CategorieGroupeAgeRepository categorieGroupeAgeRepository;
    private final ClassePlaceRepository classePlaceRepository;
    
    //?=== Get effective prices for all seat classes for a specific age group and date
    // Returns: {classePlaceId: {price, hasDiscount, percentage}}
    @GetMapping
    public ResponseEntity<Map<Integer, Map<String, Object>>> getPricing(@RequestParam Integer ageGroupId, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        CategorieGroupeAge ageGroup = categorieGroupeAgeRepository.findById(ageGroupId)
                .orElseThrow(() -> new RuntimeException("Age group not found"));
        
        List<ClassePlace> classePlaces = classePlaceRepository.findAll();
        
        Map<Integer, Map<String, Object>> pricingData = new HashMap<>();
        
        for (ClassePlace classePlace : classePlaces) {
            Double effectivePrice = pricingService.getEffectivePrice(ageGroup, classePlace, date);
            boolean hasDiscount = pricingService.hasDiscount(ageGroup, classePlace, date);
            Double percentage = pricingService.getDiscountPercentage(ageGroup, classePlace, date);
            
            Map<String, Object> priceInfo = new HashMap<>();
            priceInfo.put("price", effectivePrice);
            priceInfo.put("hasDiscount", hasDiscount);
            priceInfo.put("percentage", percentage);
            priceInfo.put("basePrice", classePlace.getPrixPlace());
            
            pricingData.put(classePlace.getId(), priceInfo);
        }
        
        return ResponseEntity.ok(pricingData);
    }
    
    //?=== Get effective price for a specific combination
    @GetMapping("/single")
    public ResponseEntity<Map<String, Object>> getSinglePrice(
            @RequestParam Integer ageGroupId,
            @RequestParam Integer classePlaceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        CategorieGroupeAge ageGroup = categorieGroupeAgeRepository.findById(ageGroupId)
                .orElseThrow(() -> new RuntimeException("Age group not found"));
        
        ClassePlace classePlace = classePlaceRepository.findById(classePlaceId)
                .orElseThrow(() -> new RuntimeException("Seat class not found"));
        
        Double effectivePrice = pricingService.getEffectivePrice(ageGroup, classePlace, date);
        boolean hasDiscount = pricingService.hasDiscount(ageGroup, classePlace, date);
        Double percentage = pricingService.getDiscountPercentage(ageGroup, classePlace, date);
        
        Map<String, Object> response = new HashMap<>();
        response.put("price", effectivePrice);
        response.put("hasDiscount", hasDiscount);
        response.put("percentage", percentage);
        response.put("basePrice", classePlace.getPrixPlace());
        response.put("ageGroupLabel", ageGroup.getLibelle());
        response.put("classePlaceLabel", classePlace.getLibelle());
        
        return ResponseEntity.ok(response);
    }
}