package com.example.bookingsystem;

import com.example.bookingsystem.controllers.BookingController;
import com.example.bookingsystem.dtos.BookingDto;
import com.example.bookingsystem.dtos.GenerateBookingDto;
import com.example.bookingsystem.dtos.UpdateBookingStatusDto;
import com.example.bookingsystem.entities.Booking;
import com.example.bookingsystem.mappers.BookingMapper;
import static com.example.bookingsystem.model.BookingStatus.*;
import com.example.bookingsystem.repositories.BookingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingRepository bookingRepository;

    @MockBean
    private BookingMapper bookingMapper;

    @Test
    void createBooking_shouldReturnCreated() throws Exception {
        GenerateBookingDto request = createValidBookingRequest();

        Booking entity = new Booking();
        entity.setId(1L);

        BookingDto responseDto = new BookingDto();
        responseDto.setId(1L);

        Mockito.when(bookingMapper.toEntity(any())).thenReturn(entity);
        Mockito.when(bookingMapper.toDto(any())).thenReturn(responseDto);

        mockMvc.perform(post("/booking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createBooking_shouldReturnBadRequest_whenMissingFields() throws Exception {
        GenerateBookingDto request = new GenerateBookingDto();
        request.setEmail("chris@example.com");
        request.setStartTime(LocalDateTime.now().plusDays(1));
        request.setEndTime(LocalDateTime.now().plusDays(1).plusHours(1));

        mockMvc.perform(post("/booking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_shouldReturnBadRequest_whenInvalidPhone() throws Exception {
        GenerateBookingDto request = createValidBookingRequest();
        request.setPhone("invalid-phone");

        mockMvc.perform(post("/booking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_shouldReturnBadRequest_whenStartTimeAfterEndTime() throws Exception {
        GenerateBookingDto request = createValidBookingRequest();
        request.setStartTime(LocalDateTime.now().plusDays(2));
        request.setEndTime(LocalDateTime.now().plusDays(1));

        mockMvc.perform(post("/booking")
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

        Mockito.when(bookingRepository.findById(eq(1L))).thenReturn(Optional.of(booking));

        mockMvc.perform(patch("/booking/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusDto)))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateBookingStatus_shouldReturnNotFound() throws Exception {
        UpdateBookingStatusDto statusDto = new UpdateBookingStatusDto();
        statusDto.setStatus(CONFIRMED);

        Mockito.when(bookingRepository
                        .findById(eq(999L))
                    ).thenReturn(Optional.empty());

        mockMvc.perform(patch("/booking/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateBookingStatus_shouldReturnBadRequest_whenStatusMissing() throws Exception {
        String json = "{}";

        mockMvc.perform(patch("/booking/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllBookings_shouldReturnList() throws Exception {
        Booking booking = new Booking();
        BookingDto dto = new BookingDto();
        dto.setId(1L);

        Mockito.when(bookingRepository
                        .findAll(any(Sort.class)))
                        .thenReturn(List.of(booking)
                    );
        Mockito.when(bookingMapper.toDto(booking))
                    .thenReturn(dto);

        mockMvc.perform(get("/booking?sort=id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getAllBookings_shouldFallbackToId_whenInvalidSortField() throws Exception {
        Booking booking = new Booking();
        BookingDto dto = new BookingDto();
        dto.setId(1L);

        Mockito.when(bookingRepository
                        .findAll(any(org.springframework.data.domain.Sort.class))
                    ).thenReturn(List.of(booking));
        Mockito.when(bookingMapper.toDto(booking)).thenReturn(dto);

        mockMvc.perform(get("/booking?sort=invalid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    private GenerateBookingDto createValidBookingRequest() {
        GenerateBookingDto request = new GenerateBookingDto();
        request.setName("Chris");
        request.setEmail("chris@example.com");
        request.setPhone("07977904132");
        request.setStartTime(LocalDateTime.now().plusDays(1));
        request.setEndTime(LocalDateTime.now().plusDays(1).plusHours(1));
        request.setTopic("Consultation");
        request.setNotes("Please call on time");
        return request;
    }

}
