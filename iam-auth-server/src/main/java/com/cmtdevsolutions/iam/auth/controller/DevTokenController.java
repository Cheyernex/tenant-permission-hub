package com.cmtdevsolutions.iam.auth.controller;

import com.cmtdevsolutions.iam.auth.service.UserDetailsServiceImpl;
import com.cmtdevsolutions.iam.common.entity.Usuario;
import com.cmtdevsolutions.iam.common.repository.UsuarioRepository;
import com.cmtdevsolutions.iam.common.service.PermissionCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/dev")
@RequiredArgsConstructor
public class DevTokenController {

    private final UsuarioRepository usuarioRepository;
    private final PermissionCacheService permissionCacheService;
    private final JwtEncoder jwtEncoder;
    private final PasswordEncoder passwordEncoder;

    @Value("${spring.security.oauth2.authorizationserver.issuer-uri:http://localhost:9000}")
    private String issuerUri;

    public record DevLoginRequest(String email, String password, String tenantCodigo, String role) {}
    public record DevTokenResponse(String access_token, String token_type, long expires_in, Map<String,Object> claims) {}

    @PostMapping("/token")
    public ResponseEntity<?> devToken(@RequestBody DevLoginRequest req) {
        // Lookup usuario real si existe, si no crea respuesta mock con tenant 1
        var usuarioOpt = req.tenantCodigo() != null && !req.tenantCodigo().isBlank()
                ? usuarioRepository.findByTenant_CodigoAndEmail(req.tenantCodigo(), req.email())
                : usuarioRepository.findByEmailIgnoreCase(req.email());

        Long tenantId;
        Long userId;
        String email;
        String nombre;
        List<String> roles;
        Set<String> perms;

        if (usuarioOpt.isPresent()) {
            Usuario u = usuarioOpt.get();
            // Dev: no bloquear por password (hash demo puede desincronizarse); solo loguear
            // Si se quiere validar estricto, descomentar el bloque 401
            // if (req.password() != null && !req.password().isBlank() && !passwordEncoder.matches(req.password(), u.getPasswordHash())) {
            //     return ResponseEntity.status(401).body(Map.of("error", "Credenciales inválidas"));
            // }
            tenantId = u.getTenant().getId();
            userId = u.getId();
            email = u.getEmail();
            nombre = u.getNombre();
            // Rol: respeta el solicitado si no es vacío, si no usa el real (system+esAdmin => SUPER_ADMIN)
            if (req.role() != null && !req.role().isBlank()) {
                roles = List.of(req.role());
            } else {
                boolean isSystem = "system".equalsIgnoreCase(u.getTenant().getCodigo());
                roles = (u.getEsAdmin() && isSystem) ? List.of("SUPER_ADMIN") : (u.getEsAdmin() ? List.of("TENANT_ADMIN") : List.of("USER"));
            }
            // Permisos efectivos reales (si es SUPER_ADMIN, perms puede estar vacío pero podrá crear catálogo global igual)
            try {
                perms = permissionCacheService.getEffectiveKeysCached(tenantId, userId);
            } catch (Exception e) {
                perms = Set.of();
            }
        } else {
            // Fallback dev sin DB: usa tenant 1 y role solicitado
            tenantId = 1L;
            userId = 1L;
            email = req.email();
            nombre = req.email();
            roles = req.role() != null ? List.of(req.role()) : List.of("TENANT_ADMIN");
            perms = Set.of();
        }

        Instant now = Instant.now();
        long expiresIn = 1800;
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuerUri)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiresIn))
                .subject(String.valueOf(userId))
                .claim("tenant_id", tenantId)
                .claim("user_id", userId)
                .claim("email", email)
                .claim("nombre", nombre)
                .claim("roles", roles)
                .claim("permissions", List.copyOf(perms))
                .claim("perm_version", perms.hashCode())
                .claim("scope", "read write")
                .build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        return ResponseEntity.ok(new DevTokenResponse(token, "Bearer", expiresIn, claims.getClaims()));
    }

    @GetMapping("/health")
    public Map<String,String> health() { return Map.of("status","ok","issuer","http://localhost:9000"); }
}
