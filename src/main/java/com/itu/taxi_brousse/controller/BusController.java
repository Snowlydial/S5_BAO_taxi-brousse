package com.itu.taxi_brousse.controller;

import com.itu.taxi_brousse.entity.*;
import com.itu.taxi_brousse.repository.*;
import com.itu.taxi_brousse.service.DisponibilitePlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/bus")
@RequiredArgsConstructor
public class BusController {
    
    private final BusRepository busRepository;
    private final BusConfRepository busConfRepository;
    private final BusBusConfRepository busBusConfRepository;
    private final ClassePlaceRepository classePlaceRepository;
    private final DisponibilitePlaceService disponibilitePlaceService;
    
    //?=== List all buses
    @GetMapping("/list")
    public String listBuses(Model model) {
        List<Bus> buses = busRepository.findAll();
        
        // Calculate potential revenue for each bus
        Map<Integer, Double> revenueMap = new HashMap<>();
        for (Bus bus : buses) {
            Double revenue = disponibilitePlaceService.calculatePotentialRevenue(bus.getId());
            revenueMap.put(bus.getId(), revenue);
        }
        
        model.addAttribute("pageTitle", "Gestion des Bus");
        model.addAttribute("buses", buses);
        model.addAttribute("busBusConfRepository", busBusConfRepository);
        model.addAttribute("revenueMap", revenueMap);
        
        return "bus/list";
    }
    
    //?=== Show create form
    @GetMapping("/create")
    public String createForm(Model model) {
        // Get all non-seat-capacity configurations (wifi, climatisation, etc.)
        List<BusConf> otherConfs = busConfRepository.findAll().stream()
                .filter(conf -> !conf.getLibelle().startsWith("nb_place_"))
                .collect(Collectors.toList());
        
        // Group other configurations by libelle
        List<String> otherConfTypes = otherConfs.stream()
                .map(BusConf::getLibelle)
                .distinct()
                .collect(Collectors.toList());
        
        // Get all available ClassePlace types for dropdown
        List<ClassePlace> classePlaceTypes = classePlaceRepository.findAll();
        
        model.addAttribute("pageTitle", "Nouveau Bus");
        model.addAttribute("otherConfs", otherConfs);
        model.addAttribute("otherConfTypes", otherConfTypes);
        model.addAttribute("classePlaceTypes", classePlaceTypes);
        
        return "bus/create";
    }
    
