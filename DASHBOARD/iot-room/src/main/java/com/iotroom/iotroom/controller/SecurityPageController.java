package com.iotroom.iotroom.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SecurityPageController {

    @GetMapping("/acesso-negado")
    public String acessoNegado() {
        return "auth/acesso-negado";
    }
}