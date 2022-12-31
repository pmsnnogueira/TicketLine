package pt.isec.pd.phase2.api_rest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.isec.pd.phase2.api_rest.model.Reservation;
import pt.isec.pd.phase2.api_rest.model.User;
import pt.isec.pd.phase2.api_rest.service.ReservationService;
import pt.isec.pd.phase2.api_rest.service.UserService;
import java.util.List;

@RestController
@RequestMapping("reservation")
public class ReservationController
{
    private final ReservationService reservationService;
    private final UserService userService;

    @Autowired
    public ReservationController(ReservationService reservationService, UserService userService)
    {
        this.reservationService = reservationService;
        this.userService = userService;
    }

    @GetMapping("/payed")
    public ResponseEntity<List<Reservation>> getPayedReservations()
    {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByName(auth.getName());
        return ResponseEntity.ok().body(reservationService.getPayedReservations(user.getId().toString()));
    }

    @GetMapping("/nonpayed")
    public ResponseEntity<List<Reservation>> getNonPayedReservations()
    {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByName(auth.getName());
        return ResponseEntity.ok().body(reservationService.getNonPayedReservations(user.getId().toString()));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Reservation>> getAllReservations()
    {
        return ResponseEntity.ok().body(reservationService.getAllReservations());
    }
}
