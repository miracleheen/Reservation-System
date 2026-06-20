package sverdlov.dev.reservation_system.reservations;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sverdlov.dev.reservation_system.reservations.availability.ReservationAvailabilityService;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class ReservationService {
    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);
    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;
    private final ReservationAvailabilityService reservationAvailabilityService;

    @Autowired
    public ReservationService(ReservationRepository reservationRepository,
                              ReservationMapper reservationMapper, ReservationAvailabilityService reservationAvailabilityService) {
        this.reservationRepository = reservationRepository;
        this.reservationMapper = reservationMapper;
        this.reservationAvailabilityService = reservationAvailabilityService;
    }

    public Reservation getReservationById(Long id) {
        ReservationEntity reservationEntity = reservationRepository
                .findById(id).orElseThrow(() -> new EntityNotFoundException("Reservation not found by id: " + id));

        return reservationMapper.toDomain(reservationEntity);
    }

    public List<Reservation> searchAllByFilter(ReservationSearchFilter filter) {
        int pageSize = filter.pageSize() != null
                ? filter.pageSize() : 10;
        int pageNumber = filter.pageNumber() != null
                ? filter.pageNumber() : 0;

        var pageable = Pageable
                .ofSize(pageSize)
                .withPage(pageNumber);

        List<ReservationEntity> allEntities = reservationRepository.searchAllByFilter(
                filter.userId(),
                filter.roomId(),
                pageable
        );

        return allEntities.stream().map(reservationMapper::toDomain).toList();
    }

    @Transactional
    public Reservation createReservation(Reservation reservationToCreate) {
        if (reservationToCreate.status() != null) {
            throw new IllegalArgumentException("Status should be empty");
        }

        if (!reservationToCreate.endDate().isAfter(reservationToCreate.startDate())) { //валидация endDate (должен быть после/позже startDate)
            throw new IllegalArgumentException("Start date must be 1 day earlier than end date");
        }

        var entityToSave = reservationMapper.toEntity(reservationToCreate);
        entityToSave.setStatus(ReservationStatus.PENDING);

        var savedEntity = reservationRepository.save(entityToSave);
        return reservationMapper.toDomain(savedEntity);
    }

    @Transactional
    public void cancelReservation(Long id) {
        var reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found by id: " + id));

        if (reservation.getStatus().equals(ReservationStatus.APPROVED)) {
            throw new IllegalStateException("Cannot cancel approved reservation. Please, contact with admin");
        }

        if (reservation.getStatus().equals(ReservationStatus.CANCELLED)) {
            throw new IllegalStateException("Cannot cancel cancelled reservation. Reservation was already cancelled");
        }

        reservationRepository.setStatus(id, ReservationStatus.CANCELLED);
        log.info("Reservation cancelled successfully: id={}", id);
    }

    @Transactional
    public Reservation updateReservation(Long id, Reservation reservationToUpdate) {
        var reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found by id: " + id));

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Cannot modify reservation: status= " + reservation.getStatus());
        }

        if (!reservationToUpdate.endDate().isAfter(reservationToUpdate.startDate())) {
            throw new IllegalArgumentException("Start date must be 1 day earlier than end date");
        }

        var reservationToSave = reservationMapper.toEntity(reservationToUpdate);
        reservationToSave.setId(reservation.getId());
        reservationToSave.setStatus(ReservationStatus.PENDING);

        var updatedReservation = reservationRepository.save(reservationToSave);
        return reservationMapper.toDomain(updatedReservation);
    }


    @Transactional
    public Reservation approveReservation(Long id) {
        var reservationEntity = reservationRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found by id: " + id));

        if (reservationEntity.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Cannot approve reservation: status= " + reservationEntity.getStatus());
        }

        var isAvailableToApprove = reservationAvailabilityService.isReservationAvailable(
                reservationEntity.getRoomId(),
                reservationEntity.getStartDate(),
                reservationEntity.getEndDate()
        );

        if (!isAvailableToApprove) {
            throw new IllegalStateException("Cannot approve reservation, because of conflict");
        }

        reservationEntity.setStatus(ReservationStatus.APPROVED);
        reservationRepository.save(reservationEntity);

        return reservationMapper.toDomain(reservationEntity);
    }
}
