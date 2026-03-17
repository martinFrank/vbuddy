package com.github.martinfrank.vbuddy.service;

import com.github.martinfrank.vbuddy.controller.exception.EntityNotFoundException;
import com.github.martinfrank.vbuddy.model.Buddy;
import com.github.martinfrank.vbuddy.model.Need;
import com.github.martinfrank.vbuddy.model.NeedType;
import com.github.martinfrank.vbuddy.repository.BuddyRepository;
import com.github.martinfrank.vbuddy.repository.NeedRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BuddyServiceTest {

    @BeforeEach
    void setUp() {
        TransactionSynchronizationManager.initSynchronization();
    }

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.clearSynchronization();
    }

    @Mock
    private BuddyRepository buddyRepository;

    @Mock
    private NeedRepository needRepository;

    @Mock
    private BackgroundAgentService backgroundAgentService;

    @InjectMocks
    private BuddyService buddyService;

    @Test
    void findAll_returnsList() {
        Buddy buddy = createBuddy(1L, "Max", "freundlich");
        when(buddyRepository.findAll()).thenReturn(List.of(buddy));

        List<Buddy> result = buddyService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo("Max");
    }

    @Test
    void findById_existingBuddy_returnsBuddy() {
        Buddy buddy = createBuddy(1L, "Max", "freundlich");
        when(buddyRepository.findById(1L)).thenReturn(Optional.of(buddy));

        Buddy result = buddyService.findById(1L);

        assertThat(result.getName()).isEqualTo("Max");
    }

    @Test
    void findById_nonExistingBuddy_throwsEntityNotFoundException() {
        when(buddyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> buddyService.findById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Buddy")
                .hasMessageContaining("99");
    }

    @Test
    void create_savesNeedsForAllNeedTypes() {
        Buddy savedBuddy = createBuddy(1L, "Max", "freundlich");
        when(buddyRepository.save(any(Buddy.class))).thenReturn(savedBuddy);

        buddyService.create("Max", "freundlich");

        ArgumentCaptor<Need> needCaptor = ArgumentCaptor.forClass(Need.class);
        verify(needRepository, times(NeedType.values().length)).save(needCaptor.capture());

        List<Need> savedNeeds = needCaptor.getAllValues();
        assertThat(savedNeeds).hasSize(NeedType.values().length);
        assertThat(savedNeeds.stream().map(Need::getNeedType).distinct().count())
                .isEqualTo(NeedType.values().length);
    }

    @Test
    void create_setsNameAndPersonality() {
        Buddy savedBuddy = createBuddy(1L, "Max", "freundlich");
        when(buddyRepository.save(any(Buddy.class))).thenReturn(savedBuddy);

        Buddy result = buddyService.create("Max", "freundlich");

        ArgumentCaptor<Buddy> buddyCaptor = ArgumentCaptor.forClass(Buddy.class);
        verify(buddyRepository).save(buddyCaptor.capture());
        assertThat(buddyCaptor.getValue().getName()).isEqualTo("Max");
        assertThat(buddyCaptor.getValue().getPersonality()).isEqualTo("freundlich");
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getNeeds_delegatesToRepository() {
        Need need = new Need();
        need.setNeedType(NeedType.HUNGER);
        when(needRepository.findByBuddyId(1L)).thenReturn(List.of(need));

        List<Need> result = buddyService.getNeeds(1L);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getNeedType()).isEqualTo(NeedType.HUNGER);
    }

    private Buddy createBuddy(Long id, String name, String personality) {
        Buddy buddy = new Buddy();
        buddy.setId(id);
        buddy.setName(name);
        buddy.setPersonality(personality);
        return buddy;
    }
}
