package com.itu.taxi_brousse.util.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {
    @ModelAttribute
    public void addAttributes(HttpServletRequest request, org.springframework.ui.Model model) {
        model.addAttribute("currentPath", request.getRequestURI());
    }
}