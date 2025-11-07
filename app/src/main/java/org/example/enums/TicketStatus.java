package org.example.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TicketStatus {
    BOOKED,
    CANCEL;

    @JsonCreator
    public static TicketStatus fromString(String key) {
        return key == null ? null : TicketStatus.valueOf(key.toUpperCase());
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}
