package com.cmtdevsolutions.iam.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            Object userId = jwt.getClaim("user_id");
            if (userId instanceof Number n) return n.longValue();
            if (userId instanceof String s) return Long.parseLong(s);
        }
        throw new IllegalStateException("No se pudo extraer user_id del JWT");
    }

    public static Long getCurrentTenantId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            Object tenantId = jwt.getClaim("tenant_id");
            if (tenantId instanceof Number n) return n.longValue();
            if (tenantId instanceof String s) return Long.parseLong(s);
        }
        throw new IllegalStateException("No se pudo extraer tenant_id del JWT");
    }
}
