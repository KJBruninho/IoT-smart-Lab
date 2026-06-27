package com.iotroom.iotroom.controller.dashboard;

import com.iotroom.iotroom.security.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    public DashboardController() {}

    @GetMapping("/")
    public String home(Authentication authentication) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            return "redirect:/admin";
        }
        if ("PROFESSOR".equalsIgnoreCase(user.getRole())) {
            return "redirect:/professor";
        }
        if ("ALUNO".equalsIgnoreCase(user.getRole())) {
            return "redirect:/aluno";
        }

        return "redirect:/professor";
    }

}