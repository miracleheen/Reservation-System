package sverdlov.dev.reservation_system.reservations;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {
    @Query("select r from ReservationEntity as r where r.status = :status")
    List<ReservationEntity> findAllByStatusIs(ReservationStatus status);

    @Query("select r from ReservationEntity as r where r.roomId = :roomId")
    List<ReservationEntity> findAllByRoomId(@Param("roomId") Long roomId);


    @Transactional
    @Modifying
    @Query("""
              update ReservationEntity as r 
              set r.userId=:userId, 
              r.roomId=:roomId, 
              r.startDate=:startDate, 
              r.endDate=:endDate,
              r.status=:status 
              where r.id=:id
            """)
    int updateAllFields(
            @Param("id") Long id,
            @Param("userId") Long userId,
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") ReservationStatus status
    );

    @Modifying
    @Query("""
            update ReservationEntity as r 
            set r.status = :status 
            where r.id=:id
            """)
    void setStatus(
            @Param("id") Long id,
            @Param("status") ReservationStatus status
    );


    @Query("""
               select r.id from ReservationEntity as r
               where r.roomId = :roomId 
               and r.endDate > :startDate
               and r.startDate < :endDate
               and r.status = :status
            """)
    List<Long> findConflictReservationIds(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") ReservationStatus status
    );


    @Query("""
               select r from ReservationEntity as r
               where (:roomId IS NULL OR r.roomId = :roomId) 
               and (:userId IS NULL OR r.userId = :userId)
            """)
    List<ReservationEntity> searchAllByFilter(
            @Param("userId") Long userId,
            @Param("roomId") Long roomId,
            Pageable pageable
    );
}
