package tomaspolacok.bachelor.application.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import tomaspolacok.bachelor.application.entities.Search;
import tomaspolacok.bachelor.application.entities.User;


public interface SearchRepository extends JpaRepository<Search, Long> {
	
	/**
	 * Checks whether there are disabled searches
	 * @return
	 */
	@Query("select count(s) > 0 from Search s where s.enabled = false and s.removed = false and s.active = true")
	Boolean checkDisabled();
	
	/**
	 * Checks whether search exists
	 * @param id
	 * @return
	 */
	@Query("select count(s) > 0 from Search s where s.id = :id")
	Boolean checkExists(Long id);
	
	/**
	 * Selects page of searches which have not been deleted and belong to user
	 * @param user
	 * @param pageable
	 * @return
	 */
	@Query("select s from Search s join s.user u where u = :user and  s.removed = false")
	Page<Search> findAllByUser(User user, Pageable pageable);
	
	/**
	 * Selects search with specific id belonging to user
	 * @param user
	 * @param id
	 * @return
	 */
	@Query("SELECT s FROM Search s WHERE s.user = :user AND s.id = :id")
	Search findByUserAndId(@Param("user") User user, @Param("id") Long id);
	
	List<Search> findByUser(User user);
	
	/**
	 * Deletes item from search
	 * @param itemId
	 * @param searchId
	 */
	@Transactional
	@Query(value="DELETE FROM search_items si WHERE si.ebayitem_id = :item AND si.search_id = :search"
			,nativeQuery=true)
	@Modifying
	void deleteItemFromSearch(@Param("item") Long itemId, @Param("search") Long searchId);
	
	/**
	 * Deletes item from all searches of user
	 * @param itemId
	 * @param userId
	 */
	@Transactional
	@Query(value="DELETE FROM search_items si WHERE si.ebayitem_id = :item AND"
			+ " si.search_id IN (select s.search_id FROM search s INNER JOIN app_user u ON s.user_user_id = u.user_id WHERE u.user_id = :user)"
			,nativeQuery=true)
	@Modifying
	void deleteItemFromSearchAll(@Param("item") Long itemId, @Param("user") Long userId);
	
	/**
	 * Deletes item from search by seller
	 * @param seller
	 * @param searchId
	 */
	@Transactional
	@Query(value="DELETE FROM search_items si WHERE si.search_id = :search AND "
			+ "si.ebayitem_id IN (SELECT i.ebayitem_id from ebay_item i WHERE i.seller_name = :seller) AND "
			+ "si.search_id IN (SELECT s.search_id from search s WHERE s.seller_search = false)"
			,nativeQuery=true)
	@Modifying
	void deleteItemFromSearchBySeller(@Param("seller") String seller, @Param("search") Long searchId);
	
	/**
	 * Deletes item from all searches of user
	 * @param seller
	 * @param userId
	 */
	@Transactional
	@Query(value="DELETE FROM search_items si WHERE "
			+ "si.ebayitem_id IN (SELECT i.ebayitem_id from ebay_item i WHERE i.seller_name = :seller) AND "
			+ "si.search_id IN (SELECT s.search_id FROM search s INNER JOIN app_user u ON s.user_user_id = u.user_id WHERE u.user_id = :user AND s.seller_search = false)"
			,nativeQuery=true)
	@Modifying
	void deleteItemFromSearchBySellerAll(@Param("seller") String seller, @Param("user") Long userId);
	
	/**
	 * Selects all searches which are active, enabled and not removed
	 * @return
	 */
//	@Query(value="select s from Search s join fetch s.listingTypes lt left join fetch s.keywordsExclude ke left join fetch s.keywordsInclude ki"
//			+ " left join fetch s.sellers ss left join fetch s.blacklistSellers sbs left join fetch s.blacklistItems sbi left join fetch s.shippingTo sst"
//			+ " join fetch s.user u left join fetch u.blacklistItems subi left join fetch u.blacklistSellers subs left join fetch u.notifiedItems suni "
//			+ "where s.active = true and s.enabled = true and s.removed = false")
//	List<Search> findAllToRefresh();
	
	/**
	 * Selects all searches which are active, enabled and not removed
	 * @return
	 */
	@Query(value="select s from Search s where s.active = true and s.enabled = true and s.removed = false")
	List<Search> findAllToRefresh();
	
	@Query(value="select s from Search s join fetch s.listingTypes lt left join fetch s.keywordsQueries ki"
			+ " left join fetch s.sellers ss left join fetch s.blacklistSellers sbs left join fetch s.blacklistItems sbi left join fetch s.shippingTo sst"
			+ " join fetch s.user u left join fetch u.blacklistItems subi left join fetch u.blacklistSellers subs left join fetch u.notifiedItems suni"
			+ " where s.id = :searchId")
	Search getSearchToRefresh(@Param("searchId") Long searchId);
	
}