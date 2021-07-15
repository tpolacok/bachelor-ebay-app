package tomaspolacok.bachelor.application.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ebay.services.finding.FindItemsAdvancedResponse;
import com.ebay.services.finding.SearchItem;

import tomaspolacok.bachelor.application.entities.Country;
import tomaspolacok.bachelor.application.entities.Currency;
import tomaspolacok.bachelor.application.entities.EbayItem;
import tomaspolacok.bachelor.application.entities.EbaySeller;
import tomaspolacok.bachelor.application.entities.Search;
import tomaspolacok.bachelor.application.entities.SearchItemBlacklist;
import tomaspolacok.bachelor.application.entities.SellingStatus;
import tomaspolacok.bachelor.application.entities.Shipping;
import tomaspolacok.bachelor.application.entities.User;
import tomaspolacok.bachelor.application.entities.UserItemBlacklist;
import tomaspolacok.bachelor.application.enums.ListingType;
import tomaspolacok.bachelor.application.enums.SellingState;
import tomaspolacok.bachelor.application.enums.ShippingType;
import tomaspolacok.bachelor.application.repositories.CountryRepository;
import tomaspolacok.bachelor.application.repositories.EbayItemRepository;
import tomaspolacok.bachelor.application.repositories.EbaySellerRepository;
import tomaspolacok.bachelor.application.repositories.SearchPreferencesRepository;
import tomaspolacok.bachelor.application.repositories.SellingStatusRepository;
import tomaspolacok.bachelor.application.repositories.ShippingRepository;

@Service
public class ItemService {

	@Autowired
	EbayItemRepository itemRepository;
	@Autowired
	EbaySellerRepository sellerRepository;
	@Autowired
	ShippingRepository shippingRepository;
	@Autowired
	CountryRepository countryRepository;
	@Autowired
	SellingStatusRepository sellingStatusRepository;
	@Autowired
	SearchPreferencesRepository searchPreferencesRepository;
	@Autowired
	SearchService searchService;
	@Autowired
	EbayService ebayService;
	@Autowired
	UserService userService;
	@Autowired
	NotificationService notificationService;
	@Autowired
	CurrencyService currencyService;

	/**
	 * Gets item based on id or throws and exception if item is not found
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public EbayItem getItem(Long id) throws Exception {
		EbayItem item = itemRepository.getOne(id);
		return item;
	}

	/**
	 * Gets item and updates it(updates information, loads pictures) by using eBay API and tries to get it's bids by parsing it in EbayService method
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public EbayItem getItemForDisplay(Long id, Search search) throws Exception {
		EbayItem item = getItem(id);
		try {
			ebayService.updateItem(item);
			item = ebayService.processEbayItem(item);
			Currency desiredCurrency;
			if (search != null) {
				desiredCurrency = currencyService.getCurrency(search.getEbayCountry().getCurrency(), userService.getLoggedUser().getUserPreferences().getCurrency());
			} else {
				desiredCurrency = currencyService.getCurrency(null, userService.getLoggedUser().getUserPreferences().getCurrency());
			}
			if (desiredCurrency != null) {
				Double rate = currencyService.getCurrencyRate(desiredCurrency);
				currencyService.convertItemWithRate(item, desiredCurrency, rate);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return item;
	}

	/**
	 * Function processes find item response from eBay, creates list of ebayItem entity and all the other objects that are linked to it, then removes items 
	 * from the list which are present in the blacklist, adds new items to user's known items (if it's automatic then new items are sent via email to user)
	 * @param response
	 * @param search
	 * @param automatic
	 */
	public void processEbayResponse(List<FindItemsAdvancedResponse> responses, Search search, Boolean automatic) {
		searchService.saveSearch(search);
		Set<EbayItem> itemSet = new HashSet<>();
		//get converter
		Currency defaultCurrency = new Currency("USD");
		Double rate = currencyService.getCurrencyRate(search.getEbayCountry().getCurrency(), defaultCurrency);
		
		for (FindItemsAdvancedResponse response : responses) {
			for (SearchItem item : response.getSearchResult().getItem()) {
				EbayItem ebayItem = itemRepository.findById(Long.parseLong(item.getItemId())).orElse(new EbayItem());
				ebayItem.setId(Long.parseLong(item.getItemId()));
				ebayItem.setTitle(item.getTitle());
				ebayItem.setSubtitle(item.getSubtitle());
				ebayItem.setPrimaryCategory(item.getPrimaryCategory().getCategoryName());
				ebayItem.setDefaultPicture(item.getGalleryURL());
				ebayItem.setReturnsAccepted(item.isReturnsAccepted());
				ebayItem.setItemUrl(item.getViewItemURL());
				ebayItem.setBuyItNow(item.getListingInfo().isBuyItNowAvailable());
				if (item.getListingInfo().isBuyItNowAvailable()) {
					ebayItem.setBuyItNowPrice(item.getListingInfo().getConvertedBuyItNowPrice().getValue() * rate);
				}
				
				if (item.getCondition() != null) {
					ebayItem.setCondition(tomaspolacok.bachelor.application.enums.Condition.getCondition(item.getCondition().getConditionId()));
				} else {
					ebayItem.setCondition(tomaspolacok.bachelor.application.enums.Condition.NOT_SPECIFIED);
				}
				if (item.getCountry() != null) {
					Country country = new Country();
					country.setCode(item.getCountry());
					ebayItem.setCountry(country);
				}
	
				ebayItem.setCurrency(defaultCurrency);
	
				Currency bidsCurrency = new Currency(item.getSellingStatus().getCurrentPrice().getCurrencyId());
				ebayItem.setBidsCurrency(bidsCurrency);
	
				EbaySeller seller = processSeller(item);
				ebayItem.setSeller(seller);
	
				Shipping shipping = processShipping(item, defaultCurrency, rate);
				ebayItem.setShipping(shipping);
	
				SellingStatus status = processSellingStatus(item, rate);
				ebayItem.setSellingStatus(status);

				ebayItem.setListingType(ListingType.getListingType(item.getListingInfo().getListingType()));
	
				if (item.getListingInfo().getStartTime() != null) {
					ebayItem.setStartTime(item.getListingInfo().getStartTime().getTime());
				}
				if (item.getListingInfo().getEndTime() != null) {
					ebayItem.setEndTime(item.getListingInfo().getEndTime().getTime());
				}
				itemSet.add(ebayItem);
			}
		}
		
		itemRepository.saveAll(itemSet);
		List<EbayItem> allowedList = cutBlacklists(itemSet, search);

		search.setItems(allowedList);
		search.setLastRefreshed(new Date());		
		searchService.saveSearch(search);
		
		//Determine which items are new for the users and send the list of those items to Notification service
		List<EbayItem> listItemsToNotify = getUserNewItems(allowedList, search.getUser());
		if (automatic && !listItemsToNotify.isEmpty()) {
			notificationService.notify(listItemsToNotify, search);
		}
	}
	
