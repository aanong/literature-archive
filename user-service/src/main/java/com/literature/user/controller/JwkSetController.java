package com.literature.user.controller;

import com.literature.user.service.JwtTokenService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JwkSetController {

    private final JwtTokenService jwtTokenService;

    public JwkSetController(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @GetMapping("/.well-known/jwks.json")
    public String keys() {
        return jwtTokenService.getJwkSetJson();
    }
}
