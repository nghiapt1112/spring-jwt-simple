package com.example.demo.infrastructure.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Generic event class for all system events
 * @param <T> The type of event data
 */
@Getter
@RequiredArgsConstructor
public class Event<T> {
    private final String eventId;
    private final String eventType;
    private final String userId;
    private final T eventData;
}
