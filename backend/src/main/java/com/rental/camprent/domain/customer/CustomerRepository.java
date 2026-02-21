package com.rental.camprent.domain.customer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findByType(CustomerType type);

    List<Customer> findByNameContaining(String name);

    boolean existsByPhone(String phobne);

    Optional<Customer> findByEmail(String email);

    Optional<Customer> findByPhone(String phone);

}
