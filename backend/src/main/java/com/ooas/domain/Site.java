package com.ooas.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "sites")
public class Site extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String country;

    @Column(name = "sea_lead_time", nullable = false)
    private int seaLeadTime;

    @Column(name = "air_lead_time", nullable = false)
    private int airLeadTime;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    public Site(String code, String name, String country, int seaLeadTime, int airLeadTime, boolean active) {
        this.code = code;
        this.name = name;
        this.country = country;
        this.seaLeadTime = seaLeadTime;
        this.airLeadTime = airLeadTime;
        this.active = active;
    }
}
