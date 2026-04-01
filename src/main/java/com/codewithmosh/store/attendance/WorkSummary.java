package com.codewithmosh.store.attendance;

import com.codewithmosh.store.users.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "work_summary", schema = "store_api")
public class WorkSummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "year")
    private Integer year;

    @Column(name = "month")
    private Short month;

    @Column(name = "total_minutes")
    private Long totalMinutes;

    @Column(name = "hourly_rate")
    private BigDecimal hourlyRate;

    @Column(name = "salary_amount")
    private BigDecimal salaryAmount;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private SummaryStatus status;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
}