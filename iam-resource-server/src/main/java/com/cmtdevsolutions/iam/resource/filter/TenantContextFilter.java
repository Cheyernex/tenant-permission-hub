package com.cmtdevsolutions.iam.resource.filter;

import com.cmtdevsolutions.iam.common.filter.TenantContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TenantContextFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TenantContextFilter.class);
    private static final String TENANT_ID_CLAIM = "tenant_id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication instanceof JwtAuthenticationToken jwtAuth) {
                Jwt jwt = jwtAuth.getToken();
                if (jwt.hasClaim(TENANT_ID_CLAIM)) {
                    Object tenantIdObj = jwt.getClaim(TENANT_ID_CLAIM);
                    if (tenantIdObj instanceof Number) {
                        Long tenantId = ((Number) tenantIdObj).longValue();
                        TenantContextHolder.setTenantId(tenantId);
                        log.debug("Tenant ID establecido en contexto: {}", tenantId);
                    }
                }
            }
            filterChain.doFilter(request, response);
        } finally {
            TenantContextHolder.clear();
        }
    }
}