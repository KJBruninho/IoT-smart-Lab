package com.iotroom.iotroom.controller;

import com.iotroom.iotroom.security.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestSecurityController {

    @GetMapping("/api/professor/me")
    public AuthenticatedUser me(Authentication authentication) {
        return (AuthenticatedUser) authentication.getPrincipal();
    }
}