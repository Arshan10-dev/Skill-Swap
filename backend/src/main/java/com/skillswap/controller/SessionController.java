package com.skillswap.controller;

import com.skillswap.dto.Dtos.*;
import com.skillswap.security.CurrentUser;
import com.skillswap.service.ReviewService;
import com.skillswap.service.SessionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SessionController {
    private final SessionService sessions;
    private final ReviewService reviews;

    public SessionController(SessionService sessions, ReviewService reviews) {
        this.sessions = sessions;
        this.reviews = reviews;
    }

    @GetMapping("/sessions")
    public List<SessionDto> mine() { return sessions.forUser(CurrentUser.id()); }

    @GetMapping("/requests/{requestId}/sessions")
    public List<SessionDto> forRequest(@PathVariable Long requestId) { return sessions.forRequest(CurrentUser.id(), requestId); }

    @PostMapping("/sessions")
    @ResponseStatus(HttpStatus.CREATED)
    public SessionDto schedule(@Valid @RequestBody SessionCreate in) { return sessions.schedule(CurrentUser.id(), in); }

    @PutMapping("/sessions/{id}")
    public SessionDto update(@PathVariable Long id, @Valid @RequestBody SessionUpdate in) {
        return sessions.update(CurrentUser.id(), id, in);
    }

    @PostMapping("/sessions/{id}/complete")
    public SessionDto complete(@PathVariable Long id) { return sessions.complete(CurrentUser.id(), id); }

    @PostMapping("/sessions/{id}/cancel")
    public SessionDto cancel(@PathVariable Long id) { return sessions.cancel(CurrentUser.id(), id); }

    @PostMapping("/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewDto review(@Valid @RequestBody ReviewCreate in) { return reviews.create(CurrentUser.id(), in); }
}
