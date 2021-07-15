package tomaspolacok.bachelor.application.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import tomaspolacok.bachelor.application.entities.SearchPreferences;

public interface SearchPreferencesRepository extends JpaRepository<SearchPreferences, Long> {
}
