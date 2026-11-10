package com.polduu.service;

import com.polduu.exception.InvalidRefreshTokenException;
import com.polduu.model.RefreshToken;
import com.polduu.model.User;
import com.polduu.repository.RefreshTokenRepository;
import com.polduu.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    // 7 days in milliseconds
    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }
    @Transactional
    public RefreshToken renewRefreshToken(User user) {
        refreshTokenRepository.deleteByUser(user);
        refreshTokenRepository.flush();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());
        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional(noRollbackFor = InvalidRefreshTokenException.class)
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new InvalidRefreshTokenException("Refresh token has expired. Please login again.");
        }
        return token;
    }



}