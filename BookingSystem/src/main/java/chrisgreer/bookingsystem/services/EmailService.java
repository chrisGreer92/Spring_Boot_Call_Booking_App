package chrisgreer.bookingsystem.services;

import chrisgreer.bookingsystem.entities.Booking;
import chrisgreer.bookingsystem.utils.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String adminEmail;

    /// For when someone has requested a booking, notify me
    public void notifyBookingRequested(Booking booking) {
        String subject = "Booking requested: " + DateTimeUtil.format(booking.getStartTime());
        String body = buildBookingDetailsBody(booking);
        //Send to both (for now)
        sendEmail(adminEmail, subject, body);
        sendEmail(booking.getEmail(), subject, body);
    }

    /// For when I've changed a booking, notify the requester
    public void sendBookingUpdated(Booking booking){
        if(booking.getEmail() == null) return;
        String subject = "Booking Updated: " + booking.getStatus();
        String body = buildBookingDetailsBody(booking);
        sendEmail(booking.getEmail(), subject, body);
    }

    private void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    private String buildBookingDetailsBody(Booking booking) {
        return """
            Name: %s
            Email: %s
            Phone: %s
            Topic: %s
            Notes: %s
            Start: %s
            End: %s
            Status: %s
            """
                .formatted(
                        booking.getName(),
                        booking.getEmail(),
                        booking.getPhone(),
                        booking.getTopic() != null ? booking.getTopic() : "(none)",
                        booking.getNotes() != null ? booking.getNotes() : "(none)",
                        DateTimeUtil.format(booking.getStartTime()),
                        DateTimeUtil.format(booking.getEndTime()),
                        booking.getStatus()
                );
    }

}
