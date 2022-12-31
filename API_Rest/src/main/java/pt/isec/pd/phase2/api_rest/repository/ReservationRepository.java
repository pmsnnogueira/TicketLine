package pt.isec.pd.phase2.api_rest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.isec.pd.phase2.api_rest.model.Reservation;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Integer>
{
    @Query("SELECT r FROM Reservation r WHERE r.user.id = :id AND r.payed = 1")
    List<Reservation> findPayedReservations(@Param("id") Integer id);

    @Query("SELECT r FROM Reservation r WHERE r.user.id = :id AND r.payed = 0")
    List<Reservation> findNonPayedReservations(@Param("id") Integer id);
}
