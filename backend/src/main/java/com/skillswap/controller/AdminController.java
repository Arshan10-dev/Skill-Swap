package com.skillswap.controller;

import com.skillswap.dto.Dtos.*;
import com.skillswap.model.*;
import com.skillswap.repo.*;
import com.skillswap.security.CurrentUser;
import com.skillswap.service.ApiException;
import com.skillswap.service.NotificationService;
import com.skillswap.service.UserLookup;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final UserRepository users;
    private final CategoryRepository categories;
    private final SkillRepository skills;
    private final UserSkillRepository userSkills;
    private final SwapRequestRepository requests;
    private final SkillSessionRepository sessions;
    private final ReviewRepository reviews;
    private final ReportRepository reports;
    private final NotificationService notifications;
    private final UserLookup lookup;

    public AdminController(UserRepository users, CategoryRepository categories, SkillRepository skills,
                           UserSkillRepository userSkills, SwapRequestRepository requests,
                           SkillSessionRepository sessions, ReviewRepository reviews, ReportRepository reports,
                           NotificationService notifications, UserLookup lookup) {
        this.users = users;
        this.categories = categories;
        this.skills = skills;
        this.userSkills = userSkills;
        this.requests = requests;
        this.sessions = sessions;
        this.reviews = reviews;
        this.reports = reports;
        this.notifications = notifications;
        this.lookup = lookup;
    }

    // ----- users -----
    @GetMapping("/users")
    public List<UserSummary> users(@RequestParam(required = false) String q) {
        return users.findAll().stream()
                .filter(u -> q == null || q.isBlank() || u.getName().toLowerCase().contains(q.toLowerCase())
                        || u.getEmail().toLowerCase().contains(q.toLowerCase()))
                .map(u -> UserSummary.of(u, lookup.avg(u.getId()))).toList();
    }

    @PostMapping("/users/{id}/suspend")
    @Transactional
    public UserSummary suspend(@PathVariable Long id) { return setActive(id, false); }

    @PostMapping("/users/{id}/activate")
    @Transactional
    public UserSummary activate(@PathVariable Long id) { return setActive(id, true); }

    private UserSummary setActive(Long id, boolean active) {
        if (id.equals(CurrentUser.id())) throw ApiException.bad("You cannot change your own status");
        User u = lookup.get(id);
        if (u.getRole() == Enums.Role.ADMIN) throw ApiException.bad("Administrators cannot be suspended");
        u.setActive(active);
        return UserSummary.of(u, lookup.avg(id));
    }

    // ----- skills & categories -----
    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto addCategory(@Valid @RequestBody CategoryCreate in) {
        if (categories.findByNameIgnoreCase(in.name().trim()).isPresent()) throw ApiException.conflict("Category exists");
        Category c = categories.save(new Category(in.name().trim(), in.icon()));
        return new CategoryDto(c.getId(), c.getName(), c.getIcon(), 0);
    }

    @DeleteMapping("/categories/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long id) {
        if (skills.countByCategoryId(id) > 0) throw ApiException.bad("Move or delete the skills in this category first");
        categories.deleteById(id);
    }

    @PostMapping("/skills")
    @ResponseStatus(HttpStatus.CREATED)
    public SkillDto addSkill(@Valid @RequestBody SkillCreate in) {
        if (skills.findByNameIgnoreCase(in.name().trim()).isPresent()) throw ApiException.conflict("Skill exists");
        Category c = categories.findById(in.categoryId()).orElseThrow(() -> ApiException.notFound("Category"));
        return SkillDto.of(skills.save(new Skill(in.name().trim(), c)));
    }

    @PutMapping("/skills/{id}")
    @Transactional
    public SkillDto updateSkill(@PathVariable Long id, @Valid @RequestBody SkillCreate in) {
        Skill s = skills.findById(id).orElseThrow(() -> ApiException.notFound("Skill"));
        s.setName(in.name().trim());
        s.setCategory(categories.findById(in.categoryId()).orElseThrow(() -> ApiException.notFound("Category")));
        return SkillDto.of(s);
    }

    @DeleteMapping("/skills/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSkill(@PathVariable Long id) {
        if (userSkills.existsBySkillId(id)) throw ApiException.bad("This skill is used by members and cannot be deleted");
        skills.deleteById(id);
    }

    // ----- reports / moderation -----
    @GetMapping("/reports")
    public List<ReportDto> reports(@RequestParam(required = false) Enums.ReportStatus status) {
        List<Report> list = status == null ? reports.findAllByOrderByCreatedAtDesc() : reports.findByStatusOrderByCreatedAtDesc(status);
        return list.stream().map(r -> new ReportDto(r.getId(), lookup.pub(r.getReporter()), lookup.pub(r.getReported()),
                r.getReason(), r.getDetails(), r.getStatus().name(), r.getAdminNote(), r.getCreatedAt(),
                r.getResolvedAt(), reports.countByReportedId(r.getReported().getId()))).toList();
    }

    @PostMapping("/reports/{id}/resolve")
    @Transactional
    public void resolve(@PathVariable Long id, @Valid @RequestBody ReportResolve in) {
        Report r = reports.findById(id).orElseThrow(() -> ApiException.notFound("Report"));
        r.setStatus(in.status());
        r.setAdminNote(in.adminNote());
        r.setResolvedAt(LocalDateTime.now());
        if (in.suspendUser() && r.getReported().getRole() != Enums.Role.ADMIN) r.getReported().setActive(false);
        notifications.notify(r.getReporter(), Enums.NotificationType.SYSTEM,
                "Your report about " + r.getReported().getName() + " was reviewed by the moderation team", "/notifications");
    }

    // ----- analytics -----
    @GetMapping("/analytics")
    @Transactional(readOnly = true)
    public AnalyticsDto analytics() {
        long total = users.count();
        long suspended = users.countByActiveFalse();
        Double avg = reviews.platformAverage();
        return new AnalyticsDto(total, total - suspended, suspended,
                users.countByCreatedAtAfter(LocalDateTime.now().minusDays(7)), skills.count(), requests.count(),
                requests.countByStatus(Enums.RequestStatus.PENDING), requests.countByStatus(Enums.RequestStatus.ACCEPTED),
                sessions.countByStatus(Enums.SessionStatus.COMPLETED), sessions.countByStatus(Enums.SessionStatus.SCHEDULED),
                reports.countByStatus(Enums.ReportStatus.OPEN), avg == null ? null : Math.round(avg * 10) / 10.0,
                top(userSkills.topSkills(Enums.SkillType.TEACH)), top(userSkills.topSkills(Enums.SkillType.LEARN)),
                Arrays.stream(Enums.RequestStatus.values())
                        .map(s -> new NameCount(s.name(), requests.countByStatus(s))).toList());
    }

    private static List<NameCount> top(List<Object[]> rows) {
        return rows.stream().limit(6).map(r -> new NameCount((String) r[0], ((Number) r[1]).longValue())).toList();
    }
}
