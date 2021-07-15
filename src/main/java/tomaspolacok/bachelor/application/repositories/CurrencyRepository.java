package tomaspolacok.bachelor.application.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import tomaspolacok.bachelor.application.entities.Currency;

public interface CurrencyRepository extends JpaRepository<Currency, String> {

}