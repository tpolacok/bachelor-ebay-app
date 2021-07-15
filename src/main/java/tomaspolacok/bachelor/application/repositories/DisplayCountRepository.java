package tomaspolacok.bachelor.application.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import tomaspolacok.bachelor.application.entities.DisplayCount;

public interface DisplayCountRepository extends JpaRepository<DisplayCount, Integer> {
}