    //?=== Create bus
    @PostMapping("/create")
    public String createBus(@RequestParam String immatriculation,
                           @RequestParam(required = false) List<String> placeTypeNames,
                           @RequestParam(required = false) List<String> placeTypeCapacities,
                           @RequestParam(required = false) List<Integer> otherBusConfIds,
                           RedirectAttributes redirectAttributes) {
        try {
            System.out.println("=== DEBUG BUS CREATION ===");
            System.out.println("Immatriculation: " + immatriculation);
            System.out.println("placeTypeNames: " + placeTypeNames);
            System.out.println("placeTypeCapacities: " + placeTypeCapacities);
            System.out.println("otherBusConfIds: " + otherBusConfIds);
            
            //*-- Validation
            if (immatriculation == null || immatriculation.trim().isEmpty()) {
                throw new RuntimeException("L'immatriculation est obligatoire");
            }
            
            //*-- Check if immatriculation already exists
            if (busRepository.findByImmatriculation(immatriculation.trim()).isPresent()) {
                throw new RuntimeException("Un bus avec cette immatriculation existe déjà");
            }
            
            //*-- Create bus
            Bus bus = Bus.builder()
                    .immatriculation(immatriculation.trim())
                    .build();
            
            bus = busRepository.save(bus);
            System.out.println("Bus created with ID: " + bus.getId());
            
            //*-- Process place type capacities (arrays)
            if (placeTypeNames != null && placeTypeCapacities != null && 
                !placeTypeNames.isEmpty() && placeTypeNames.size() == placeTypeCapacities.size()) {
                
                System.out.println("Processing " + placeTypeNames.size() + " place types");
                
                for (int i = 0; i < placeTypeNames.size(); i++) {
                    String placeType = placeTypeNames.get(i);
                    String capacityStr = placeTypeCapacities.get(i);
                    
                    System.out.println("  [" + i + "] placeType='" + placeType + "', capacity='" + capacityStr + "'");
                    
                    if (placeType != null && !placeType.trim().isEmpty() &&
                        capacityStr != null && !capacityStr.trim().isEmpty()) {
                        
                        try {
                            int capacity = Integer.parseInt(capacityStr.trim());
                            System.out.println("  Parsed capacity: " + capacity);
                            
                            if (capacity > 0) {
                                //*-- Check if configuration exists (normalize to lowercase)
                                String confLibelle = "nb_place_" + placeType.toLowerCase();
                                Optional<BusConf> existingConf = busConfRepository.findAll().stream()
                                        .filter(c -> c.getLibelle().equalsIgnoreCase(confLibelle) && 
                                                     c.getValeur().equals(String.valueOf(capacity)))
                                        .findFirst();
                                
                                BusConf conf;
                                if (existingConf.isPresent()) {
                                    conf = existingConf.get();
                                    System.out.println("  Using existing BusConf ID: " + conf.getId());
                                } else {
                                    //*-- Create new configuration
                                    conf = BusConf.builder()
                                            .libelle(confLibelle)
                                            .valeur(String.valueOf(capacity))
                                            .build();
                                    conf = busConfRepository.save(conf);
                                    System.out.println("  Created new BusConf ID: " + conf.getId());
                                }
                                
                                //*-- Link to bus
                                BusBusConf link = BusBusConf.builder()
                                        .bus(bus)
                                        .busConf(conf)
                                        .build();
                                busBusConfRepository.save(link);
                                System.out.println("  Linked BusConf to Bus");
                            }
                        } catch (NumberFormatException e) {
                            System.err.println("  ERROR: Cannot parse capacity '" + capacityStr + "' as integer");
                            throw new RuntimeException("Capacité invalide pour " + placeType + ": '" + capacityStr + "'");
                        }
                    }
                }
            } else {
                System.out.println("No place type capacities provided or mismatched arrays");
            }
            
            //*-- Link other configurations (wifi, climatisation, etc.)
            if (otherBusConfIds != null && !otherBusConfIds.isEmpty()) {
                System.out.println("Linking " + otherBusConfIds.size() + " other configurations");
                for (Integer confId : otherBusConfIds) {
                    BusConf conf = busConfRepository.findById(confId)
                            .orElseThrow(() -> new RuntimeException("Configuration introuvable"));
                    
                    BusBusConf link = BusBusConf.builder()
                            .bus(bus)
                            .busConf(conf)
                            .build();
                    
                    busBusConfRepository.save(link);
                }
            }
            
            System.out.println("=== BUS CREATION SUCCESS ===");
            redirectAttributes.addFlashAttribute("success", "Bus créé avec succès!");
            return "redirect:/bus/list";
            
        } catch (Exception e) {
            System.err.println("=== BUS CREATION ERROR ===");
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/bus/create";
        }
    }
    
    //?=== Show edit form
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        Bus bus = busRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bus introuvable"));
        
        // Get current configurations
        List<BusBusConf> currentLinks = busBusConfRepository.findByBus(bus);
        
        // Separate place type configs from other configs
        Map<String, Integer> currentPlaceTypes = new HashMap<>();
        List<Integer> selectedOtherConfIds = new ArrayList<>();
        
        for (BusBusConf link : currentLinks) {
            String libelle = link.getBusConf().getLibelle();
            if (libelle.startsWith("nb_place_")) {
                String placeType = libelle.substring("nb_place_".length());
                try {
                    int capacity = Integer.parseInt(link.getBusConf().getValeur());
                    currentPlaceTypes.put(placeType, capacity);
                } catch (NumberFormatException e) {
                    // Skip invalid values
                }
            } else {
                selectedOtherConfIds.add(link.getBusConf().getId());
            }
        }
        
        // Get all non-seat-capacity configurations
        List<BusConf> otherConfs = busConfRepository.findAll().stream()
                .filter(conf -> !conf.getLibelle().startsWith("nb_place_"))
                .collect(Collectors.toList());
        
        List<String> otherConfTypes = otherConfs.stream()
                .map(BusConf::getLibelle)
                .distinct()
                .collect(Collectors.toList());
        
        // Get all available ClassePlace types for dropdown
        List<ClassePlace> classePlaceTypes = classePlaceRepository.findAll();
        
        model.addAttribute("pageTitle", "Modifier Bus");
        model.addAttribute("bus", bus);
        model.addAttribute("currentPlaceTypes", currentPlaceTypes);
        model.addAttribute("otherConfs", otherConfs);
        model.addAttribute("otherConfTypes", otherConfTypes);
        model.addAttribute("selectedOtherConfIds", selectedOtherConfIds);
        model.addAttribute("classePlaceTypes", classePlaceTypes);
        
