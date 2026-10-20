package com.skillswap.service;

import com.skillswap.dto.Dtos.*;
import com.skillswap.model.*;
import com.skillswap.repo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SwapService {
    private final SwapRequestRepository requests;
    private final SkillRepository skills;
    private final UserSkillRepository userSkills;
    private final BlockRepository blocks;
    private final MessageRepository messages;
    private final MatchingService matching;
    private final NotificationService notifications;
    private final UserLookup lookup;

    public SwapService(SwapRequestRepository requests, SkillRepository skills, UserSkillRepository userSkills,
                       BlockRepository blocks, MessageRepository messages, MatchingService matching,
                       NotificationService notifications, UserLookup lookup) {
        this.requests = requests;
        this.skills = skills;
        this.userSkills = userSkills;
        this.blocks = blocks;
        this.messages = messages;
        this.matching = matching;
        this.notifications = notifications;
        this.lookup = lookup;
    }

    @Transactional
    public SwapRequestDto create(Long me, SwapRequestCreate in) {
        if (in.receiverId().equals(me)) throw ApiException.bad("You cannot send a request to yourself");
        User sender = lookup.get(me);
        User receiver = lookup.get(in.receiverId());
        if (!receiver.isActive()) throw ApiException.bad("This user is not available");
        if (blocks.existsEitherWay(me, receiver.getId())) throw ApiException.forbidden("You cannot contact this user");
        if (requests.existsOpenBetween(me, receiver.getId()))
            throw ApiException.conflict("You already have an open request with this user");

        SwapRequest r = new SwapRequest();
        r.setSender(sender);
        r.setReceiver(receiver);
        if (in.offeredSkillId() != null) {
            if (!userSkills.existsByUserIdAndSkillIdAndType(me, in.offeredSkillId(), Enums.SkillType.TEACH))
                throw ApiException.bad("You can only offer skills listed in your 'can teach' list");
            r.setOfferedSkill(skills.findById(in.offeredSkillId()).orElseThrow(() -> ApiException.notFound("Skill")));
        }
        if (in.requestedSkillId() != null) {
            if (!userSkills.existsByUserIdAndSkillIdAndType(receiver.getId(), in.requestedSkillId(), Enums.SkillType.TEACH))
                throw ApiException.bad(receiver.getName() + " does not teach that skill");
            r.setRequestedSkill(skills.findById(in.requestedSkillId()).orElseThrow(() -> ApiException.notFound("Skill")));
        }
        if (r.getOfferedSkill() == null && r.getRequestedSkill() == null)
            throw ApiException.bad("Choose at least one skill to offer or learn");
        r.setMessage(in.message());
        r.setCompatibilityScore(matching.pairScore(me, receiver.getId()));
        requests.save(r);
        notifications.notify(receiver, Enums.NotificationType.REQUEST,
                sender.getName() + " sent you a skill-swap request", "/requests");
        return toDto(r, me);
    }

    @Transactional
    public SwapRequestDto respond(Long me, Long id, boolean accept) {
        SwapRequest r = find(id);
        if (!r.getReceiver().getId().equals(me)) throw ApiException.forbidden("Only the receiver can respond");
        if (r.getStatus() != Enums.RequestStatus.PENDING) throw ApiException.bad("This request is no longer pending");
        r.setStatus(accept ? Enums.RequestStatus.ACCEPTED : Enums.RequestStatus.REJECTED);
        r.setRespondedAt(LocalDateTime.now());
        notifications.notify(r.getSender(), Enums.NotificationType.REQUEST_UPDATE,
                r.getReceiver().getName() + (accept ? " accepted" : " declined") + " your skill-swap request",
                accept ? "/chat/" + r.getId() : "/requests");
        return toDto(r, me);
    }

    @Transactional
    public SwapRequestDto cancel(Long me, Long id) {
        SwapRequest r = find(id);
        if (!r.getSender().getId().equals(me)) throw ApiException.forbidden("Only the sender can cancel");
        if (r.getStatus() != Enums.RequestStatus.PENDING) throw ApiException.bad("Only pending requests can be cancelled");
        r.setStatus(Enums.RequestStatus.CANCELLED);
        return toDto(r, me);
    }

    @Transactional
    public SwapRequestDto complete(Long me, Long id) {
        SwapRequest r = find(id);
        if (!r.involves(me)) throw ApiException.forbidden("Not your exchange");
        if (r.getStatus() != Enums.RequestStatus.ACCEPTED) throw ApiException.bad("Only active exchanges can be closed");
        r.setStatus(Enums.RequestStatus.COMPLETED);
        notifications.notify(r.other(me), Enums.NotificationType.REQUEST_UPDATE,
                lookup.get(me).getName() + " marked your exchange as completed", "/requests");
        return toDto(r, me);
    }

    @Transactional(readOnly = true)
    public List<SwapRequestDto> incoming(Long me) {
        return requests.findByReceiverIdOrderByCreatedAtDesc(me).stream().map(r -> toDto(r, me)).toList();
    }

    @Transactional(readOnly = true)
    public List<SwapRequestDto> outgoing(Long me) {
        return requests.findBySenderIdOrderByCreatedAtDesc(me).stream().map(r -> toDto(r, me)).toList();
    }

    @Transactional(readOnly = true)
    public List<SwapRequestDto> active(Long me) {
        return requests.findForUserWithStatus(me, List.of(Enums.RequestStatus.ACCEPTED, Enums.RequestStatus.COMPLETED))
                .stream().map(r -> toDto(r, me)).toList();
    }

    @Transactional(readOnly = true)
    public SwapRequestDto get(Long me, Long id) {
        SwapRequest r = find(id);
        if (!r.involves(me)) throw ApiException.forbidden("Not your exchange");
        return toDto(r, me);
    }

    // ----- chat -----
    @Transactional
    public MessageDto send(Long me, Long requestId, String content) {
        SwapRequest r = chatAccess(me, requestId);
        if (r.getStatus() != Enums.RequestStatus.ACCEPTED)
            throw ApiException.bad("Chat is available only for active exchanges");
        if (blocks.existsEitherWay(r.getSender().getId(), r.getReceiver().getId()))
            throw ApiException.forbidden("Messaging is disabled because one of you blocked the other");
        Message m = new Message();
        m.setSwapRequest(r);
        m.setSender(lookup.get(me));
        m.setContent(content.trim());
        messages.save(m);
        notifications.notify(r.other(me), Enums.NotificationType.MESSAGE,
                "New message from " + m.getSender().getName(), "/chat/" + r.getId());
        return MessageDto.of(m);
    }

    @Transactional
    public List<MessageDto> history(Long me, Long requestId, Long afterId) {
        chatAccess(me, requestId);
        List<Message> list = afterId == null ? messages.findBySwapRequestIdOrderBySentAtAsc(requestId)
                : messages.findBySwapRequestIdAndIdGreaterThanOrderBySentAtAsc(requestId, afterId);
        list.stream().filter(m -> !m.getSender().getId().equals(me) && !m.isReadByRecipient())
                .forEach(m -> m.setReadByRecipient(true));
        return list.stream().map(MessageDto::of).toList();
    }

    private SwapRequest chatAccess(Long me, Long requestId) {
        SwapRequest r = find(requestId);
        if (!r.involves(me)) throw ApiException.forbidden("Not your exchange");
        if (r.getStatus() != Enums.RequestStatus.ACCEPTED && r.getStatus() != Enums.RequestStatus.COMPLETED)
            throw ApiException.bad("Chat opens once the request is accepted");
        return r;
    }

    SwapRequest find(Long id) { return requests.findById(id).orElseThrow(() -> ApiException.notFound("Request")); }

    SwapRequestDto toDto(SwapRequest r, Long me) {
        return new SwapRequestDto(r.getId(), lookup.pub(r.getSender()), lookup.pub(r.getReceiver()),
                r.getOfferedSkill() == null ? null : r.getOfferedSkill().getName(),
                r.getRequestedSkill() == null ? null : r.getRequestedSkill().getName(),
                r.getMessage(), r.getStatus().name(), r.getCompatibilityScore(), r.getCreatedAt(),
                r.getRespondedAt(), messages.countUnread(r.getId(), me));
    }
}
