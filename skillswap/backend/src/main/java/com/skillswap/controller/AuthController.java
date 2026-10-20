package com.skillswap.controller;

import com.skillswap.dto.Dtos.*;
import com.skillswap.model.User;
import com.skillswap.repo.UserRepository;
import com.skillswap.security.JwtService;
import com.skillswap.service.ApiException;
import com.skillswap.service.UserLookup;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final UserLookup lookup;

    public AuthController(UserRepository users, PasswordEncoder encoder, JwtService jwt, UserLookup lookup) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
        this.lookup = lookup;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest in) {
        if (users.existsByEmailIgnoreCase(in.email())) throw ApiException.conflict("An account with this email already exists");
        User u = new User();
        u.setName(in.name().trim());
        u.setEmail(in.email().trim().toLowerCase());
        u.setPassword(encoder.encode(in.password()));
        u.setAvailableDays("SAT,SUN");
        u.setPreferredTime("FLEXIBLE");
        users.save(u);
        return token(u);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest in) {
        User u = users.findByEmailIgnoreCase(in.email().trim())
                .filter(x -> encoder.matches(in.password(), x.getPassword()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        if (!u.isActive()) throw ApiException.forbidden("Your account has been suspended. Contact the administrator.");
        return token(u);
    }

    private AuthResponse token(User u) {
        return new AuthResponse(jwt.generate(u.getId(), u.getEmail(), u.getRole().name()),
                UserSummary.of(u, lookup.avg(u.getId())));
    }
}
