package com.example.bookingsystem;

import com.example.bookingsystem.controllers.BookingController;
import com.example.bookingsystem.dtos.BookingDto;
import com.example.bookingsystem.dtos.CreateAvailableSlotDto;
import com.example.bookingsystem.dtos.RequestBookingDto;
import com.example.bookingsystem.dtos.UpdateBookingStatusDto;
import com.example.bookingsystem.entities.Booking;
import com.example.bookingsystem.mappers.BookingMapper;
import com.example.bookingsystem.repositories.BookingRepository;
import com.example.bookingsystem.services.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static com.example.bookingsystem.model.BookingStatus.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
@AutoConfigureMockMvc(addFilters = false)
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookingRepository bookingRepository;

    @MockitoBean
    private BookingMapper bookingMapper;

    @MockitoBean
    private EmailService emailService;

    @Test
    void createBooking_shouldReturnCreated() throws Exception {
        CreateAvailableSlotDto request = createValidBookingSlot();

        Booking entity = new Booking();
        entity.setId(1L);

        BookingDto responseDto = new BookingDto();
        responseDto.setId(1L);

        Mockito.when(bookingMapper.availableDtoToEntity(any())).thenReturn(entity);
        Mockito.when(bookingMapper.toDto(any())).thenReturn(responseDto);

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
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStatus(PENDING); //Not AVAILABLE
        bookingRepository.save(booking);

        Mockito.when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        RequestBookingDto request = createValidBookingRequest();
        mockMvc.perform(patch("/booking/request/{id}", booking.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void requestBooking_returnsOk_andSetsPending_whenValid() throws Exception {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStatus(AVAILABLE);
        bookingRepository.save(booking);

        Mockito.when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        RequestBookingDto request = createValidBookingRequest();
        mockMvc.perform(patch("/booking/request/{id}", booking.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());


        Booking updated = bookingRepository.findById(booking.getId()).orElseThrow();
        assertEquals(PENDING, updated.getStatus());
    }



    @Test
    void requestBooking_shouldReturnBadRequest_whenInvalidPhone() throws Exception {
        RequestBookingDto request = new RequestBookingDto();
        request.setPhone("invalid-phone");
        request.setName("Valid Name");

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStatus(PENDING);

        Mockito.when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        mockMvc.perform(patch("/booking/request/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void updateBookingStatus_shouldReturnNoContent() throws Exception {
        UpdateBookingStatusDto statusDto = new UpdateBookingStatusDto();
        statusDto.setStatus(CONFIRMED);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStatus(PENDING);

        Mockito.when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        mockMvc.perform(patch("/booking/admin/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusDto)))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateBookingStatus_shouldReturnNotFound() throws Exception {
        UpdateBookingStatusDto statusDto = new UpdateBookingStatusDto();
        statusDto.setStatus(CONFIRMED);

        Mockito.when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

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
        Booking booking = new Booking();
        BookingDto dto = new BookingDto(); dto.setId(1L);


        Mockito.when(bookingRepository.findFutureFilterStatus(
                eq(false),
                eq(AVAILABLE.name())
        )).thenReturn(List.of(booking));

        Mockito.when(bookingMapper.toDto(booking)).thenReturn(dto);

        mockMvc.perform(get("/booking/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getAllBookings_shouldFallbackToId_whenInvalidSortField() throws Exception {
        Booking booking = new Booking();
        BookingDto dto = new BookingDto(); dto.setId(1L);

        Mockito.when(bookingRepository.findAllByDeleted(eq(false), eq(Sort.by("id"))))
                .thenReturn(List.of(booking));
        Mockito.when(bookingMapper.toDto(booking)).thenReturn(dto);

        mockMvc.perform(get("/booking/admin?sort=invalid&showPast=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void deleteBooking_shouldReturnNoContent_whenBookingExists() throws Exception {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStatus(PENDING);

        Mockito.when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        mockMvc.perform(delete("/booking/admin/1"))
                .andExpect(status().isNoContent());

        Mockito.verify(bookingRepository, Mockito.times(1)).delete(booking);
    }

    @Test
    void deleteBooking_shouldReturnNotFound_whenBookingMissing() throws Exception {
        Mockito.when(bookingRepository.findById(1L))
                .thenReturn(Optional.empty());

        mockMvc.perform(delete("/booking/1"))
                .andExpect(status().isNotFound());

        Mockito.verify(bookingRepository, Mockito.never()).delete(Mockito.any());
    }


    /// --- Email Service Tests ---

    @Test
    void updateBookingStatus_shouldSendUpdateEmail() throws Exception {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStatus(PENDING);

        Mockito.when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        UpdateBookingStatusDto statusDto = new UpdateBookingStatusDto();
        statusDto.setStatus(CONFIRMED);

        mockMvc.perform(patch("/booking/admin/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusDto)))
                .andExpect(status().isNoContent());

        Mockito.verify(emailService).sendBookingUpdated(booking);
    }

    @Test
    void requestBooking_shouldSendRequestedEmail() throws Exception {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStatus(AVAILABLE);

        Mockito.when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        RequestBookingDto request = createValidBookingRequest();

        mockMvc.perform(patch("/booking/request/{id}", booking.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());


        Mockito.verify(emailService).notifyBookingRequested(booking);
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
