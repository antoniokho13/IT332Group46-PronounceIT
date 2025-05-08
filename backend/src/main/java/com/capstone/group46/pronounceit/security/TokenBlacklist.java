package com.capstone.group46.pronounceit.security;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class TokenBlacklist {
    private final Set<String> blacklistedTokens = new HashSet<>();

    public void addToken(String token) {
        blacklistedTokens.add(token);
    }

    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }
}
