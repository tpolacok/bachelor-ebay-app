package tomaspolacok.bachelor.application.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import tomaspolacok.bachelor.application.entities.EbaySeller;


public interface EbaySellerRepository extends JpaRepository<EbaySeller, String> {
}