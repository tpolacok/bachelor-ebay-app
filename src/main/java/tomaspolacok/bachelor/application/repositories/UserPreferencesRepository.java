package tomaspolacok.bachelor.application.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import tomaspolacok.bachelor.application.entities.UserPreferences;

public interface UserPreferencesRepository extends JpaRepository<UserPreferences, Long> {
}

