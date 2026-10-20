package com.skillswap.config;

import com.skillswap.model.*;
import com.skillswap.repo.*;
import com.skillswap.service.ReputationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Seeds categories, skills, an admin and demo members on first start (empty database only).
 * Demo password for everyone: password123   Admin: admin@skillswap.com / admin123
 */
@Component
public class DataSeeder implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository users;
    private final CategoryRepository categories;
    private final SkillRepository skills;
    private final UserSkillRepository userSkills;
    private final SwapRequestRepository requests;
    private final SkillSessionRepository sessions;
    private final ReviewRepository reviews;
    private final MessageRepository messages;
    private final ReputationService reputation;
    private final PasswordEncoder encoder;
    private final boolean demo;

    private final Map<String, Skill> skillByName = new HashMap<>();

    public DataSeeder(UserRepository users, CategoryRepository categories, SkillRepository skills,
                      UserSkillRepository userSkills, SwapRequestRepository requests, SkillSessionRepository sessions,
                      ReviewRepository reviews, MessageRepository messages, ReputationService reputation,
                      PasswordEncoder encoder, @Value("${app.seed-demo-data:true}") boolean demo) {
        this.users = users;
        this.categories = categories;
        this.skills = skills;
        this.userSkills = userSkills;
        this.requests = requests;
        this.sessions = sessions;
        this.reviews = reviews;
        this.messages = messages;
        this.reputation = reputation;
        this.encoder = encoder;
        this.demo = demo;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (users.count() > 0) return;
        log.info("Seeding SkillSwap data...");

        cat("Programming", "code", "Java", "Python", "JavaScript", "React", "Spring Boot", "SQL", "C++", "Android Development");
        cat("Design", "palette", "UI/UX Design", "Figma", "Graphic Design", "Video Editing", "Photography");
        cat("Languages", "translate", "English Speaking", "Hindi", "Urdu", "French", "German", "Japanese");
        cat("Music", "music", "Guitar", "Piano", "Singing", "Tabla", "Music Production");
        cat("Business", "briefcase", "Public Speaking", "Digital Marketing", "Excel", "Content Writing", "Personal Finance");
        cat("Academics", "book", "Mathematics", "Physics", "Chemistry", "Engineering Drawing", "Data Structures");
        cat("Lifestyle", "heart", "Cooking", "Yoga", "Fitness Training", "Chess", "Drawing & Sketching");

        User admin = user("Platform Admin", "admin@skillswap.com", "admin123", Enums.Role.ADMIN, "Mumbai", "MON,TUE,WED,THU,FRI", "FLEXIBLE", "BOTH", null);

        if (!demo) return;

        User arshan = user("Arshan Ansari", "arshan@demo.com", "password123", Enums.Role.USER, "Mumbai",
                "MON,WED,SAT,SUN", "EVENING", "ONLINE", "Engineering student who loves building Java apps. Want to get better at design and public speaking.");
        User rashid = user("Rashid Khan", "rashid@demo.com", "password123", Enums.Role.USER, "Navi Mumbai",
                "TUE,SAT,SUN", "EVENING", "BOTH", "Full-stack learner. I can teach React and guitar; want to learn Spring Boot.");
        User priya = user("Priya Sharma", "priya@demo.com", "password123", Enums.Role.USER, "Thane",
                "MON,WED,FRI,SAT", "EVENING", "ONLINE", "Product designer, 4 years in UI/UX. Learning Python for data work.");
        User aman = user("Aman Verma", "aman@demo.com", "password123", Enums.Role.USER, "Pune",
                "SAT,SUN", "MORNING", "ONLINE", "Toastmasters member and marketing lead. Want to learn to code.");
        User sara = user("Sara D'Souza", "sara@demo.com", "password123", Enums.Role.USER, "Mumbai",
                "WED,SAT,SUN", "AFTERNOON", "IN_PERSON", "Classical piano teacher. Curious about French and photography.");
        User kabir = user("Kabir Mehta", "kabir@demo.com", "password123", Enums.Role.USER, "Bengaluru",
                "MON,TUE,THU", "EVENING", "ONLINE", "Backend engineer (Java, Spring Boot, SQL). Learning guitar and cooking.");
        User zoya = user("Zoya Shaikh", "zoya@demo.com", "password123", Enums.Role.USER, "Mumbai",
                "SAT,SUN", "FLEXIBLE", "BOTH", "Content writer and Urdu poetry fan. Want to learn Figma and video editing.");
        User rohan = user("Rohan Iyer", "rohan@demo.com", "password123", Enums.Role.USER, "Chennai",
                "FRI,SAT,SUN", "EVENING", "ONLINE", "Chess coach (FIDE 1900) and math tutor. Learning Japanese.");

        teach(arshan, "Java", Enums.Level.EXPERT, 3); teach(arshan, "Data Structures", Enums.Level.INTERMEDIATE, 2); teach(arshan, "Mathematics", Enums.Level.INTERMEDIATE, 2);
        learn(arshan, "UI/UX Design"); learn(arshan, "Public Speaking"); learn(arshan, "Guitar");

        teach(rashid, "React", Enums.Level.INTERMEDIATE, 2); teach(rashid, "JavaScript", Enums.Level.INTERMEDIATE, 2); teach(rashid, "Guitar", Enums.Level.EXPERT, 6);
        learn(rashid, "Spring Boot"); learn(rashid, "Java"); learn(rashid, "SQL");

        teach(priya, "UI/UX Design", Enums.Level.EXPERT, 4); teach(priya, "Figma", Enums.Level.EXPERT, 4); teach(priya, "Graphic Design", Enums.Level.INTERMEDIATE, 3);
        learn(priya, "Python"); learn(priya, "Java"); learn(priya, "Data Structures");

        teach(aman, "Public Speaking", Enums.Level.EXPERT, 5); teach(aman, "Digital Marketing", Enums.Level.EXPERT, 5);
        learn(aman, "JavaScript"); learn(aman, "Java"); learn(aman, "Excel");

        teach(sara, "Piano", Enums.Level.EXPERT, 10); teach(sara, "Singing", Enums.Level.INTERMEDIATE, 4);
        learn(sara, "French"); learn(sara, "Photography");

        teach(kabir, "Spring Boot", Enums.Level.EXPERT, 5); teach(kabir, "SQL", Enums.Level.EXPERT, 5); teach(kabir, "Java", Enums.Level.EXPERT, 6);
        learn(kabir, "Guitar"); learn(kabir, "Cooking"); learn(kabir, "UI/UX Design");

        teach(zoya, "Content Writing", Enums.Level.EXPERT, 4); teach(zoya, "Urdu", Enums.Level.EXPERT, 10); teach(zoya, "English Speaking", Enums.Level.INTERMEDIATE, 3);
        learn(zoya, "Figma"); learn(zoya, "Video Editing"); learn(zoya, "Public Speaking");

        teach(rohan, "Chess", Enums.Level.EXPERT, 8); teach(rohan, "Mathematics", Enums.Level.EXPERT, 6);
        learn(rohan, "Japanese"); learn(rohan, "Java"); learn(rohan, "Guitar");

        // A completed exchange with history: Rashid <-> Kabir (Spring Boot for Guitar)
        SwapRequest rk = request(rashid, kabir, "Guitar", "Spring Boot", "Hey Kabir! I can teach you guitar from scratch if you can help me with Spring Boot.", Enums.RequestStatus.ACCEPTED, 88);
        msg(rk, rashid, "Hey Kabir! Thanks for accepting. When are you free?");
        msg(rk, kabir, "Saturday evenings work best for me. Shall we start with a Spring Boot REST API?");
        msg(rk, rashid, "Perfect. I'll bring my guitar for the second half.");
        SkillSession s1 = session(rk, kabir, "Spring Boot basics: REST controllers", LocalDateTime.now().minusDays(6), Enums.SessionStatus.COMPLETED);
        SkillSession s2 = session(rk, rashid, "Guitar 101: chords and strumming", LocalDateTime.now().minusDays(3), Enums.SessionStatus.COMPLETED);
        session(rk, kabir, "Spring Data JPA and MySQL", LocalDateTime.now().plusDays(2).withHour(18).withMinute(30), Enums.SessionStatus.SCHEDULED);
        review(s1, rashid, kabir, 5, "Kabir explains Spring Boot clearly, with real examples. Highly recommended!");
        review(s1, kabir, rashid, 5, "Great student, came prepared with questions.");
        review(s2, kabir, rashid, 4, "Fun guitar session, learned my first three chords.");

        // Priya <-> Arshan accepted, one completed session
        SwapRequest pa = request(arshan, priya, "Java", "UI/UX Design", "Hi Priya, I'd love to learn UI/UX. I can teach Java and DSA in return.", Enums.RequestStatus.ACCEPTED, 79);
        msg(pa, priya, "Hi Arshan! Sounds good. Let's do a Figma walkthrough first.");
        SkillSession s3 = session(pa, priya, "Intro to UI/UX & Figma", LocalDateTime.now().minusDays(2), Enums.SessionStatus.COMPLETED);
        review(s3, arshan, priya, 5, "Priya is an amazing designer and teacher.");
        session(pa, arshan, "Java OOP fundamentals", LocalDateTime.now().plusDays(1).withHour(19).withMinute(0), Enums.SessionStatus.SCHEDULED);

        // Pending requests for the demo inbox
        request(aman, arshan, "Public Speaking", "Java", "Hey Arshan, want to swap Java lessons for public speaking coaching?", Enums.RequestStatus.PENDING, 74);
        request(zoya, priya, "Content Writing", "Figma", "Hi Priya! Would love Figma lessons; I can help with content writing.", Enums.RequestStatus.PENDING, 52);
        request(rohan, rashid, "Mathematics", "Guitar", "Guitar for maths tutoring?", Enums.RequestStatus.REJECTED, 41);

        for (User u : users.findAll()) reputation.evaluateBadges(u);
        log.info("Seed complete. Admin: admin@skillswap.com / admin123, demo users: <name>@demo.com / password123");
    }

    private void cat(String name, String icon, String... skillNames) {
        Category c = categories.save(new Category(name, icon));
        for (String s : skillNames) skillByName.put(s, skills.save(new Skill(s, c)));
    }

    private User user(String name, String email, String pw, Enums.Role role, String loc, String days, String time,
                      String mode, String bio) {
        User u = new User();
        u.setName(name); u.setEmail(email); u.setPassword(encoder.encode(pw)); u.setRole(role);
        u.setLocation(loc); u.setAvailableDays(days); u.setPreferredTime(time); u.setPreferredMode(mode); u.setBio(bio);
        u.setCreatedAt(LocalDateTime.now().minusDays((long) (Math.random() * 40) + 1));
        return users.save(u);
    }

    private void teach(User u, String skill, Enums.Level level, int years) {
        userSkills.save(new UserSkill(u, skillByName.get(skill), Enums.SkillType.TEACH, level, years));
    }

    private void learn(User u, String skill) {
        userSkills.save(new UserSkill(u, skillByName.get(skill), Enums.SkillType.LEARN, Enums.Level.BEGINNER, 0));
    }

    private SwapRequest request(User from, User to, String offered, String requested, String message,
                                Enums.RequestStatus status, int score) {
        SwapRequest r = new SwapRequest();
        r.setSender(from); r.setReceiver(to);
        r.setOfferedSkill(skillByName.get(offered)); r.setRequestedSkill(skillByName.get(requested));
        r.setMessage(message); r.setStatus(status); r.setCompatibilityScore(score);
        if (status != Enums.RequestStatus.PENDING) r.setRespondedAt(LocalDateTime.now().minusDays(7));
        return requests.save(r);
    }

    private void msg(SwapRequest r, User from, String text) {
        Message m = new Message();
        m.setSwapRequest(r); m.setSender(from); m.setContent(text); m.setReadByRecipient(true);
        messages.save(m);
    }

    private SkillSession session(SwapRequest r, User by, String title, LocalDateTime at, Enums.SessionStatus status) {
        SkillSession s = new SkillSession();
        s.setSwapRequest(r); s.setCreatedBy(by); s.setTitle(title); s.setScheduledAt(at.withSecond(0).withNano(0));
        s.setDurationMinutes(60); s.setMode(Enums.SessionMode.ONLINE); s.setLocationOrLink("https://meet.google.com/");
        s.setStatus(status);
        if (status == Enums.SessionStatus.COMPLETED) {
            s.setCompletedAt(at.plusHours(1));
            s.setReminderSent(true);
        }
        s = sessions.save(s);
        if (status == Enums.SessionStatus.COMPLETED) {
            r.getSender().setReputation(r.getSender().getReputation() + ReputationService.POINTS_SESSION);
            r.getReceiver().setReputation(r.getReceiver().getReputation() + ReputationService.POINTS_SESSION);
        }
        return s;
    }

    private void review(SkillSession s, User from, User to, int rating, String comment) {
        Review r = new Review();
        r.setSession(s); r.setReviewer(from); r.setReviewee(to); r.setRating(rating); r.setComment(comment);
        reviews.save(r);
        to.setReputation(to.getReputation() + rating * 4);
        from.setReputation(from.getReputation() + ReputationService.POINTS_WRITE_REVIEW);
    }
}
