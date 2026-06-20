package sverdlov.dev.reservation_system.reservations.availability;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reservation/availability")
public class ReservationAvailabilityController {
    private final ReservationAvailabilityService reservationAvailabilityService;
    private static final Logger log = LoggerFactory.getLogger(ReservationAvailabilityController.class);

    @Autowired
    public ReservationAvailabilityController(ReservationAvailabilityService reservationAvailabilityService) {
        this.reservationAvailabilityService = reservationAvailabilityService;
    }

    @PostMapping("/check")
    public ResponseEntity<CheckAvailabilityResponse> checkAvailability(@Valid CheckAvailabilityRequest request) {
        log.info("Check availability: request -> {}", request);
        boolean isAvailable = reservationAvailabilityService.isReservationAvailable(
                request.roomId(),
                request.startDate(),
                request.endDate()
        );

        var message = isAvailable
                ? "Room available to reservation"
                : "Room not available to reservation";

        var status = isAvailable
                ? AvailabilityStatus.AVAILABLE
                : AvailabilityStatus.RESERVED;

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new CheckAvailabilityResponse(message,status));
    }
}
