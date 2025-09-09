package chrisgreer.bookingsystem.dtos;

import chrisgreer.bookingsystem.model.BookingStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateBookingStatusDto {

    @NotNull
    private BookingStatus status;

}
