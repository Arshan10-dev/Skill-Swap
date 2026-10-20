package com.skillswap.controller;

import com.skillswap.dto.Dtos.MatchDto;
import com.skillswap.security.CurrentUser;
import com.skillswap.service.MatchingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
public class MatchController {
    private final MatchingService matching;
    public MatchController(MatchingService matching) { this.matching = matching; }

    /** Recommended partners, best first. Optional filters: skillId (they teach it), minScore, limit. */
    @GetMapping
    public List<MatchDto> matches(@RequestParam(required = false) Long skillId,
                                  @RequestParam(defaultValue = "0") int minScore,
                                  @RequestParam(defaultValue = "30") int limit) {
        return matching.recommendationsFor(CurrentUser.id(), skillId, minScore, Math.min(limit, 100));
    }
}
