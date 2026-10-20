package com.skillswap.controller;

import com.skillswap.dto.Dtos.*;
import com.skillswap.security.CurrentUser;
import com.skillswap.service.SwapService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
public class RequestController {
    private final SwapService swaps;
    public RequestController(SwapService swaps) { this.swaps = swaps; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SwapRequestDto create(@Valid @RequestBody SwapRequestCreate in) { return swaps.create(CurrentUser.id(), in); }

    @GetMapping("/incoming")
    public List<SwapRequestDto> incoming() { return swaps.incoming(CurrentUser.id()); }

    @GetMapping("/outgoing")
    public List<SwapRequestDto> outgoing() { return swaps.outgoing(CurrentUser.id()); }

    /** Accepted and completed exchanges: the user's "partners". */
    @GetMapping("/active")
    public List<SwapRequestDto> active() { return swaps.active(CurrentUser.id()); }

    @GetMapping("/{id}")
    public SwapRequestDto get(@PathVariable Long id) { return swaps.get(CurrentUser.id(), id); }

    @PostMapping("/{id}/accept")
    public SwapRequestDto accept(@PathVariable Long id) { return swaps.respond(CurrentUser.id(), id, true); }

    @PostMapping("/{id}/reject")
    public SwapRequestDto reject(@PathVariable Long id) { return swaps.respond(CurrentUser.id(), id, false); }

    @PostMapping("/{id}/cancel")
    public SwapRequestDto cancel(@PathVariable Long id) { return swaps.cancel(CurrentUser.id(), id); }

    @PostMapping("/{id}/complete")
    public SwapRequestDto complete(@PathVariable Long id) { return swaps.complete(CurrentUser.id(), id); }

    // ----- chat (REST; the frontend polls every few seconds, WebSocket push is optional) -----
    @GetMapping("/{id}/messages")
    public List<MessageDto> messages(@PathVariable Long id, @RequestParam(required = false) Long after) {
        return swaps.history(CurrentUser.id(), id, after);
    }

    @PostMapping("/{id}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageDto send(@PathVariable Long id, @Valid @RequestBody MessageCreate in) {
        return swaps.send(CurrentUser.id(), id, in.content());
    }
}
