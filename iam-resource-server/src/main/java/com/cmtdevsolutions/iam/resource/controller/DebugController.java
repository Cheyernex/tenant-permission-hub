package com.cmtdevsolutions.iam.resource.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/debug")
public class DebugController {

    @GetMapping("/auth")
    public Map<String,Object> auth(Authentication auth) {
        if (auth == null) return Map.of("auth", "null");
        var m = new java.util.HashMap<String,Object>();
        m.put("name", auth.getName());
        m.put("authorities", auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
        m.put("authenticated", auth.isAuthenticated());
        m.put("class", auth.getClass().getSimpleName());
        if (auth instanceof JwtAuthenticationToken jwt) {
            m.put("jwt_claims", jwt.getToken().getClaims());
        }
        return m;
    }
}
