package com.ooas.desktop.shared.model;

public record TrackingRequest(POStatus status, String location, String notes, String evidenceFileUrl) {
}

