package dev.mrraxyer.sportstournamentmanager.security;

import dev.mrraxyer.sportstournamentmanager.dto.UsuarioSesionDto;
import dev.mrraxyer.sportstournamentmanager.models.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthTokenService {

    private static final Duration TOKEN_TTL = Duration.ofHours(8);

    private final ConcurrentHashMap<String, TokenSession> sessions = new ConcurrentHashMap<>();

    public LoginToken createToken(Usuario usuario, Collection<? extends GrantedAuthority> authorities) {
        String token = UUID.randomUUID().toString().replace("-", "");
        Instant expiresAt = Instant.now().plus(TOKEN_TTL);
        List<String> authorityNames = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        UsuarioSesionDto usuarioSesion = new UsuarioSesionDto(
                usuario.getUsuariosId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getRol() != null ? usuario.getRol().getNombre() : null);

        sessions.put(token, new TokenSession(usuarioSesion, authorityNames, expiresAt));
        return new LoginToken(token, expiresAt, usuarioSesion, authorityNames);
    }

    public Optional<TokenSession> validate(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        TokenSession session = sessions.get(token);
        if (session == null) {
            return Optional.empty();
        }

        if (Instant.now().isAfter(session.expiresAt())) {
            sessions.remove(token);
            return Optional.empty();
        }

        return Optional.of(session);
    }

    public void revoke(String token) {
        if (token != null && !token.isBlank()) {
            sessions.remove(token);
        }
    }

    public Optional<LoginToken> refresh(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        TokenSession session = sessions.get(token);
        if (session == null) {
            return Optional.empty();
        }

        if (Instant.now().isAfter(session.expiresAt())) {
            sessions.remove(token);
            return Optional.empty();
        }

        // Remove old token and create a new one with same session data
        sessions.remove(token);
        String newToken = UUID.randomUUID().toString().replace("-", "");
        Instant expiresAt = Instant.now().plus(TOKEN_TTL);
        sessions.put(newToken, new TokenSession(session.usuario(), session.authorities(), expiresAt));
        return Optional.of(new LoginToken(newToken, expiresAt, session.usuario(), session.authorities()));
    }

    public record TokenSession(UsuarioSesionDto usuario, List<String> authorities, Instant expiresAt) {
    }

    public record LoginToken(String accessToken, Instant expiresAt, UsuarioSesionDto usuario,
            List<String> authorities) {
        public Collection<? extends GrantedAuthority> toGrantedAuthorities() {
            return authorities.stream().map(SimpleGrantedAuthority::new).toList();
        }
    }
}
