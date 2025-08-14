package com.example.bookingsystem.repositories;

import com.example.bookingsystem.entities.Booking;
import com.example.bookingsystem.model.BookingStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByStatus(BookingStatus status, Sort by);
    List<Booking> findAllByDeletedAndStatus(boolean deleted, BookingStatus status, Sort by);
    List<Booking> findAllByDeleted(boolean deleted, Sort by);

    @Query(value = """
    SELECT * FROM booking WHERE deleted = :deleted AND status = :status AND start_time > NOW() ORDER BY start_time
    """, nativeQuery = true)
    List<Booking> findFutureFilterStatus(
            @Param("deleted") boolean deleted,
            @Param("status") String status
    );

    @Query(value = """
    SELECT * FROM booking WHERE deleted = :deleted AND start_time > NOW() ORDER BY start_time
    """, nativeQuery = true)
    List<Booking> findFuture(@Param("deleted") boolean deleted);


}
