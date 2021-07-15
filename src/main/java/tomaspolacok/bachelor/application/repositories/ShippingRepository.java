package tomaspolacok.bachelor.application.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import tomaspolacok.bachelor.application.entities.Shipping;

public interface ShippingRepository extends JpaRepository<Shipping, Integer> {
}