package com.ooas.service;

import com.ooas.entity.UserAccount;
import com.ooas.security.JwtPrincipal;

public interface JwtService {
    String generateToken(UserAccount user);
    JwtPrincipal parseToken(String token);
}
