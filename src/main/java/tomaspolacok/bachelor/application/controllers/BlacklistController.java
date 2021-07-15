package tomaspolacok.bachelor.application.controllers;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import tomaspolacok.bachelor.application.entities.EbayItem;
import tomaspolacok.bachelor.application.entities.EbaySeller;
import tomaspolacok.bachelor.application.entities.Search;
import tomaspolacok.bachelor.application.entities.User;
import tomaspolacok.bachelor.application.repositories.EbaySellerRepository;
import tomaspolacok.bachelor.application.services.BlacklistService;
import tomaspolacok.bachelor.application.services.ItemService;
import tomaspolacok.bachelor.application.services.SearchService;
import tomaspolacok.bachelor.application.services.SellerService;
import tomaspolacok.bachelor.application.services.UserService;

@Controller
public class BlacklistController {

	@Autowired
	EbaySellerRepository sellerRepository;
	@Autowired
	ItemService itemService;
	@Autowired
	SellerService sellerService;
	@Autowired
	SearchService searchService;
	@Autowired
	UserService userService;
	@Autowired
	BlacklistService blacklistService;
	
	/**
	 * Servlet used to get page of globally blacklisted sellers
	 * @param pageable
	 * @return
	 */
	@GetMapping(value={"/blacklist/sellers"})
	public ModelAndView blacklistSellersGlobal( @RequestParam(value = "size", required=false) Integer size,
												@RequestParam(value = "page", defaultValue="0", required=false) Integer page,
												@RequestParam(value = "all", required=false) Boolean all){
        ModelAndView modelAndView = new ModelAndView();
        User user = userService.getLoggedUser();
        userService.updateDisplayCount(user.getUserPreferences().getSellerBanCount(), size, all);
        modelAndView.addObject("page", userService.getBlacklistedSellers(page));
        modelAndView.addObject("displayCount", user.getUserPreferences().getSellerBanCount());
        modelAndView.addObject("sizes", Arrays.asList(12,24,36));
        modelAndView.addObject("global", true);
        modelAndView.setViewName("blacklistSellers");
        return modelAndView;
    }
	
	/**
	 * Servlet used to get page of globally blacklisted items
	 * @param pageable
	 * @return
	 */
	@GetMapping(value={"/blacklist/items"})
	public ModelAndView blacklistItemsGlobal( @RequestParam(value = "size", required=false) Integer size,
											  @RequestParam(value = "page", defaultValue="0", required=false) Integer page,
											  @RequestParam(value = "all", required=false) Boolean all){
        ModelAndView modelAndView = new ModelAndView();
        User user = userService.getLoggedUser();
        userService.updateDisplayCount(user.getUserPreferences().getItemBanCount(), size, all);
        modelAndView.addObject("page", userService.getBlacklistedItems(page));
        modelAndView.addObject("displayCount", user.getUserPreferences().getItemBanCount());
        modelAndView.addObject("sizes", Arrays.asList(12,24,36));
        modelAndView.addObject("global", true);
        modelAndView.setViewName("blacklistItems");
        return modelAndView;
    }
	
	/**
	 * Servlet used to add item in search's blacklist (locally)
	 * @param pageable
	 * @return
	 */
	@PostMapping("/blacklist/item/{itemId}")
	public ModelAndView blacklistItem(@PathVariable Long itemId, @RequestParam("search") Long searchId, @RequestParam String reason) {
		ModelAndView modelAndView = new ModelAndView();
		EbayItem item;
		Search search;
		try {
			item = itemService.getItem(itemId);
			search = searchService.getSearch(searchId);
		} catch(Exception e) {
			modelAndView.setViewName("redirect:/error");
			return modelAndView;
		}
		blacklistService.blacklistItemLocally(item, search, reason);
		modelAndView.setViewName("redirect:/search/view/" + searchId);
		return modelAndView;
	}
	
	/**
	 * Servlet used to delete item from search's blacklist (locally)
	 * @param pageable
	 * @return
	 */
	@PostMapping("/blacklist/item/delete/{itemId}")
	public ModelAndView blacklistItemDelete(@PathVariable Long itemId, @RequestParam("search") Long searchId) {
		ModelAndView modelAndView = new ModelAndView();
		EbayItem item;
		Search search;
		try {
			item = itemService.getItem(itemId);
			search = searchService.getSearch(searchId);
		} catch(Exception e) {
			modelAndView.setViewName("redirect:/error");
			return modelAndView;
		}
		blacklistService.blacklistItemDeleteLocally(item, search);
		modelAndView.setViewName("redirect:/search/blacklist/items/" + searchId);
		return modelAndView;
	}
	
