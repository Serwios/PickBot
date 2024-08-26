package org.archivision.pickbot.service;

import lombok.RequiredArgsConstructor;
import org.archivision.pickbot.entity.UserStat;
import org.archivision.pickbot.repo.UserStatRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserStatService {
    private final UserStatRepository userStatRepository;

    public void recordInteraction(Long userId, String username) {
        final Optional<UserStat> existingStat = userStatRepository.findByUserId(userId);

        if (existingStat.isPresent()) {
            final UserStat userStat = existingStat.get();
            userStat.updateInteraction(LocalDateTime.now());
            userStatRepository.save(userStat);
        } else {
            final UserStat newUserStat = new UserStat(userId, username, LocalDateTime.now());
            newUserStat.updateInteraction(LocalDateTime.now());
            userStatRepository.save(newUserStat);
        }
    }

    public long countActiveUsers(int days) {
        return userStatRepository.countByLastInteractionDateAfter(LocalDateTime.now().minusDays(days));
    }
}
