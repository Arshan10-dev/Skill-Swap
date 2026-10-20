package com.skillswap.controller;

import com.skillswap.dto.Dtos.*;
import com.skillswap.model.User;
import com.skillswap.repo.UserRepository;
import com.skillswap.security.CurrentUser;
import com.skillswap.service.*;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private static final Set<String> DAYS = Set.of("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN");

    private final UserRepository users;
    private final ProfileService profiles;
    private final ReviewService reviews;
    private final PasswordEncoder encoder;
    private final UserLookup lookup;

    public UserController(UserRepository users, ProfileService profiles, ReviewService reviews,
                          PasswordEncoder encoder, UserLookup lookup) {
        this.users = users;
        this.profiles = profiles;
        this.reviews = reviews;
        this.encoder = encoder;
        this.lookup = lookup;
    }

    @GetMapping("/me")
    public ProfileResponse me() { return profiles.build(CurrentUser.id(), CurrentUser.id()); }

    @PutMapping("/me")
    @Transactional
    public ProfileResponse update(@Valid @RequestBody ProfileUpdate in) {
        User u = lookup.get(CurrentUser.id());
        u.setName(in.name().trim());
        u.setBio(in.bio());
        u.setLocation(in.location());
        if (in.availableDays() != null) {
            String days = String.join(",", java.util.Arrays.stream(in.availableDays().toUpperCase().split(","))
                    .map(String::trim).filter(DAYS::contains).distinct().toList());
            u.setAvailableDays(days);
        }
        u.setPreferredTime(in.preferredTime());
        u.setPreferredMode(in.preferredMode());
        users.save(u);
        return profiles.build(u.getId(), u.getId());
    }

    @PutMapping("/me/password")
    @Transactional
    public void changePassword(@Valid @RequestBody PasswordChange in) {
        User u = lookup.get(CurrentUser.id());
        if (!encoder.matches(in.currentPassword(), u.getPassword())) throw ApiException.bad("Current password is incorrect");
        u.setPassword(encoder.encode(in.newPassword()));
    }

    @GetMapping("/{id}")
    public ProfileResponse profile(@PathVariable Long id) { return profiles.build(id, CurrentUser.id()); }

    @GetMapping("/{id}/reviews")
    public List<ReviewDto> reviews(@PathVariable Long id) { return reviews.forUser(id); }
}