	/**
	 * Servlet used to add item in user's blacklist (globally)
	 * @param pageable
	 * @return
	 */
	@PostMapping("/blacklist/item/global/{itemId}")
	public ModelAndView blacklistItemGlobal(@PathVariable Long itemId, @RequestParam("search") Long searchId, @RequestParam String reason) {
		ModelAndView modelAndView = new ModelAndView();
		EbayItem item;
		try {
			item = itemService.getItem(itemId);
		} catch(Exception e) {
			modelAndView.setViewName("redirect:/error");
			return modelAndView;
		}
		blacklistService.blacklistItemGlobally(item, reason);
		modelAndView.setViewName("redirect:/search/view/" + searchId);
		return modelAndView;
	}
	
	/**
	 * Servlet used to delete item from user's blacklist (globally)
	 * @param pageable
	 * @return
	 */
	@PostMapping("/blacklist/item/delete/global/{itemId}")
	public ModelAndView blacklistSellerItemGlobal(@PathVariable Long itemId) {
		ModelAndView modelAndView = new ModelAndView();
		EbayItem item;
		try {
			item = itemService.getItem(itemId);
		} catch(Exception e) {
			modelAndView.setViewName("redirect:/error");
			return modelAndView;
		}
		blacklistService.blacklistItemDeleteGlobally(item);
		modelAndView.setViewName("redirect:/blacklist/items");
		return modelAndView;
	}
	
	/**
	 * Servlet used to add seller in search's blacklist (locally)
	 * @param pageable
	 * @return
	 */
	@PostMapping("/blacklist/seller/{sellerName}")
	public ModelAndView blacklistSeller(@PathVariable String sellerName, @RequestParam("search") Long searchId, @RequestParam String reason) {
		ModelAndView modelAndView = new ModelAndView();
		EbaySeller seller;
		Search search;
		try {
			seller = sellerService.getSeller(sellerName);
			search = searchService.getSearch(searchId);
		} catch(Exception e) {
			modelAndView.setViewName("redirect:/error");
			return modelAndView;
		}
		blacklistService.blacklistSellerLocally(seller, search, reason);
		modelAndView.setViewName("redirect:/search/view/" + searchId);
		return modelAndView;
	}
	
	/**
	 * Servlet used to delete seller from search's blacklist (locally)
	 * @param pageable
	 * @return
	 */
	@PostMapping("/blacklist/seller/delete/{sellerName}")
	public ModelAndView blacklistSellerDelete(@PathVariable String sellerName, @RequestParam("search") Long searchId) {
		ModelAndView modelAndView = new ModelAndView();
		EbaySeller seller;
		Search search;
		try {
			seller = sellerService.getSeller(sellerName);
			search = searchService.getSearch(searchId);
		} catch(Exception e) {
			modelAndView.setViewName("redirect:/error");
			return modelAndView;
		}
		blacklistService.blacklistSellerDeleteLocally(seller, search);
		modelAndView.setViewName("redirect:/search/blacklist/sellers/" + searchId);
		return modelAndView;
	}
	
	/**
	 * Servlet used to add seller in user's blacklist (globally)
	 * @param pageable
	 * @return
	 */
	@PostMapping("/blacklist/seller/global/{sellerName}")
	public ModelAndView blacklistSellerGlobal(@PathVariable String sellerName, @RequestParam("search") Long searchId, @RequestParam String reason) {
		ModelAndView modelAndView = new ModelAndView();
		EbaySeller seller;
		try {
			seller = sellerService.getSeller(sellerName);
		} catch(Exception e) {
			modelAndView.setViewName("redirect:/error");
			return modelAndView;
		}
		blacklistService.blacklistSellerGlobally(seller, reason);
		modelAndView.setViewName("redirect:/search/view/" + searchId);
		return modelAndView;
	}
	
	/**
	 * Servlet used to delete seller from user's blacklist (globally)
	 * @param pageable
	 * @return
	 */
	@PostMapping("/blacklist/seller/delete/global/{sellerName}")
	public ModelAndView blacklistSellerDeleteGlobal(@PathVariable String sellerName) {
		ModelAndView modelAndView = new ModelAndView();
		EbaySeller seller;
		try {
			seller = sellerService.getSeller(sellerName);
		} catch(Exception e) {
			modelAndView.setViewName("redirect:/error");
			return modelAndView;
		}
		blacklistService.blacklistSellerDeleteGlobally(seller);
		modelAndView.setViewName("redirect:/blacklist/sellers");
		return modelAndView;
	}
	
	
}
