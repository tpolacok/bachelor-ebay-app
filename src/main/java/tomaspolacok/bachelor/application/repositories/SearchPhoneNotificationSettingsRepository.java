package tomaspolacok.bachelor.application.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import tomaspolacok.bachelor.application.entities.SearchPhoneNotificationSettings;

public interface SearchPhoneNotificationSettingsRepository extends JpaRepository<SearchPhoneNotificationSettings, Long> {
}