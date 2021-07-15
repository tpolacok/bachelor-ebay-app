package tomaspolacok.bachelor.application.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import tomaspolacok.bachelor.application.entities.Category;
import tomaspolacok.bachelor.application.entities.CategoryId;
import tomaspolacok.bachelor.application.entities.EbayCountry;


public interface CategoryRepository extends JpaRepository<Category, CategoryId> {
	
	/**
	 * Selects all categories from specific ebay country
	 * @param ebayCountry
	 * @return
	 */
	@Query(value="select * from Category c where c.EBAY_COUNTRY_EBAY_SITE_ID = :country",
			nativeQuery=true)
	List<Category> findByEbayCountry(@Param("country") Integer ebayCountry);
	/**
	 * Changes category's active state
	 * @param active
	 * @return
	 */
	@Modifying
	@Query("update Category c set c.active = :active where c.id.ebayCountry = :country")
	@Transactional
	int updateCategoriesByCountrySetActive(Boolean active, EbayCountry country);
	
	/**
	 * Deletes categories with state
	 * @param active
	 * @return
	 */
	@Modifying
	@Query("delete Category c where c.active = :active")
	@Transactional
	int deleteCategoriesWhereActive(Boolean active);
	
	/**
	 * Maps categories of searches belonging to specific ebay country
	 * @param from
	 * @param to
	 * @param ebayCountry
	 * @return
	 */
	@Modifying
	@Query(value = "UPDATE search_categories SET categories_ebay_category_id = :idTo "
			+ "WHERE categories_ebay_category_id = :idFrom "
			+ "AND categories_ebay_country_ebay_site_id = :country "
			+ "AND :idTo NOT IN (SELECT scc.categories_ebay_category_id FROM search_categories scc WHERE scc.categories_ebay_country_ebay_site_id = :country)",
			nativeQuery = true)
	@Transactional
	int updateCategoriesWhereCountry(@Param("idFrom") String from, @Param("idTo") String to, @Param("country") int ebayCountry);
	
	/**
	 * Deletes obsolete categories which couldnt be mapped since category to which it should be mapped already existed within searches
	 * @param id
	 * @param ebayCountry
	 * @return
	 */
	@Modifying
	@Query(value = "DELETE from search_categories sc WHERE sc.categories_ebay_category_id = :id AND sc.categories_ebay_country_ebay_site_id = :country",
		nativeQuery = true)
	@Transactional
	int deleteCategoriesObsolote(@Param("id") String id, @Param("country") int ebayCountry);
	
}