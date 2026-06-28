package com.iotroom.iotroom.controller;

import com.iotroom.iotroom.repository.UtilizadorRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(annotations = Controller.class)
public class GlobalModelAttributes {

    private final UtilizadorRepository utilizadorRepository;

    public GlobalModelAttributes(UtilizadorRepository utilizadorRepository) {
        this.utilizadorRepository = utilizadorRepository;
    }

    @ModelAttribute("nomeUtilizador")
    public String nomeUtilizador(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "Utilizador";
        }

        String email = authentication.getName();

        if (email == null || email.isBlank() || "anonymousUser".equals(email)) {
            return "Utilizador";
        }

        return utilizadorRepository.findByEmail(email)
                .map(utilizador -> utilizador.getNome())
                .filter(nome -> nome != null && !nome.isBlank())
                .orElse(email);
    }
}