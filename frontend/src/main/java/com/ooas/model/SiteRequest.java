package com.ooas.model;

public record SiteRequest(String code, String name, String country, int seaLeadTime, int airLeadTime, Boolean active) {
}