        return "bus/edit";
    }
    
    //?=== Update bus
    @PostMapping("/edit/{id}")
    public String updateBus(@PathVariable Integer id,
                           @RequestParam String immatriculation,
                           @RequestParam(required = false) List<String> placeTypeNames,
                           @RequestParam(required = false) List<String> placeTypeCapacities,
                           @RequestParam(required = false) List<Integer> otherBusConfIds,
                           RedirectAttributes redirectAttributes) {
        try {
            System.out.println("=== DEBUG BUS UPDATE ===");
            System.out.println("Bus ID: " + id);
            System.out.println("Immatriculation: " + immatriculation);
            System.out.println("placeTypeNames: " + placeTypeNames);
            System.out.println("placeTypeCapacities: " + placeTypeCapacities);
            
            Bus bus = busRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Bus introuvable"));
            
            //*-- Validation
            if (immatriculation == null || immatriculation.trim().isEmpty()) {
                throw new RuntimeException("L'immatriculation est obligatoire");
            }
            
            //*-- Check if immatriculation exists for another bus
            Optional<Bus> existingBusOpt = busRepository.findByImmatriculation(immatriculation.trim());
            if (existingBusOpt.isPresent() && !existingBusOpt.get().getId().equals(id)) {
                throw new RuntimeException("Un autre bus avec cette immatriculation existe déjà");
            }
            
            //*-- Update bus
            bus.setImmatriculation(immatriculation.trim());
            busRepository.save(bus);
            
            //*-- Delete all old links
            List<BusBusConf> oldLinks = busBusConfRepository.findByBus(bus);
            busBusConfRepository.deleteAll(oldLinks);
            System.out.println("Deleted " + oldLinks.size() + " old configurations");
            
            //*-- Add place type configurations
            if (placeTypeNames != null && placeTypeCapacities != null && 
                !placeTypeNames.isEmpty() && placeTypeNames.size() == placeTypeCapacities.size()) {
                
                System.out.println("Processing " + placeTypeNames.size() + " place types");
                
                for (int i = 0; i < placeTypeNames.size(); i++) {
                    String placeType = placeTypeNames.get(i);
                    String capacityStr = placeTypeCapacities.get(i);
                    
                    System.out.println("  [" + i + "] placeType='" + placeType + "', capacity='" + capacityStr + "'");
                    
                    if (placeType != null && !placeType.trim().isEmpty() &&
                        capacityStr != null && !capacityStr.trim().isEmpty()) {
                        
                        try {
                            int capacity = Integer.parseInt(capacityStr.trim());
                            
                            if (capacity > 0) {
                                String confLibelle = "nb_place_" + placeType.toLowerCase();
                                Optional<BusConf> existingConf = busConfRepository.findAll().stream()
                                        .filter(c -> c.getLibelle().equalsIgnoreCase(confLibelle) && 
                                                     c.getValeur().equals(String.valueOf(capacity)))
                                        .findFirst();
                                
                                BusConf conf;
                                if (existingConf.isPresent()) {
                                    conf = existingConf.get();
                                } else {
                                    conf = BusConf.builder()
                                            .libelle(confLibelle)
                                            .valeur(String.valueOf(capacity))
                                            .build();
                                    conf = busConfRepository.save(conf);
                                }
                                
                                BusBusConf link = BusBusConf.builder()
                                        .bus(bus)
                                        .busConf(conf)
                                        .build();
                                busBusConfRepository.save(link);
                            }
                        } catch (NumberFormatException e) {
                            System.err.println("  ERROR: Cannot parse capacity '" + capacityStr + "' as integer");
                            throw new RuntimeException("Capacité invalide pour " + placeType + ": '" + capacityStr + "'");
                        }
                    }
                }
            }
            
            //*-- Add other configurations
            if (otherBusConfIds != null && !otherBusConfIds.isEmpty()) {
                for (Integer confId : otherBusConfIds) {
                    BusConf conf = busConfRepository.findById(confId)
                            .orElseThrow(() -> new RuntimeException("Configuration introuvable"));
                    
                    BusBusConf link = BusBusConf.builder()
                            .bus(bus)
                            .busConf(conf)
                            .build();
                    
                    busBusConfRepository.save(link);
                }
            }
            
            System.out.println("=== BUS UPDATE SUCCESS ===");
            redirectAttributes.addFlashAttribute("success", "Bus modifié avec succès!");
            return "redirect:/bus/list";
            
        } catch (Exception e) {
            System.err.println("=== BUS UPDATE ERROR ===");
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/bus/edit/" + id;
        }
    }
    
    //?=== Delete bus
    @PostMapping("/delete/{id}")
    public String deleteBus(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            Bus bus = busRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Bus introuvable"));
            
            // Delete configurations links first
            List<BusBusConf> links = busBusConfRepository.findByBus(bus);
            busBusConfRepository.deleteAll(links);
            
            // Delete bus
            busRepository.delete(bus);
            
            redirectAttributes.addFlashAttribute("success", "Bus supprimé avec succès!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Erreur lors de la suppression: " + e.getMessage() + 
                ". Ce bus est peut-être utilisé dans des Bus_Voyage.");
        }
        
        return "redirect:/bus/list";
    }
}