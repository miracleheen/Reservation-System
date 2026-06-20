package sverdlov.dev.reservation_system.reservations.availability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sverdlov.dev.reservation_system.reservations.ReservationRepository;
import sverdlov.dev.reservation_system.reservations.ReservationStatus;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationAvailabilityService {
    private final ReservationRepository reservationRepository;
    private static final Logger log = LoggerFactory.getLogger(ReservationAvailabilityService.class);

    @Autowired
    public ReservationAvailabilityService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public boolean isReservationAvailable(
            Long roomId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (!endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("Start date must be 1 day earlier than end date");
        }

        List<Long> conflictWithIds = reservationRepository.findConflictReservationIds(
                roomId,
                startDate,
                endDate,
                ReservationStatus.APPROVED
        );

        if (conflictWithIds.isEmpty()) {
            return true;
        }

        log.info("Conflicting with ids={}", conflictWithIds);
        return false;
    }
}
