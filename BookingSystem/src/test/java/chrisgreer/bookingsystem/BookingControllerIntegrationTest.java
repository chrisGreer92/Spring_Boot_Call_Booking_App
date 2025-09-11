package chrisgreer.bookingsystem;


import chrisgreer.bookingsystem.dtos.CreateAvailableSlotDto;
import chrisgreer.bookingsystem.dtos.RequestBookingDto;
import chrisgreer.bookingsystem.dtos.UpdateBookingStatusDto;
import chrisgreer.bookingsystem.entities.Booking;
import chrisgreer.bookingsystem.repositories.BookingRepository;
import chrisgreer.bookingsystem.services.EmailService;
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

import static chrisgreer.bookingsystem.model.BookingStatus.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

    @MockitoBean
    private EmailService emailService;

    @Test
    void createBooking_shouldReturnCreated() throws Exception {
        CreateAvailableSlotDto request = TestUtil.createValidBookingSlot();

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
        CreateAvailableSlotDto request = TestUtil.createValidBookingSlot();
        request.setStartTime(OffsetDateTime.now().plusDays(2));
        request.setEndTime(OffsetDateTime.now().plusDays(1));

        mockMvc.perform(post("/booking/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void requestBooking_returnsNotFound_whenBookingDoesNotExist() throws Exception {
        RequestBookingDto request = TestUtil.createValidBookingRequest();

        mockMvc.perform(patch("/booking/{id}/request", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void requestBooking_returnsConflict_whenBookingNotAvailable() throws Exception {
        Booking booking = TestUtil.createValidBooking();
        booking.setStatus(PENDING); //Not AVAILABLE
        bookingRepository.save(booking);

        RequestBookingDto request = TestUtil.createValidBookingRequest();
        mockMvc.perform(patch("/booking/request/{id}", booking.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void requestBooking_returnsOk_andSetsPending_whenValid() throws Exception {
        Booking booking = TestUtil.persistAvailableBooking(bookingRepository);

        RequestBookingDto request = TestUtil.createValidBookingRequest();
        mockMvc.perform(patch("/booking/request/{id}", booking.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());


        Booking updated = bookingRepository.findById(booking.getId()).orElseThrow();
        assertEquals(PENDING, updated.getStatus());
    }

    @Test
    void requestBooking_shouldReturnBadRequest_whenInvalidPhone() throws Exception {
        RequestBookingDto request = TestUtil.createValidBookingRequest();
        request.setPhone("invalid-phone");

        Booking booking = TestUtil.persistAvailableBooking(bookingRepository);

        mockMvc.perform(patch("/booking/request/{id}", booking.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateBookingStatus_shouldReturnNoContent_andSetsStatus() throws Exception {

        Booking booking = TestUtil.persistAvailableBooking(bookingRepository);

        UpdateBookingStatusDto statusDto = new UpdateBookingStatusDto();
        statusDto.setStatus(CONFIRMED);

        mockMvc.perform(patch("/booking/admin/{id}", booking.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusDto)))
                .andExpect(status().isNoContent());

        Booking updated = bookingRepository.findById(booking.getId()).orElseThrow();
        assertEquals(CONFIRMED, updated.getStatus());

    }

    @Test
    void updateBookingStatus_shouldReturnNotFound() throws Exception {
        UpdateBookingStatusDto statusDto = new UpdateBookingStatusDto();
        statusDto.setStatus(CONFIRMED);

        mockMvc.perform(patch("/booking/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateBookingStatus_shouldReturnBadRequest_whenStatusMissing() throws Exception {
        String json = "{}";

        mockMvc.perform(patch("/booking/admin/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAvailableBookings_shouldReturnList() throws Exception {

        bookingRepository.deleteAll();
        int totalBookings = 3;
        for(int i = 0 ; i < totalBookings ; i++){
            TestUtil.persistAvailableBooking(bookingRepository);
        }

        mockMvc.perform(get("/booking/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(totalBookings));
    }

    @Test
    void getBookings_shouldFallbackToId_whenInvalidSortField() throws Exception {
        bookingRepository.deleteAll();
        Booking firstBooking = TestUtil.persistAvailableBooking(bookingRepository);
        for(int i = 0 ; i < 2 ; i++){
            TestUtil.persistAvailableBooking(bookingRepository);
        }

        mockMvc.perform(get("/booking/admin?sort=invalid&showPast=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(firstBooking.getId()));
    }

    @Test
    void deleteBooking_shouldReturnNoContent_whenBookingExists() throws Exception {

        Booking booking = TestUtil.persistAvailableBooking(bookingRepository);

        mockMvc.perform(delete("/booking/admin/{id}", booking.getId()))
                .andExpect(status().isNoContent());

        assertNull(bookingRepository.findById(booking.getId()).orElse(null));
    }

    @Test
    void deleteBooking_shouldReturnNotFound_whenBookingMissing() throws Exception {
        mockMvc.perform(delete("/booking/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateBookingStatus_shouldSendUpdateEmail() throws Exception {
        Booking booking = TestUtil.persistAvailableBooking(bookingRepository);

        UpdateBookingStatusDto statusDto = new UpdateBookingStatusDto();
        statusDto.setStatus(CONFIRMED);

        mockMvc.perform(patch("/booking/admin/{id}",booking.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusDto)))
                .andExpect(status().isNoContent());

        Mockito.verify(emailService).sendBookingUpdated(booking);
    }

    @Test
    void requestBooking_shouldSendRequestedEmail() throws Exception {
        Booking booking = TestUtil.persistAvailableBooking(bookingRepository);

        RequestBookingDto request = TestUtil.createValidBookingRequest();
        mockMvc.perform(patch("/booking/request/{id}", booking.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        Mockito.verify(emailService).notifyBookingRequested(booking);
    }


}
