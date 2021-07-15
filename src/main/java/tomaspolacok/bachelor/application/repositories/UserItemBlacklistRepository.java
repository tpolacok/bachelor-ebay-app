package tomaspolacok.bachelor.application.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tomaspolacok.bachelor.application.entities.EbayItem;
import tomaspolacok.bachelor.application.entities.User;
import tomaspolacok.bachelor.application.entities.UserItemBlacklist;

public interface UserItemBlacklistRepository extends JpaRepository<UserItemBlacklist, Long> {
	
	/**
	 * Selects blacklist mapping between user and item
	 * @param search
	 * @param item
	 * @return
	 */
	public UserItemBlacklist findByUserAndItem(User user, EbayItem item);
	
	/**
	 * Selects page of blacklist mappings between user and items
	 * @param search
	 * @param item
	 * @return
	 */
	@Query("select uib from UserItemBlacklist uib where uib.user = :user")
	public Page<UserItemBlacklist> findBlacklistItem(@Param("user") User user, Pageable pageable);
}

