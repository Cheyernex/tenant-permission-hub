package com.cmtdevsolutions.iam.auth.service;

import com.cmtdevsolutions.iam.common.entity.Usuario;
import com.cmtdevsolutions.iam.common.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String[] parts = username.split(":", 2);
        
        if (parts.length == 2) {
            String tenantCodigo = parts[0];
            String email = parts[1];
            Usuario usuario = usuarioRepository.findByTenant_CodigoAndEmail(tenantCodigo, email)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
            return buildUserDetails(usuario);
        } else {
            Usuario usuario = usuarioRepository.findByEmailIgnoreCase(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
            return buildUserDetails(usuario);
        }
    }

    private UserDetails buildUserDetails(Usuario usuario) {
        return User.withUsername(usuario.getEmail())
                .password(usuario.getPasswordHash())
                .authorities(usuario.getEsAdmin() ? "TENANT_ADMIN" : "USER")
                .accountExpired(!usuario.getActivo())
                .accountLocked(!usuario.getActivo())
                .credentialsExpired(false)
                .disabled(!usuario.getActivo())
                .build();
    }
}