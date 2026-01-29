package com.itu.taxi_brousse.controller.api;

import com.itu.taxi_brousse.entity.ClassePlace;
import com.itu.taxi_brousse.repository.ClassePlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/classeplace")
@RequiredArgsConstructor
public class ClassePlaceApiController {
    
    private final ClassePlaceRepository classePlaceRepository;
    
    //?=== Get fallback price for a specific place type (case-insensitive)
    // NOTE: This is now a FALLBACK price - primary pricing comes from ClasseAgeConf
    @GetMapping("/price")
    public ResponseEntity<Map<String, Object>> getPrice(@RequestParam String type) {
        Optional<ClassePlace> classePlaceOpt = classePlaceRepository
            .findByLibelleIgnoreCase(type.trim());
        
        if (classePlaceOpt.isPresent()) {
            ClassePlace classePlace = classePlaceOpt.get();
            Map<String, Object> response = new HashMap<>();
            response.put("type", classePlace.getLibelle());
            response.put("prix", classePlace.getPrixPlace()); // Fallback price
            response.put("isFallback", true);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    //?=== Get all ClassePlace types and fallback prices
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllPrices() {
        Map<String, Double> prices = new HashMap<>();
        
        classePlaceRepository.findAll().forEach(cp -> {
            prices.put(cp.getLibelle(), cp.getPrixPlace());
        });
        
        Map<String, Object> response = new HashMap<>();
        response.put("prices", prices);
        response.put("note", "These are fallback prices. Use /api/pricing/voyage/{voyageId}/ranges for actual pricing.");
        
        return ResponseEntity.ok(response);
    }
}