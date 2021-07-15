package tomaspolacok.bachelor.application.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import tomaspolacok.bachelor.application.entities.SellingStatus;

public interface SellingStatusRepository extends JpaRepository<SellingStatus, Long> {
}