package com.ooas.model;

public record TrackingRequest(POStatus status, String location, String notes, String evidenceFileUrl) {
}
