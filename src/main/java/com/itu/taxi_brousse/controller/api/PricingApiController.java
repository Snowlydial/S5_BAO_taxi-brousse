package com.itu.taxi_brousse.controller.api;

import com.itu.taxi_brousse.entity.CategorieGroupeAge;
import com.itu.taxi_brousse.entity.ClassePlace;
import com.itu.taxi_brousse.entity.Voyage;
import com.itu.taxi_brousse.repository.CategorieGroupeAgeRepository;
import com.itu.taxi_brousse.repository.ClassePlaceRepository;
import com.itu.taxi_brousse.repository.VoyageRepository;
import com.itu.taxi_brousse.service.PricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/pricing")
@RequiredArgsConstructor
public class PricingApiController {
    
    private final PricingService pricingService;
    private final CategorieGroupeAgeRepository categorieGroupeAgeRepository;
    private final ClassePlaceRepository classePlaceRepository;
    private final VoyageRepository voyageRepository;
    
    //?=== Get price matrix for a specific voyage: PlaceType -> AgeGroup -> Price
    // Returns: {placeTypeId: {ageGroupId: {price, hasDiscount, percentage, basePrice}}}
    @GetMapping("/voyage/{voyageId}")
    public ResponseEntity<Map<Integer, Map<Integer, Map<String, Object>>>> getVoyagePricingMatrix(
            @PathVariable Integer voyageId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        Voyage voyage = voyageRepository.findById(voyageId)
                .orElseThrow(() -> new RuntimeException("Voyage not found"));
        
        LocalDate pricingDate = date != null ? date : LocalDate.now();
        
        List<ClassePlace> classePlaces = classePlaceRepository.findAll();
        List<CategorieGroupeAge> ageGroups = categorieGroupeAgeRepository.findAll();
        
        //*-- Build matrix: PlaceType -> AgeGroup -> PriceInfo
        Map<Integer, Map<Integer, Map<String, Object>>> pricingMatrix = new LinkedHashMap<>();
        
        for (ClassePlace classePlace : classePlaces) {
            Map<Integer, Map<String, Object>> ageGroupPrices = new LinkedHashMap<>();
            
            for (CategorieGroupeAge ageGroup : ageGroups) {
                Double effectivePrice = pricingService.getEffectivePrice(voyage, ageGroup, classePlace, pricingDate);
                boolean hasDiscount = pricingService.hasDiscount(voyage, ageGroup, classePlace, pricingDate);
                Double percentage = pricingService.getDiscountPercentage(voyage, ageGroup, classePlace, pricingDate);
                Double baselinePrice = pricingService.getBaselinePrice(voyage, classePlace, pricingDate);
                
                Map<String, Object> priceInfo = new HashMap<>();
                priceInfo.put("price", effectivePrice);
                priceInfo.put("hasDiscount", hasDiscount);
                priceInfo.put("percentage", percentage);
                priceInfo.put("basePrice", baselinePrice);
                priceInfo.put("ageGroupLabel", ageGroup.getLibelle());
                
                ageGroupPrices.put(ageGroup.getId(), priceInfo);
            }
            
            pricingMatrix.put(classePlace.getId(), ageGroupPrices);
        }
        
        return ResponseEntity.ok(pricingMatrix);
    }
    
