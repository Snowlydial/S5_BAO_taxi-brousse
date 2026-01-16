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
    
    //?=== Get price for a specific place type (case-insensitive)
    @GetMapping("/price")
    public ResponseEntity<Map<String, Object>> getPrice(@RequestParam String type) {
        Optional<ClassePlace> classePlaceOpt = classePlaceRepository
            .findByLibelleIgnoreCase(type.trim());
        
        if (classePlaceOpt.isPresent()) {
            ClassePlace classePlace = classePlaceOpt.get();
            Map<String, Object> response = new HashMap<>();
            response.put("type", classePlace.getLibelle());
            response.put("prix", classePlace.getPrixPlace());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    //?=== Get all ClassePlace types and prices
    @GetMapping("/all")
    public ResponseEntity<Map<String, Double>> getAllPrices() {
        Map<String, Double> prices = new HashMap<>();
        
        classePlaceRepository.findAll().forEach(cp -> {
            prices.put(cp.getLibelle(), cp.getPrixPlace());
        });
        
        return ResponseEntity.ok(prices);
    }
}