	/**
	 * Returns list of items to notify for user
	 * @param itemList
	 * @param user
	 * @return
	 */
	private List<EbayItem> getUserNewItems(List<EbayItem> itemList, User user) {
		List<EbayItem> listItemsToNotify = new ArrayList<>();
		for (EbayItem item : itemList) {
			if (!user.getNotifiedItems().contains(item)) {
				listItemsToNotify.add(item);
				user.getNotifiedItems().add(item);
			}
		}
		userService.updateUser(user);
		return listItemsToNotify;
	}
	
	/**
	 * Method returns EbayItem list without blacklisted items of user and search
	 * Also checks for matching titles
	 * @param list
	 * @param search
	 * @return
	 */
	private List<EbayItem> cutBlacklists(Set<EbayItem> itemSet, Search search) {
		List<EbayItem> allowedList = new ArrayList<>();
		for (EbayItem ei : itemSet) {
			Boolean isBlacklisted = false;
			for (UserItemBlacklist uib : search.getUser().getBlacklistItems()) {
				if (ei.getId() == uib.getItem().getId() || (ei.getTitle().compareTo(uib.getItem().getTitle()) == 0)) {
					isBlacklisted = true;
					break;
				}
			}
			if (isBlacklisted) {
				continue;
			} else {
				for (SearchItemBlacklist sib : search.getBlacklistItems()) {
					if (ei.getId() == sib.getItem().getId() || (ei.getTitle().compareTo(sib.getItem().getTitle()) == 0)) {
						isBlacklisted = true;
						break;
					}
				}
			}
			if (!isBlacklisted) {
				allowedList.add(ei);
			}
		}
		return allowedList;
	}

	/**
	 * Adds seller information to EbayItem object
	 * @param item
	 * @return
	 */
	private EbaySeller processSeller(SearchItem item) {
		EbaySeller seller = new EbaySeller();
		seller.setName(item.getSellerInfo().getSellerUserName());
		seller.setFeedbackPositivePercent(item.getSellerInfo().getPositiveFeedbackPercent());
		seller.setFeedbackScore(item.getSellerInfo().getFeedbackScore());
		seller.setTopRated(item.getSellerInfo().isTopRatedSeller());
		sellerRepository.save(seller);
		return seller;
	}

	/**
	 * Adds shipping information to EbayItem object
	 * @param item
	 * @param currency
	 * @param rate
	 * @return
	 */
	private Shipping processShipping(SearchItem item, Currency currency, Double rate) {
		Shipping shipping = new Shipping();
		shipping.setId(Long.parseLong(item.getItemId()));
		shipping.setExpeditedShipping(item.getShippingInfo().isExpeditedShipping());
		if (item.getShippingInfo().getHandlingTime() != null)
			shipping.setHandlingTime(item.getShippingInfo().getHandlingTime());
		shipping.setOneDayShipping(item.getShippingInfo().isOneDayShippingAvailable());
		if (item.getShippingInfo().getShippingServiceCost() != null) {
			shipping.setShippingCost(item.getShippingInfo().getShippingServiceCost().getValue() * rate);
			shipping.setCurrency(currency);
		}
		shipping.setShippingType(ShippingType.getShippingType(item.getShippingInfo().getShippingType()));
		shippingRepository.save(shipping);
		return shipping;
	}

	/**
	 * Adds selling status information to EbayItem object
	 * @param item
	 * @param rate
	 * @return
	 */
	private SellingStatus processSellingStatus(SearchItem item, Double rate) {
		SellingStatus status = new SellingStatus();
		status.setId(Long.parseLong(item.getItemId()));
		if (item.getSellingStatus().getBidCount() != null) {
			status.setBidCount(item.getSellingStatus().getBidCount());
		}
		status.setCurrentPrice(item.getSellingStatus().getConvertedCurrentPrice().getValue() * rate);
		status.setSellingState(SellingState.getSellingState(item.getSellingStatus().getSellingState()));
		sellingStatusRepository.save(status);
		return status;
	}

}
