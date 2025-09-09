package chrisgreer.bookingsystem.repositories;

import chrisgreer.bookingsystem.entities.Booking;
import chrisgreer.bookingsystem.model.BookingStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
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
