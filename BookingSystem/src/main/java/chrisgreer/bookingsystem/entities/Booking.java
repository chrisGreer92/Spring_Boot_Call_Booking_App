package chrisgreer.bookingsystem.entities;

import chrisgreer.bookingsystem.model.BookingStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

import static chrisgreer.bookingsystem.model.BookingStatus.AVAILABLE;


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
    private OffsetDateTime startTime;

    @Column(name = "end_time")
    private OffsetDateTime endTime;

    @Column(name = "time_zone")
    private String timeZone;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private BookingStatus status = AVAILABLE;

    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();

    private boolean deleted = false;

}
