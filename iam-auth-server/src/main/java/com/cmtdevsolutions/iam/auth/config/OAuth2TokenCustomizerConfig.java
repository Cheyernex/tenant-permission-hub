package com.cmtdevsolutions.iam.auth.config;

import com.cmtdevsolutions.iam.common.repository.UsuarioRepository;
import com.cmtdevsolutions.iam.common.service.PermissionCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import java.util.List;
import java.util.Set;

/**
 * Fase 5: JWT híbrido — claims tenant_id, user_id, roles, permissions, perm_version.
 * permissions se resuelve vía PermissionCacheService (Redis eff_perms:{tenant}:{user}, TTL 10m)
 * con fallback a DB (UNION). Access token 30m; validación anti-escalación siempre contra DB/cache
 * en service layer, no solo JWT.
 * Evaluación: ~500 perms x 20 chars ≈ 10KB < límite header 8-16KB → viable para gateway,
 * pero revocación inmediata requiere verificación cache/DB en operaciones sensibles.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class OAuth2TokenCustomizerConfig {

    private final UsuarioRepository usuarioRepository;
    private final PermissionCacheService permissionCacheService;

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
        return context -> {
            // Solo access_token y id_token
            if (!"access_token".equals(context.getTokenType().getValue()) &&
                !"id_token".equals(context.getTokenType().getValue())) {
                return;
            }
            Authentication principal = context.getPrincipal();
            if (principal == null) return;
            Object principalObj = principal.getPrincipal();
            String username;
            if (principalObj instanceof UserDetails ud) {
                username = ud.getUsername();
            } else if (principalObj instanceof String s) {
                username = s;
            } else {
                return;
            }

            // username puede ser "tenantCodigo:email" o solo email (UserDetailsServiceImpl)
            var usuarioOpt = username.contains(":")
                    ? usuarioRepository.findByTenant_CodigoAndEmail(
                            username.substring(0, username.indexOf(':')),
                            username.substring(username.indexOf(':') + 1))
                    : usuarioRepository.findByEmailIgnoreCase(username);

            if (usuarioOpt.isEmpty()) {
                log.debug("TokenCustomizer: usuario no encontrado para {}", username);
                return;
            }
            var usuario = usuarioOpt.get();
            Long tenantId = usuario.getTenant().getId();
            Long userId = usuario.getId();

            // Claims core (nunca dependen de cliente)
            context.getClaims().claim("tenant_id", tenantId);
            context.getClaims().claim("user_id", userId);
            context.getClaims().claim("email", usuario.getEmail());
            context.getClaims().claim("nombre", usuario.getNombre());

            List<String> roles = usuario.getEsAdmin() ? List.of("TENANT_ADMIN") : List.of("USER");
            context.getClaims().claim("roles", roles);

            // Permisos efectivos vía cache (UNION directos + plantillas) — evaluado como Set<String "submodulo:accion">
            Set<String> perms = permissionCacheService.getEffectiveKeysCached(tenantId, userId);
            // Fallback a lista vacía si usuario sin permisos
            context.getClaims().claim("permissions", List.copyOf(perms));

            // Versión para detectar staleness (incremental; Fase 5 usa hash simple: size + timestamp)
            int permVersion = perms.hashCode();
            context.getClaims().claim("perm_version", permVersion);

            log.debug("JWT emitido user={} tenant={} roles={} perms={} version={}", userId, tenantId, roles, perms.size(), permVersion);
        };
    }
}
