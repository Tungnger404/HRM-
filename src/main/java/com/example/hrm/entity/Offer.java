package com.example.hrm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "offers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "offer_id")
    private Integer offerId;

    @ManyToOne
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @Column(name = "salary_offered")
    private Double salaryOffered;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "probation_period")
    private String probationPeriod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private OfferStatus status;

    @Column(name = "offer_letter_url")
    private String offerLetterUrl;
}