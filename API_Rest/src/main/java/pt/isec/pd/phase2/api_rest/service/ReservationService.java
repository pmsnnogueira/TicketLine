package pt.isec.pd.phase2.api_rest.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.isec.pd.phase2.api_rest.model.Reservation;
import pt.isec.pd.phase2.api_rest.repository.ReservationRepository;

import java.util.List;

@Service
public class ReservationService
{
    private final ReservationRepository reservationRepository;

    @Autowired
    public ReservationService(ReservationRepository reservationRepository) {this.reservationRepository = reservationRepository;}

    public List<Reservation> getPayedReservations(String id)
    {
        return reservationRepository.findPayedReservations(Integer.parseInt(id));
    }

    public List<Reservation> getAllReservations()
    {
        return reservationRepository.findAll();
    }
}
