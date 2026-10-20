package com.skillswap.service;

import com.skillswap.dto.Dtos.MatchDto;
import com.skillswap.model.Enums;
import com.skillswap.model.User;
import com.skillswap.model.UserSkill;
import com.skillswap.repo.BlockRepository;
import com.skillswap.repo.UserSkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Matching engine.
 *
 * Compatibility score (0-100) between the current user A and a candidate B:
 *   40 pts  share of A's "want to learn" skills that B can teach
 *   25 pts  share of B's "want to learn" skills that A can teach
 *   10 pts  mutual bonus when both directions have at least one skill
 *   10 pts  teacher level fit (EXPERT=1.0, INTERMEDIATE=0.7, BEGINNER=0.4) on the skills B would teach A
 *   10 pts  overlap of available days (Jaccard)
 *    5 pts  preferred time and mode agree
 * A small reputation nudge (max +5, capped at 100) breaks ties in favour of trusted users.
 * Candidates with no skill overlap in either direction, inactive users, admins and blocked users are excluded.
 */
@Service
public class MatchingService {
    private final UserSkillRepository userSkills;
    private final BlockRepository blocks;
    private final UserLookup lookup;

    public MatchingService(UserSkillRepository userSkills, BlockRepository blocks, UserLookup lookup) {
        this.userSkills = userSkills;
        this.blocks = blocks;
        this.lookup = lookup;
    }

    @Transactional(readOnly = true)
    public List<MatchDto> recommendationsFor(Long userId, Long skillFilter, int minScore, int limit) {
        User me = lookup.get(userId);
        Map<User, List<UserSkill>> byUser = userSkills.findByUserActiveTrue().stream()
                .collect(Collectors.groupingBy(UserSkill::getUser));

        List<UserSkill> mine = byUser.getOrDefault(me, List.of());
        if (mine.isEmpty()) {
            // groupingBy uses entity equality, so fall back to id match
            mine = byUser.entrySet().stream().filter(e -> e.getKey().getId().equals(userId))
                    .findFirst().map(Map.Entry::getValue).orElse(List.of());
        }

        List<MatchDto> out = new ArrayList<>();
        for (Map.Entry<User, List<UserSkill>> e : byUser.entrySet()) {
            User other = e.getKey();
            if (other.getId().equals(userId) || other.getRole() == Enums.Role.ADMIN) continue;
            if (blocks.existsEitherWay(userId, other.getId())) continue;
            MatchDto m = score(me, mine, other, e.getValue());
            if (m == null || m.score() < minScore) continue;
            if (skillFilter != null) {
                boolean hit = e.getValue().stream().anyMatch(us -> us.getSkill().getId().equals(skillFilter)
                        && us.getType() == Enums.SkillType.TEACH);
                if (!hit) continue;
            }
            out.add(m);
        }
        out.sort(Comparator.comparingInt(MatchDto::score).reversed()
                .thenComparing(m -> -m.user().reputation()));
        return out.size() > limit ? out.subList(0, limit) : out;
    }

    /** Score a single pair; returns null if there is no overlap at all. */
    public MatchDto score(User a, List<UserSkill> aSkills, User b, List<UserSkill> bSkills) {
        Map<Long, UserSkill> aTeach = index(aSkills, Enums.SkillType.TEACH);
        Map<Long, UserSkill> aLearn = index(aSkills, Enums.SkillType.LEARN);
        Map<Long, UserSkill> bTeach = index(bSkills, Enums.SkillType.TEACH);
        Map<Long, UserSkill> bLearn = index(bSkills, Enums.SkillType.LEARN);

        List<UserSkill> bTeachesA = aLearn.keySet().stream().filter(bTeach::containsKey).map(bTeach::get).toList();
        List<UserSkill> aTeachesB = bLearn.keySet().stream().filter(aTeach::containsKey).map(aTeach::get).toList();
        if (bTeachesA.isEmpty() && aTeachesB.isEmpty()) return null;

        double s = 0;
        List<String> reasons = new ArrayList<>();

        if (!aLearn.isEmpty()) s += 40.0 * bTeachesA.size() / aLearn.size();
        if (!bLearn.isEmpty()) s += 25.0 * aTeachesB.size() / bLearn.size();

        boolean mutual = !bTeachesA.isEmpty() && !aTeachesB.isEmpty();
        if (mutual) { s += 10; reasons.add("Two-way swap: you both learn something"); }

        if (!bTeachesA.isEmpty()) {
            double fit = bTeachesA.stream().mapToDouble(us -> switch (us.getLevel()) {
                case EXPERT -> 1.0; case INTERMEDIATE -> 0.7; default -> 0.4; }).average().orElse(0);
            s += 10 * fit;
            long experts = bTeachesA.stream().filter(us -> us.getLevel() == Enums.Level.EXPERT).count();
            if (experts > 0) reasons.add("Expert-level teacher in " + experts + " skill" + (experts > 1 ? "s" : ""));
        }

        Set<String> aDays = days(a.getAvailableDays());
        Set<String> bDays = days(b.getAvailableDays());
        Set<String> common = new TreeSet<>(Comparator.comparingInt(MatchingService::dayOrder));
        common.addAll(aDays);
        common.retainAll(bDays);
        Set<String> union = new HashSet<>(aDays);
        union.addAll(bDays);
        if (!union.isEmpty()) s += 10.0 * common.size() / union.size();
        if (!common.isEmpty()) reasons.add(common.size() + " shared available day" + (common.size() > 1 ? "s" : ""));

        boolean timeOk = same(a.getPreferredTime(), b.getPreferredTime()) || "FLEXIBLE".equalsIgnoreCase(a.getPreferredTime())
                || "FLEXIBLE".equalsIgnoreCase(b.getPreferredTime());
        boolean modeOk = same(a.getPreferredMode(), b.getPreferredMode()) || "BOTH".equalsIgnoreCase(a.getPreferredMode())
                || "BOTH".equalsIgnoreCase(b.getPreferredMode());
        if (timeOk) s += 2.5;
        if (modeOk) s += 2.5;
        if (timeOk && modeOk) reasons.add("Same time and mode preference");

        s += Math.min(5, b.getReputation() / 40.0);
        if (b.getReputation() >= 100) reasons.add("Highly reputed member");

        int score = (int) Math.round(Math.min(100, s));
        return new MatchDto(lookup.pub(b), score, mutual,
                bTeachesA.stream().map(us -> us.getSkill().getName()).sorted().toList(),
                aTeachesB.stream().map(us -> us.getSkill().getName()).sorted().toList(),
                new ArrayList<>(common), reasons);
    }

    /** Compatibility for a specific pair, used when a request is created. */
    @Transactional(readOnly = true)
    public int pairScore(Long a, Long b) {
        MatchDto m = score(lookup.get(a), userSkills.findByUserId(a), lookup.get(b), userSkills.findByUserId(b));
        return m == null ? 0 : m.score();
    }

    private static Map<Long, UserSkill> index(List<UserSkill> list, Enums.SkillType type) {
        Map<Long, UserSkill> m = new HashMap<>();
        for (UserSkill us : list) if (us.getType() == type) m.put(us.getSkill().getId(), us);
        return m;
    }

    private static Set<String> days(String csv) {
        if (csv == null || csv.isBlank()) return Set.of();
        return Arrays.stream(csv.split(",")).map(String::trim).map(String::toUpperCase)
                .filter(x -> !x.isEmpty()).collect(Collectors.toSet());
    }

    private static boolean same(String x, String y) { return x != null && x.equalsIgnoreCase(y); }

    private static int dayOrder(String d) {
        return List.of("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN").indexOf(d);
    }
}
