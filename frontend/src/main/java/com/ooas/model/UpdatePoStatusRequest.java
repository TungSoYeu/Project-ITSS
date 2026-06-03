package com.ooas.model;

import java.time.LocalDate;

public record UpdatePoStatusRequest(POStatus status, LocalDate actualArrivalDate) {
}
