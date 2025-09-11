package chrisgreer.bookingsystem;


import chrisgreer.bookingsystem.dtos.BookingDto;
import chrisgreer.bookingsystem.dtos.CreateAvailableSlotDto;
import chrisgreer.bookingsystem.dtos.RequestBookingDto;
import chrisgreer.bookingsystem.entities.Booking;
import chrisgreer.bookingsystem.mappers.BookingMapper;
import chrisgreer.bookingsystem.model.BookingStatus;
import chrisgreer.bookingsystem.repositories.BookingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;

import static chrisgreer.bookingsystem.model.BookingStatus.PENDING;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
public class BookingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    void createBooking_shouldReturnCreated() throws Exception {
        CreateAvailableSlotDto request = createValidBookingSlot();

        mockMvc.perform(post("/booking/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    void createBooking_shouldReturnBadRequest_whenMissingFields() throws Exception {
        RequestBookingDto request = new RequestBookingDto();
        request.setEmail("chris@example.com");

        mockMvc.perform(post("/booking/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_shouldReturnBadRequest_whenStartTimeAfterEndTime() throws Exception {
        CreateAvailableSlotDto request = createValidBookingSlot();
        request.setStartTime(OffsetDateTime.now().plusDays(2));
        request.setEndTime(OffsetDateTime.now().plusDays(1));

        mockMvc.perform(post("/booking/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void requestBooking_returnsNotFound_whenBookingDoesNotExist() throws Exception {
        RequestBookingDto request = createValidBookingRequest();

        mockMvc.perform(patch("/booking/{id}/request", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void requestBooking_returnsConflict_whenBookingNotAvailable() throws Exception {
        Booking booking = createValidBooking();
        booking.setStatus(PENDING); //Not AVAILABLE
        bookingRepository.save(booking);

        RequestBookingDto request = createValidBookingRequest();
        mockMvc.perform(patch("/booking/request/{id}", booking.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    private Booking createValidBooking(){
        Booking booking = new Booking();
        booking.setName("Bob");
        booking.setEmail("bob@example.com");
        booking.setStatus(BookingStatus.AVAILABLE);
        booking.setStartTime(OffsetDateTime.now().plusDays(1));
        booking.setEndTime(OffsetDateTime.now().plusDays(1).plusHours(1));
        return booking;
    }

    private CreateAvailableSlotDto createValidBookingSlot() {
        CreateAvailableSlotDto request = new CreateAvailableSlotDto();
        request.setStartTime(OffsetDateTime.now().plusDays(1));
        request.setEndTime(OffsetDateTime.now().plusDays(1).plusHours(1));
        return request;
    }

    private RequestBookingDto createValidBookingRequest() {
        RequestBookingDto request = new RequestBookingDto();
        request.setName("Valid Name");
        request.setPhone("01234567890");
        request.setEmail("valid@email.com");
        return request;
    }


}
