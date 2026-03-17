package com.github.martinfrank.vbuddy.service;

import com.github.martinfrank.vbuddy.controller.exception.EntityNotFoundException;
import com.github.martinfrank.vbuddy.model.*;
import com.github.martinfrank.vbuddy.repository.BuddyRepository;
import com.github.martinfrank.vbuddy.repository.NeedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BuddyService {

    private final BuddyRepository buddyRepository;
    private final NeedRepository needRepository;
    private final BackgroundAgentService backgroundAgentService;

    public List<Buddy> findAll() {
        return buddyRepository.findAll();
    }

    public Buddy findById(Long id) {
        return buddyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Buddy", id));
    }

    @Transactional
    public Buddy create(String name, String personality) {
        Buddy buddy = new Buddy();
        buddy.setName(name);
        buddy.setPersonality(personality);
        buddy = buddyRepository.save(buddy);

        for (NeedType needType : NeedType.values()) {
            Need need = new Need();
            need.setBuddy(buddy);
            need.setNeedType(needType);
            needRepository.save(need);
        }

        Long buddyId = buddy.getId();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                backgroundAgentService.generateBackground(buddyId);
            }
        });

        return buddy;
    }

    public List<Need> getNeeds(Long buddyId) {
        return needRepository.findByBuddyId(buddyId);
    }
}
