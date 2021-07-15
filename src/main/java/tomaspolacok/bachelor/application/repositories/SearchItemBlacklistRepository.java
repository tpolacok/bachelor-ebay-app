package tomaspolacok.bachelor.application.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tomaspolacok.bachelor.application.entities.EbayItem;
import tomaspolacok.bachelor.application.entities.Search;
import tomaspolacok.bachelor.application.entities.SearchItemBlacklist;

public interface SearchItemBlacklistRepository extends JpaRepository<  SearchItemBlacklist, Long> {
	
	/**
	 * Selects blacklist mapping between search and item
	 * @param search
	 * @param item
	 * @return
	 */
	public  SearchItemBlacklist findBySearchAndItem(Search search, EbayItem item);
	
	/**
	 * Selects page of blacklist mappings between search and items
	 * @param search
	 * @param pageable
	 * @return
	 */
	@Query("select sib from SearchItemBlacklist sib where sib.search = :search")
	public Page<SearchItemBlacklist> findBlacklistItemsBySearch(@Param("search") Search search, Pageable pageable);
	
}