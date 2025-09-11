package chrisgreer.bookingsystem;

import chrisgreer.bookingsystem.entities.Booking;
import chrisgreer.bookingsystem.repositories.BookingRepository;
import chrisgreer.bookingsystem.services.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    @MockitoBean
    private EmailService emailService;

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
    @Autowired
    private BookingRepository bookingRepository;

    @Test
    void getAllBookings_requiresNoAuth() throws Exception {
        // No auth leads to 200
        mockMvc.perform(get("/booking/public"))
                .andExpect(status().isOk());
    }

    @Test
    void createBooking_requiresAuth() throws Exception {
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
        //No Auth
        Booking booking = TestUtil.persistAvailableBooking(bookingRepository);
        mockMvc.perform(patch("/booking/request/{id}", booking.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST_BOOKING_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateBookingStatus_requiresAuth() throws Exception {

        Booking booking = TestUtil.persistAvailableBooking(bookingRepository);

        // Without auth
        mockMvc.perform(patch("/booking/admin/{id}",booking.getId())
                        .contentType("application/json")
                        .content(UPDATE_STATUS_JSON))
                .andExpect(status().isUnauthorized());

        // With auth
        mockMvc.perform(patch("/booking/admin/{id}",booking.getId())
                        .with(httpBasic(adminUsername, adminPassword))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(UPDATE_STATUS_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteBooking_requiresAuth() throws Exception {
        Booking booking = TestUtil.persistAvailableBooking(bookingRepository);
        bookingRepository.save(booking);

        // No Auth leads to 401
        mockMvc.perform(delete("/booking/admin/{id}", booking.getId()))
                .andExpect(status().isUnauthorized());

        // Basic Auth leads to 204
        mockMvc.perform(delete("/booking/admin/{id}", booking.getId())
                        .with(httpBasic(adminUsername, adminPassword)))
                .andExpect(status().isNoContent());

        //Confirm actually deleted
        assertNull(bookingRepository.findById(booking.getId()).orElse(null));



    }
}
