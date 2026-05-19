package dev.mrraxyer.sportstournamentmanager.security;

import dev.mrraxyer.sportstournamentmanager.dto.UsuarioSesionDto;
import dev.mrraxyer.sportstournamentmanager.models.AuthToken;
import dev.mrraxyer.sportstournamentmanager.models.Usuario;
import dev.mrraxyer.sportstournamentmanager.repositories.AuthTokenRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthTokenService {

    private static final Duration TOKEN_TTL = Duration.ofHours(8);
    private final AuthTokenRepository authTokenRepository;

    public AuthTokenService(AuthTokenRepository authTokenRepository) {
        this.authTokenRepository = authTokenRepository;
    }

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

        // Guardar token en la BD
        AuthToken authToken = new AuthToken(usuario, token, expiresAt);
        authTokenRepository.save(authToken);

        return new LoginToken(token, expiresAt, usuarioSesion, authorityNames);
    }

    public Optional<TokenSession> validate(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        // Buscar token en la BD
        Optional<AuthToken> authTokenOpt = authTokenRepository.findByTokenValue(token);
        if (authTokenOpt.isEmpty()) {
            return Optional.empty();
        }

        AuthToken authToken = authTokenOpt.get();
        if (Instant.now().isAfter(authToken.getExpiresAt())) {
            authTokenRepository.delete(authToken);
            return Optional.empty();
        }

        Usuario usuario = authToken.getUsuario();
        List<String> authorities = usuario.getRol() != null
                ? List.of("ROLE_" + usuario.getRol().getNombre())
                : List.of();

        UsuarioSesionDto usuarioSesion = new UsuarioSesionDto(
                usuario.getUsuariosId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getRol() != null ? usuario.getRol().getNombre() : null);

        return Optional.of(new TokenSession(usuarioSesion, authorities, authToken.getExpiresAt()));
    }

    public void revoke(String token) {
        if (token != null && !token.isBlank()) {
            authTokenRepository.findByTokenValue(token).ifPresent(authTokenRepository::delete);
        }
    }

    public Optional<LoginToken> refresh(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        Optional<AuthToken> authTokenOpt = authTokenRepository.findByTokenValue(token);
        if (authTokenOpt.isEmpty()) {
            return Optional.empty();
        }

        AuthToken authToken = authTokenOpt.get();
        if (Instant.now().isAfter(authToken.getExpiresAt())) {
            authTokenRepository.delete(authToken);
            return Optional.empty();
        }

        // Revocar token antiguo y crear uno nuevo
        authTokenRepository.delete(authToken);
        String newToken = UUID.randomUUID().toString().replace("-", "");
        Instant expiresAt = Instant.now().plus(TOKEN_TTL);
        AuthToken newAuthToken = new AuthToken(authToken.getUsuario(), newToken, expiresAt);
        authTokenRepository.save(newAuthToken);

        Usuario usuario = authToken.getUsuario();
        List<String> authorities = usuario.getRol() != null
                ? List.of("ROLE_" + usuario.getRol().getNombre())
                : List.of();

        UsuarioSesionDto usuarioSesion = new UsuarioSesionDto(
                usuario.getUsuariosId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getRol() != null ? usuario.getRol().getNombre() : null);

        return Optional.of(new LoginToken(newToken, expiresAt, usuarioSesion, authorities));
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
