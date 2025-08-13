package com.example.bookingsystem;

import com.example.bookingsystem.dtos.BookingDto;
import com.example.bookingsystem.entities.Booking;
import com.example.bookingsystem.mappers.BookingMapper;
import com.example.bookingsystem.model.BookingStatus;
import com.example.bookingsystem.repositories.BookingRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static com.example.bookingsystem.model.BookingStatus.*;

import java.util.Optional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingRepository bookingRepository;

    @MockitoBean
    private BookingMapper bookingMapper;

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    private static final String VALID_BOOKING_JSON = """
        {
          "name": "Chris",
          "email": "chris@example.com",
          "phone": "07977904132",
          "startTime": "12-09-2025 10:00",
          "endTime": "12-09-2025 11:00",
          "topic": "Consultation",
          "notes": "Please call on time"
        }
        """;

    private static final String UPDATE_STATUS_JSON = """
        {"status": "CONFIRMED"}""";

    @Test
    void getAllBookings_requiresAuth() throws Exception {
        // No auth leads to 401
        mockMvc.perform(get("/booking"))
                .andExpect(status().isUnauthorized());

        // Basic Auth leads to 200
        mockMvc.perform(get("/booking")
                        .with(httpBasic(adminUsername, adminPassword)))
                .andExpect(status().isOk());
    }

    @Test
    void createBooking_isPublic() throws Exception {
        Booking bookingEntity = new Booking();
        bookingEntity.setId(1L);

        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(1L);

        Mockito.when(bookingMapper.toEntity(Mockito.any()))
                .thenReturn(bookingEntity);
        Mockito.when(bookingMapper.toDto(Mockito.any()))
                .thenReturn(bookingDto);
        Mockito.when(bookingRepository.save(Mockito.any()))
                .thenReturn(bookingEntity);

        //No auth required (currently), should accept
        mockMvc.perform(post("/booking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BOOKING_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void updateBookingStatus_requiresAuth() throws Exception {

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStatus(PENDING);
        Mockito.when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        // Without auth
        mockMvc.perform(patch("/booking/1")
                        .contentType("application/json")
                        .content(UPDATE_STATUS_JSON))
                .andExpect(status().isUnauthorized());

        // With auth
        mockMvc.perform(patch("/booking/1")
                        .with(httpBasic(adminUsername, adminPassword))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(UPDATE_STATUS_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteBooking_requiresAuth() throws Exception {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStatus(PENDING);
        Mockito.when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        // No Auth leads to 401
        mockMvc.perform(delete("/booking/1"))
                .andExpect(status().isUnauthorized());

        // Basic Auth leads to 204
        mockMvc.perform(delete("/booking/1")
                        .with(httpBasic(adminUsername, adminPassword)))
                .andExpect(status().isNoContent());

        // Verify delete was actually called too (for auth)
        Mockito.verify(bookingRepository, Mockito.times(1)).delete(booking);
    }
}
