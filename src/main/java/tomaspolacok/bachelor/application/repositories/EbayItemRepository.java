package tomaspolacok.bachelor.application.repositories;

import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import tomaspolacok.bachelor.application.entities.EbayItem;
import tomaspolacok.bachelor.application.entities.Search;
import tomaspolacok.bachelor.application.enums.ListingType;


public interface EbayItemRepository extends JpaRepository<EbayItem, Long> {
	
	/**
	 * Select all items belonging to specific search
	 * @param search
	 * @param pageable
	 * @return
	 */
	@Query("select i from EbayItem i join i.searches s where s = :search and i.listingType in :listingTypes")
	Page<EbayItem> findAllBySearch(Search search, Pageable pageable, Set<ListingType> listingTypes);
	
}