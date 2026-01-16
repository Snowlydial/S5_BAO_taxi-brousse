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
    private final BusClasseRepository busClasseRepository;
    private final BusConfRepository busConfRepository;
    private final BusBusConfRepository busBusConfRepository;
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
        
        model.addAttribute("pageTitle", "Nouveau Bus");
        model.addAttribute("classes", busClasseRepository.findAll());
        model.addAttribute("otherConfs", otherConfs);
        model.addAttribute("otherConfTypes", otherConfTypes);
        
        return "bus/create";
    }
    
    //?=== Create bus
    @PostMapping("/create")
    public String createBus(@RequestParam String immatriculation,
                           @RequestParam Integer busClasseId,
                           @RequestParam(required = false) Map<String, String> placeTypeCapacities,
                           @RequestParam(required = false) List<Integer> otherBusConfIds,
                           RedirectAttributes redirectAttributes) {
        try {
            // Validation
            if (immatriculation == null || immatriculation.trim().isEmpty()) {
                throw new RuntimeException("L'immatriculation est obligatoire");
            }
            
            // Check if immatriculation already exists
            if (busRepository.findByImmatriculation(immatriculation.trim()).isPresent()) {
                throw new RuntimeException("Un bus avec cette immatriculation existe déjà");
            }
            
            // Create bus
            Bus bus = Bus.builder()
                    .immatriculation(immatriculation.trim())
                    .busClasse(busClasseRepository.findById(busClasseId)
                            .orElseThrow(() -> new RuntimeException("Classe de bus introuvable")))
                    .build();
            
            bus = busRepository.save(bus);
            
            // Process place type capacities
            if (placeTypeCapacities != null) {
                for (Map.Entry<String, String> entry : placeTypeCapacities.entrySet()) {
                    String placeType = entry.getKey();
                    String capacityStr = entry.getValue();
                    
                    if (capacityStr != null && !capacityStr.trim().isEmpty()) {
                        int capacity = Integer.parseInt(capacityStr.trim());
                        
                        if (capacity > 0) {
                            // Check if configuration exists
                            String confLibelle = "nb_place_" + placeType;
                            Optional<BusConf> existingConf = busConfRepository.findAll().stream()
                                    .filter(c -> c.getLibelle().equals(confLibelle) && 
                                                 c.getValeur().equals(String.valueOf(capacity)))
                                    .findFirst();
                            
                            BusConf conf;
                            if (existingConf.isPresent()) {
                                conf = existingConf.get();
                            } else {
                                // Create new configuration
                                conf = BusConf.builder()
                                        .libelle(confLibelle)
                                        .valeur(String.valueOf(capacity))
                                        .build();
                                conf = busConfRepository.save(conf);
                            }
                            
                            // Link to bus
                            BusBusConf link = BusBusConf.builder()
                                    .bus(bus)
                                    .busConf(conf)
                                    .build();
                            busBusConfRepository.save(link);
                        }
                    }
                }
            }
            
            // Link other configurations (wifi, climatisation, etc.)
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
            
            redirectAttributes.addFlashAttribute("success", "Bus créé avec succès!");
            return "redirect:/bus/list";
            
        } catch (Exception e) {
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
        
        model.addAttribute("pageTitle", "Modifier Bus");
        model.addAttribute("bus", bus);
        model.addAttribute("classes", busClasseRepository.findAll());
        model.addAttribute("currentPlaceTypes", currentPlaceTypes);
        model.addAttribute("otherConfs", otherConfs);
        model.addAttribute("otherConfTypes", otherConfTypes);
        model.addAttribute("selectedOtherConfIds", selectedOtherConfIds);
        
        return "bus/edit";
    }
    
    //?=== Update bus
    @PostMapping("/edit/{id}")
    public String updateBus(@PathVariable Integer id,
                           @RequestParam String immatriculation,
                           @RequestParam Integer busClasseId,
                           @RequestParam(required = false) Map<String, String> placeTypeCapacities,
                           @RequestParam(required = false) List<Integer> otherBusConfIds,
                           RedirectAttributes redirectAttributes) {
        try {
            Bus bus = busRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Bus introuvable"));
            
            // Validation
            if (immatriculation == null || immatriculation.trim().isEmpty()) {
                throw new RuntimeException("L'immatriculation est obligatoire");
            }
            
            // Check if immatriculation exists for another bus
            Optional<Bus> existingBusOpt = busRepository.findByImmatriculation(immatriculation.trim());
            if (existingBusOpt.isPresent() && !existingBusOpt.get().getId().equals(id)) {
                throw new RuntimeException("Un autre bus avec cette immatriculation existe déjà");
            }
            
            // Update bus
            bus.setImmatriculation(immatriculation.trim());
            bus.setBusClasse(busClasseRepository.findById(busClasseId)
                    .orElseThrow(() -> new RuntimeException("Classe de bus introuvable")));
            
            busRepository.save(bus);
            
            // Delete all old links
            List<BusBusConf> oldLinks = busBusConfRepository.findByBus(bus);
            busBusConfRepository.deleteAll(oldLinks);
            
            // Add place type configurations
            if (placeTypeCapacities != null) {
                for (Map.Entry<String, String> entry : placeTypeCapacities.entrySet()) {
                    String placeType = entry.getKey();
                    String capacityStr = entry.getValue();
                    
                    if (capacityStr != null && !capacityStr.trim().isEmpty()) {
                        int capacity = Integer.parseInt(capacityStr.trim());
                        
                        if (capacity > 0) {
                            String confLibelle = "nb_place_" + placeType;
                            Optional<BusConf> existingConf = busConfRepository.findAll().stream()
                                    .filter(c -> c.getLibelle().equals(confLibelle) && 
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
                    }
                }
            }
            
            // Add other configurations
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
            
            redirectAttributes.addFlashAttribute("success", "Bus modifié avec succès!");
            return "redirect:/bus/list";
            
        } catch (Exception e) {
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