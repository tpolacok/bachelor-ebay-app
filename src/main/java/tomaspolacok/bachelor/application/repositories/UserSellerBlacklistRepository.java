package tomaspolacok.bachelor.application.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tomaspolacok.bachelor.application.entities.EbaySeller;
import tomaspolacok.bachelor.application.entities.User;
import tomaspolacok.bachelor.application.entities.UserSellerBlacklist;

public interface UserSellerBlacklistRepository extends JpaRepository<UserSellerBlacklist, Long> {
	
	/**
	 * Selects blacklist mapping between user and item
	 * @param search
	 * @param item
	 * @return
	 */
	public UserSellerBlacklist findByUserAndSeller(User user, EbaySeller seller);
	
	/**
	 * Selects page of blacklist mappings between user and sellers
	 * @param search
	 * @param item
	 * @return
	 */
	@Query("select usb from UserSellerBlacklist usb where usb.user = :user")
	public Page<UserSellerBlacklist> findBlacklistSeller(@Param("user") User user, Pageable pageable);
}