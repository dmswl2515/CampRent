package com.rental.camprent.domain.campingrental;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RentalRepository extends JpaRepository<Rental, Long> {

    List<Rental> findByStatus(RentalStatus status);

    List<Rental> findByCustomerId(Long customerId);

    List<Rental> findByMachineId(Long machineId);

    List<Rental> findByStartDateBetween(LocalDate startDate, LocalDate endDate);

    List<Rental> findByEndDateBefore(LocalDate date);
}
