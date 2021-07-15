package tomaspolacok.bachelor.application.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import tomaspolacok.bachelor.application.entities.Country;


public interface CountryRepository extends JpaRepository<Country, String> {}