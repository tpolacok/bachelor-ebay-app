package tomaspolacok.bachelor.application.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tomaspolacok.bachelor.application.entities.EbaySeller;
import tomaspolacok.bachelor.application.entities.Search;
import tomaspolacok.bachelor.application.entities.SearchSellerBlacklist;

public interface SearchSellerBlacklistRepository extends JpaRepository< SearchSellerBlacklist, Long> {
	
	/**
	 * Selects blacklist mapping between search and seller
	 * @param search
	 * @param seller
	 * @return
	 */
	public SearchSellerBlacklist findBySearchAndSeller(Search search, EbaySeller seller);
	
	/**
	 * Selects page of blacklist mappings between search and sellers
	 * @param search
	 * @param pageable
	 * @return
	 */
	@Query("select ssb from SearchSellerBlacklist ssb where ssb.search = :search")
	public Page<SearchSellerBlacklist> findBlacklistItemsBySearch(@Param("search") Search search, Pageable pageable);
}
