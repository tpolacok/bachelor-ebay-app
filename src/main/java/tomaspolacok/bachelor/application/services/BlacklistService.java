package tomaspolacok.bachelor.application.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tomaspolacok.bachelor.application.entities.EbayItem;
import tomaspolacok.bachelor.application.entities.EbaySeller;
import tomaspolacok.bachelor.application.entities.Search;
import tomaspolacok.bachelor.application.repositories.UserRepository;

@Service
public class BlacklistService {

	@Autowired
	SearchService searchService;
	@Autowired
	UserRepository userRepository;
	@Autowired
	UserService userService;
	
	/**
	 * Add user blacklist for item
	 * @param item
	 * @param reason
	 */
	public void blacklistItemGlobally(EbayItem item, String reason) {
		userService.addBlacklistItem(item, reason);
		searchService.deleteItemFromSearchAll(item);
	}
	
	/**
	 * Deletes user blacklist for item
	 * @param item
	 */
	public void blacklistItemDeleteGlobally(EbayItem item) {
		userService.deleteBlacklistItem(item);
	}
	
	/**
	 * Add search blacklist for item
	 * @param item
	 * @param search
	 * @param reason
	 */
	public void blacklistItemLocally(EbayItem item, Search search, String reason) {
		searchService.addBlacklistItem(item, search, reason);
		searchService.deleteItemFromSearch(item, search);
	}
	
	/**
	 * Deletes search blacklist for item
	 * @param item
	 * @param search
	 */
	public void blacklistItemDeleteLocally(EbayItem item, Search search) {
		searchService.deleteBlacklistItem(item, search);
	}
	
	/**
	 * Add user blacklist for seller
	 * @param seller
	 * @param reason
	 */
	public void blacklistSellerGlobally(EbaySeller seller, String reason) {
		userService.addBlacklistSeller(seller, reason);
		searchService.deleteItemFromSearchBySellerAll(seller);
	}
	
	/**
	 * Deletes user blacklist for seller
	 * @param seller
	 */
	public void blacklistSellerDeleteGlobally(EbaySeller seller) {
		userService.deleteBlacklistSeller(seller);
	}
	
	/**
	 * Add search blacklist for seller
	 * @param seller
	 * @param reason
	 */
	public void blacklistSellerLocally(EbaySeller seller, Search search, String reason) {
		searchService.addBlacklistSeller(seller, search, reason);
		searchService.deleteItemFromSearchBySeller(seller, search);
	}
	
	/**
	 * Deletes search blacklist for seller
	 * @param seller
	 * @param search
	 */
	public void blacklistSellerDeleteLocally(EbaySeller seller, Search search) {
		searchService.deleteBlacklistSeller(seller, search);
	}
	
	
}
