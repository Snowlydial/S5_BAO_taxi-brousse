package com.itu.taxi_brousse.controller;

import com.itu.taxi_brousse.entity.Bus;
import com.itu.taxi_brousse.entity.BusBusConf;
import com.itu.taxi_brousse.entity.BusConf;
import com.itu.taxi_brousse.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/bus")
@RequiredArgsConstructor
public class BusController {
    
    private final BusRepository busRepository;
    private final BusClasseRepository busClasseRepository;
    private final BusConfRepository busConfRepository;
    private final BusBusConfRepository busBusConfRepository;
    
    //?=== List all buses
    @GetMapping("/list")
    public String listBuses(Model model) {
        List<Bus> buses = busRepository.findAll();
        
        model.addAttribute("pageTitle", "Gestion des Bus");
        model.addAttribute("buses", buses);
        model.addAttribute("busBusConfRepository", busBusConfRepository);
        
        return "bus/list";
    }
    
    //?=== Show create form
    @GetMapping("/create")
    public String createForm(Model model) {
        List<BusConf> allConfs = busConfRepository.findAll();
        
        // Group configurations by libelle for easier selection
        List<String> confTypes = allConfs.stream()
                .map(BusConf::getLibelle)
                .distinct()
                .collect(Collectors.toList());
        
        model.addAttribute("pageTitle", "Nouveau Bus");
        model.addAttribute("classes", busClasseRepository.findAll());
        model.addAttribute("allConfs", allConfs);
        model.addAttribute("confTypes", confTypes);
        
        return "bus/create";
    }
    
    //?=== Create bus
    @PostMapping("/create")
    public String createBus(@RequestParam String immatriculation,
                           @RequestParam Integer busClasseId,
                           @RequestParam(required = false) List<Integer> busConfIds,
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
            
            // Link configurations
            if (busConfIds != null && !busConfIds.isEmpty()) {
                for (Integer confId : busConfIds) {
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
        
        List<BusConf> allConfs = busConfRepository.findAll();
        List<String> confTypes = allConfs.stream()
                .map(BusConf::getLibelle)
                .distinct()
                .collect(Collectors.toList());
        
        // Get current configurations
        List<BusBusConf> currentLinks = busBusConfRepository.findByBus(bus);
        List<Integer> selectedConfIds = currentLinks.stream()
                .map(link -> link.getBusConf().getId())
                .collect(Collectors.toList());
        
        model.addAttribute("pageTitle", "Modifier Bus");
        model.addAttribute("bus", bus);
        model.addAttribute("classes", busClasseRepository.findAll());
        model.addAttribute("allConfs", allConfs);
        model.addAttribute("confTypes", confTypes);
        model.addAttribute("selectedConfIds", selectedConfIds);
        
        return "bus/edit";
    }
    
    //?=== Update bus
    @PostMapping("/edit/{id}")
    public String updateBus(@PathVariable Integer id,
                           @RequestParam String immatriculation,
                           @RequestParam Integer busClasseId,
                           @RequestParam(required = false) List<Integer> busConfIds,
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
            
            // Update configurations - delete old links and create new ones
            List<BusBusConf> oldLinks = busBusConfRepository.findByBus(bus);
            busBusConfRepository.deleteAll(oldLinks);
            
            if (busConfIds != null && !busConfIds.isEmpty()) {
                for (Integer confId : busConfIds) {
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