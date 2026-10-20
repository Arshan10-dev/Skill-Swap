package com.skillswap.service;

import com.skillswap.dto.Dtos.PublicUser;
import com.skillswap.model.User;
import com.skillswap.repo.ReviewRepository;
import com.skillswap.repo.UserRepository;
import org.springframework.stereotype.Component;

/** Small helper shared by services to load users and build public views. */
@Component
public class UserLookup {
    private final UserRepository users;
    private final ReviewRepository reviews;

    public UserLookup(UserRepository users, ReviewRepository reviews) { this.users = users; this.reviews = reviews; }

    public User get(Long id) { return users.findById(id).orElseThrow(() -> ApiException.notFound("User")); }

    public Double avg(Long userId) {
        Double a = reviews.averageFor(userId);
        return a == null ? null : Math.round(a * 10) / 10.0;
    }

    public PublicUser pub(User u) { return PublicUser.of(u, avg(u.getId())); }
}
