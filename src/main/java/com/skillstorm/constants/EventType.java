package com.skillstorm.constants;

import lombok.Getter;
import reactor.core.publisher.Flux;

@Getter
public enum EventType {
    UNIVERSITY_COURSE(0.8),
    SEMINAR(0.6),
    CERT_PREP_CLASS(0.75),
    CERTIFICATION(1.0),
    TECH_TRAINING(0.9),
    OTHER(0.3);

    private final double rate;

    EventType(double rate) {
        this.rate = rate;
    }

    public static Flux<EventType> getEventTypes() {
        return Flux.fromArray(EventType.values());
    }
}
