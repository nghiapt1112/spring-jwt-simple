package com.example.demo.infrastructure.event.reward;

import com.example.demo.model.UserRewardPoints;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Data for reward events
 */
@Getter
@RequiredArgsConstructor
public class RewardEventData {
    private final int points;
    private final UserRewardPoints userRewardPoints;
}
