package chrisgreer.bookingsystem.services;

import chrisgreer.bookingsystem.dtos.BookingDto;
import chrisgreer.bookingsystem.dtos.CreateAvailableSlotDto;
import chrisgreer.bookingsystem.dtos.RequestBookingDto;
import chrisgreer.bookingsystem.dtos.UpdateBookingStatusDto;
import chrisgreer.bookingsystem.entities.Booking;
import chrisgreer.bookingsystem.mappers.BookingMapper;
import chrisgreer.bookingsystem.model.BookingStatus;
import chrisgreer.bookingsystem.repositories.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static chrisgreer.bookingsystem.model.BookingStatus.AVAILABLE;
import static chrisgreer.bookingsystem.model.BookingStatus.PENDING;

@Service
@RequiredArgsConstructor
public class BookingService {

    BookingRepository bookingRepository;
    BookingMapper bookingMapper;
    EmailService emailService;

    private static final Set<String> SORT_FIELDS
            = Set.of("id", "status", "startTime");
    public static final String DEFAULT_SORT = "id";

    public Iterable<BookingDto> getAvailableBookings(){
        return bookingRepository
                .findFutureFilterStatus(false, AVAILABLE.name())
                .stream().map(bookingMapper::toDto).toList();
    }

    public Iterable<BookingDto> getBookings(String sort,
                                            BookingStatus status,
                                            boolean deleted,
                                            boolean showPast){

        if(!SORT_FIELDS.contains(sort)) sort = DEFAULT_SORT;

        List<Booking> bookings;
        if (showPast) {
            bookings = (status != null)
                    ? bookingRepository.findAllByDeletedAndStatus(deleted, status, Sort.by(sort))
                    : bookingRepository.findAllByDeleted(deleted, Sort.by(sort));
        } else {
            bookings = (status != null)
                    //N.B Can't combine showing future only and sorting by something else other than start time
                    //Could implement but wouldn't be as clean, and it's not necessary for API
                    ? bookingRepository.findFutureFilterStatus(deleted, status.name())
                    : bookingRepository.findFuture(deleted);
        }

        return bookings.stream().map(bookingMapper::toDto).toList();
    }

    @Transactional
    public void createBooking(CreateAvailableSlotDto dto){
        var booking = bookingMapper.availableDtoToEntity(dto);
        bookingRepository.save(booking);
    }

    @Transactional
    public boolean requestBooking(Long id, RequestBookingDto dto){
        /// ???
        return true;
    }

    @Transactional
    public boolean updateBookingStatus(Long id, UpdateBookingStatusDto dto){
        var booking = bookingRepository.findById(id).orElse(null);
        if(booking == null) return false;

        booking.setStatus(dto.getStatus());
        bookingRepository.save(booking);

        emailService.sendBookingUpdated(booking);

        return true;
    }

    @Transactional
    public boolean deleteBooking(Long id){
        var booking = bookingRepository.findById(id).orElse(null);
        if(booking == null) return false;

        bookingRepository.delete(booking);
        return true;
    }

}
