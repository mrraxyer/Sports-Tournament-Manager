package dev.mrraxyer.sportstournamentmanager.repositories;

import dev.mrraxyer.sportstournamentmanager.models.AuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface AuthTokenRepository extends JpaRepository<AuthToken, Integer> {
    Optional<AuthToken> findByTokenValue(String tokenValue);

    @Query("DELETE FROM AuthToken t WHERE t.expiresAt < ?1")
    void deleteExpiredTokens(Instant now);
}
