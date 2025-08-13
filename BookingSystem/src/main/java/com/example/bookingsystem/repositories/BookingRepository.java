package com.example.bookingsystem.repositories;

import com.example.bookingsystem.entities.Booking;
import com.example.bookingsystem.model.BookingStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByStatus(BookingStatus status, Sort by);
}