    //?=== Get price ranges for each place type on a voyage
    // Returns: {placeTypeId: {placeTypeLabel, minPrice, maxPrice, pricesByAge: [{ageGroup, price}]}}
    @GetMapping("/voyage/{voyageId}/ranges")
    public ResponseEntity<Map<Integer, Map<String, Object>>> getVoyagePriceRanges(
            @PathVariable Integer voyageId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        Voyage voyage = voyageRepository.findById(voyageId)
                .orElseThrow(() -> new RuntimeException("Voyage not found"));
        
        LocalDate pricingDate = date != null ? date : LocalDate.now();
        
        List<ClassePlace> classePlaces = classePlaceRepository.findAll();
        List<CategorieGroupeAge> ageGroups = categorieGroupeAgeRepository.findAll();
        
        Map<Integer, Map<String, Object>> priceRanges = new LinkedHashMap<>();
        
        for (ClassePlace classePlace : classePlaces) {
            List<Map<String, Object>> pricesByAge = new ArrayList<>();
            Double minPrice = null;
            Double maxPrice = null;
            
            for (CategorieGroupeAge ageGroup : ageGroups) {
                Double price = pricingService.getEffectivePrice(voyage, ageGroup, classePlace, pricingDate);
                
                if (price != null && price > 0) {
                    Map<String, Object> agePrice = new HashMap<>();
                    agePrice.put("ageGroup", ageGroup.getLibelle());
                    agePrice.put("ageGroupId", ageGroup.getId());
                    agePrice.put("price", price);
                    pricesByAge.add(agePrice);
                    
                    if (minPrice == null || price < minPrice) {
                        minPrice = price;
                    }
                    if (maxPrice == null || price > maxPrice) {
                        maxPrice = price;
                    }
                }
            }
            
            if (!pricesByAge.isEmpty()) {
                Map<String, Object> rangeInfo = new LinkedHashMap<>();
                rangeInfo.put("placeTypeLabel", classePlace.getLibelle());
                rangeInfo.put("minPrice", minPrice != null ? minPrice : 0.0);
                rangeInfo.put("maxPrice", maxPrice != null ? maxPrice : 0.0);
                rangeInfo.put("pricesByAge", pricesByAge);
                
                priceRanges.put(classePlace.getId(), rangeInfo);
            }
        }
        
        return ResponseEntity.ok(priceRanges);
    }
    
    //?=== LEGACY: Get effective prices for all seat classes for a specific age group and date (without voyage)
    // Returns: {classePlaceId: {price, hasDiscount, percentage}}
    @GetMapping
    public ResponseEntity<Map<Integer, Map<String, Object>>> getPricing(
            @RequestParam Integer ageGroupId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        CategorieGroupeAge ageGroup = categorieGroupeAgeRepository.findById(ageGroupId)
                .orElseThrow(() -> new RuntimeException("Age group not found"));
        
        List<ClassePlace> classePlaces = classePlaceRepository.findAll();
        
        Map<Integer, Map<String, Object>> pricingData = new HashMap<>();
        
        for (ClassePlace classePlace : classePlaces) {
            Double effectivePrice = pricingService.getEffectivePrice(ageGroup, classePlace, date);
            boolean hasDiscount = pricingService.hasDiscount(ageGroup, classePlace, date);
            Double percentage = pricingService.getDiscountPercentage(ageGroup, classePlace, date);
            Double baselinePrice = pricingService.getBaselinePrice(classePlace, date);
            
            Map<String, Object> priceInfo = new HashMap<>();
            priceInfo.put("price", effectivePrice);
            priceInfo.put("hasDiscount", hasDiscount);
            priceInfo.put("percentage", percentage);
            priceInfo.put("basePrice", baselinePrice);
            
            pricingData.put(classePlace.getId(), priceInfo);
        }
        
        return ResponseEntity.ok(pricingData);
    }
    
    //?=== Get effective price for a specific combination (voyage-specific)
    @GetMapping("/single")
    public ResponseEntity<Map<String, Object>> getSinglePrice(
            @RequestParam Integer voyageId,
            @RequestParam Integer ageGroupId,
            @RequestParam Integer classePlaceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        Voyage voyage = voyageRepository.findById(voyageId)
                .orElseThrow(() -> new RuntimeException("Voyage not found"));
        
        CategorieGroupeAge ageGroup = categorieGroupeAgeRepository.findById(ageGroupId)
                .orElseThrow(() -> new RuntimeException("Age group not found"));
        
        ClassePlace classePlace = classePlaceRepository.findById(classePlaceId)
                .orElseThrow(() -> new RuntimeException("Seat class not found"));
        
        Double effectivePrice = pricingService.getEffectivePrice(voyage, ageGroup, classePlace, date);
        boolean hasDiscount = pricingService.hasDiscount(voyage, ageGroup, classePlace, date);
        Double percentage = pricingService.getDiscountPercentage(voyage, ageGroup, classePlace, date);
        Double basePrice = pricingService.getBaselinePrice(voyage, classePlace, date);

        Map<String, Object> response = new HashMap<>();
        response.put("price", effectivePrice);
        response.put("hasDiscount", hasDiscount);
        response.put("percentage", percentage);
        response.put("basePrice", basePrice);
        response.put("ageGroupLabel", ageGroup.getLibelle());
        response.put("classePlaceLabel", classePlace.getLibelle());
        response.put("voyageLabel", voyage.getGareDepart().getLibelle() + " → " + voyage.getGareArrivee().getLibelle());
        
        return ResponseEntity.ok(response);
    }
}