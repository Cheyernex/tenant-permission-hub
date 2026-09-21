package com.cmtdevsolutions.iam.resource.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CustomJwtGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final JwtGrantedAuthoritiesConverter defaultConverter = new JwtGrantedAuthoritiesConverter();

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = defaultConverter.convert(jwt);

        if (jwt.hasClaim("permissions")) {
            @SuppressWarnings("unchecked")
            List<String> permissions = jwt.getClaimAsStringList("permissions");
            if (permissions != null) {
                authorities = Stream.concat(
                        authorities.stream(),
                        permissions.stream().map(p -> new SimpleGrantedAuthority("PERM_" + p))
                ).collect(Collectors.toSet());
            }
        }

        if (jwt.hasClaim("tenant_id")) {
            Object tenantIdObj = jwt.getClaim("tenant_id");
            if (tenantIdObj instanceof Number) {
                Long tenantId = ((Number) tenantIdObj).longValue();
                authorities = Stream.concat(
                        authorities.stream(),
                        Stream.of(new SimpleGrantedAuthority("TENANT_" + tenantId))
                ).collect(Collectors.toSet());
            }
        }

        if (jwt.hasClaim("roles")) {
            @SuppressWarnings("unchecked")
            List<String> roles = jwt.getClaimAsStringList("roles");
            if (roles != null) {
                authorities = Stream.concat(
                        authorities.stream(),
                        roles.stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                ).collect(Collectors.toSet());
            }
        }

        return authorities;
    }
}