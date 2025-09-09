package chrisgreer.bookingsystem;

import chrisgreer.bookingsystem.dtos.BookingDto;
import chrisgreer.bookingsystem.entities.Booking;
import chrisgreer.bookingsystem.mappers.BookingMapper;
import chrisgreer.bookingsystem.repositories.BookingRepository;
import chrisgreer.bookingsystem.services.BookingService;
import chrisgreer.bookingsystem.services.EmailService;
import chrisgreer.bookingsystem.web.ResponseMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.Optional;

import static chrisgreer.bookingsystem.model.BookingStatus.PENDING;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private BookingService bookingService;

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    private static final String VALID_BOOKING_JSON = """
         {
          "startTime": "%s",
          "endTime": "%s"
        }
        """.formatted(
                    OffsetDateTime.now().plusDays(1).toString(),
                    OffsetDateTime.now().plusDays(1).plusHours(1).toString()
            );

    private static final String REQUEST_BOOKING_JSON = """
        {
          "name": "Chris",
          "email": "chris@example.com",
          "phone": "07977904132",
          "topic": "Consultation",
          "notes": "Please call"
        }
        """;



    private static final String UPDATE_STATUS_JSON = """
        {"status": "CONFIRMED"}""";

    @Test
    void getAllBookings_requiresNoAuth() throws Exception {
        // No auth leads to 200
        mockMvc.perform(get("/booking/public"))
                .andExpect(status().isOk());
    }

    @Test
    void createBooking_requiresAuth() throws Exception {
        Booking bookingEntity = new Booking();
        bookingEntity.setId(1L);

        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(1L);

        Mockito.when(bookingMapper.availableDtoToEntity(Mockito.any()))
                .thenReturn(bookingEntity);
        Mockito.when(bookingMapper.toDto(Mockito.any()))
                .thenReturn(bookingDto);
        Mockito.when(bookingRepository.save(Mockito.any()))
                .thenReturn(bookingEntity);

        // Without auth
        mockMvc.perform(post("/booking/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BOOKING_JSON))
                .andExpect(status().isUnauthorized());

        // With auth
        mockMvc.perform(post("/booking/admin")
                        .with(httpBasic(adminUsername, adminPassword))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BOOKING_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void requestBooking_isPublic() throws Exception {
        mockMvc.perform(patch("/booking/request/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST_BOOKING_JSON))
                .andExpect(status().isNotFound()); //Not found means still auth (didn't create and add)
    }

    @Test
    void updateBookingStatus_requiresAuth() throws Exception {

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStatus(PENDING);
        Mockito.when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        // Without auth
        mockMvc.perform(patch("/booking/admin/1")
                        .contentType("application/json")
                        .content(UPDATE_STATUS_JSON))
                .andExpect(status().isUnauthorized());

        // With auth
        mockMvc.perform(patch("/booking/admin/1")
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
        mockMvc.perform(delete("/booking/admin/1"))
                .andExpect(status().isUnauthorized());

        // Basic Auth leads to 204
        mockMvc.perform(delete("/booking/admin/1")
                        .with(httpBasic(adminUsername, adminPassword)))
                .andExpect(status().isNoContent());

        // Verify delete was actually called too (for auth)
        Mockito.verify(bookingRepository, Mockito.times(1)).delete(booking);
    }
}
