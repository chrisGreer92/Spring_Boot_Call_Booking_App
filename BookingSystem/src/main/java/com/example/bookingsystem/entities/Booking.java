package com.example.bookingsystem.entities;

import com.example.bookingsystem.model.BookingStatus;
import static com.example.bookingsystem.model.BookingStatus.PENDING;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Entity
@Table(name = "booking")
@Getter
@Setter
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "phone_number")
    private String phone;

    @Column(name = "topic")
    private String topic;

    @Column(name = "notes")
    private String notes;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "time_zone")
    private String timeZone;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private BookingStatus status = PENDING;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

}
