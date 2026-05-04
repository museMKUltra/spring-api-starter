package com.codewithmosh.store.auth;

import com.codewithmosh.store.users.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@AllArgsConstructor
public class GuestCleanupService {
    private final UserRepository userRepository;

    @Transactional
    @Scheduled(cron = "0 0 * * * *")
    public void cleanupGuests() {
        var guests = userRepository
                .findAllByGuestTrueAndExpiresAtBefore(Instant.now());

        userRepository.deleteAll(guests);
    }
}
