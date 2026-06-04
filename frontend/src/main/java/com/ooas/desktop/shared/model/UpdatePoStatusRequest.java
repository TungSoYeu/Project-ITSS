package com.ooas.desktop.shared.model;

import java.time.LocalDate;

public record UpdatePoStatusRequest(POStatus status, LocalDate actualArrivalDate) {
}

