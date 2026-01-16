package com.itu.taxi_brousse.controller;

import com.itu.taxi_brousse.entity.Client;
import com.itu.taxi_brousse.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {
    
    private final ClientService clientService;
    
    @GetMapping
    public String listClients(@RequestParam(required = false) String search, Model model) {
        List<Client> clients;
        
        if (search != null && !search.trim().isEmpty()) {
            clients = clientService.searchClients(search);
            model.addAttribute("search", search);
        } else {
            clients = clientService.getAllClients();
        }
        
        model.addAttribute("pageTitle", "Liste des Clients");
        model.addAttribute("clients", clients);
        return "client/list";
    }
    
    @GetMapping("/new")
    public String createClientForm(Model model) {
        model.addAttribute("pageTitle", "Nouveau Client");
        model.addAttribute("client", new Client());
        return "client/create";
    }
    
    @PostMapping("/new")
    public String createClient(@ModelAttribute Client client) {
        clientService.saveClient(client);
        return "redirect:/clients";
    }
    
    @GetMapping("/{id}")
    public String viewClient(@PathVariable Integer id, Model model) {
        Client client = clientService.getClientById(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));
        
        model.addAttribute("pageTitle", "Détails du Client");
        model.addAttribute("client", client);
        return "client/details";
    }
    
    @GetMapping("/{id}/edit")
    public String editClientForm(@PathVariable Integer id, Model model) {
        Client client = clientService.getClientById(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));
        
        model.addAttribute("pageTitle", "Modifier Client");
        model.addAttribute("client", client);
        return "client/edit";
    }
    
    @PostMapping("/{id}/edit")
    public String updateClient(@PathVariable Integer id, @ModelAttribute Client client) {
        client.setId(id);
        clientService.saveClient(client);
        return "redirect:/clients/" + id;
    }
    
    @PostMapping("/{id}/delete")
    public String deleteClient(@PathVariable Integer id) {
        clientService.deleteClient(id);
        return "redirect:/clients";
    }
}