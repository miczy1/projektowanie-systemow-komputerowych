package com.example.hotel.Controller;

import com.example.hotel.Model.Reservation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final List<Reservation> reservations = new ArrayList<>();
    private final AtomicInteger nextId = new AtomicInteger(1);

    @GetMapping
    public ResponseEntity<List<Reservation>> getReservations() {
        return new ResponseEntity<>(reservations, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getReservation(@PathVariable Integer id) {
        Optional<Reservation> reservation = reservations.stream()
                .filter(r -> r.getId().equals(id))
                .findFirst();
        if (reservation.isPresent()) {
            return new ResponseEntity<>(reservation.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>("{\"error\": \"Reservation not found\"}", HttpStatus.NOT_FOUND);
    }

    @PostMapping
    public ResponseEntity<?> createReservation(@RequestBody Reservation data) {
        if (data.getRoom() == null || data.getDate() == null ||
                data.getTime() == null || data.getReservedBy() == null) {
            return new ResponseEntity<>("{\"error\": \"Missing required fields\"}", HttpStatus.BAD_REQUEST);
        }

        boolean conflict = reservations.stream().anyMatch(r ->
                r.getRoom().equals(data.getRoom()) &&
                        r.getDate().equals(data.getDate()) &&
                        r.getTime().equals(data.getTime())
        );

        if (conflict) {
            return new ResponseEntity<>("{\"error\": \"Room already booked\"}", HttpStatus.CONFLICT);
        }

        data.setId(nextId.getAndIncrement());
        reservations.add(data);
        return new ResponseEntity<>(data, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateReservation(@PathVariable Integer id, @RequestBody Reservation data) {
        Optional<Reservation> existingOpt = reservations.stream()
                .filter(r -> r.getId().equals(id))
                .findFirst();

        if (existingOpt.isPresent()) {
            Reservation r = existingOpt.get();
            if(data.getRoom() != null) r.setRoom(data.getRoom());
            if(data.getDate() != null) r.setDate(data.getDate());
            if(data.getTime() != null) r.setTime(data.getTime());
            if(data.getReservedBy() != null) r.setReservedBy(data.getReservedBy());
            return new ResponseEntity<>(r, HttpStatus.OK);
        }
        return new ResponseEntity<>("{\"error\": \"Reservation not found\"}", HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReservation(@PathVariable Integer id) {
        boolean removed = reservations.removeIf(r -> r.getId().equals(id));
        if (removed) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>("{\"error\": \"Reservation not found\"}", HttpStatus.NOT_FOUND);
    }
}