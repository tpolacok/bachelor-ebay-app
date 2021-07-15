package tomaspolacok.bachelor.application.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import tomaspolacok.bachelor.application.entities.EbayCountry;


public interface EbayCountryRepository extends JpaRepository<EbayCountry, Integer> {
}