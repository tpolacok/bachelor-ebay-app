package tomaspolacok.bachelor.application.helper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Set;
import java.util.TimeZone;

import com.ebay.services.finding.FindItemsAdvancedRequest;
import com.ebay.services.finding.ItemFilter;
import com.ebay.services.finding.ItemFilterType;
import com.ebay.services.finding.OutputSelectorType;
import com.ebay.services.finding.PaginationInput;

import tomaspolacok.bachelor.application.entities.Category;
import tomaspolacok.bachelor.application.entities.Country;
import tomaspolacok.bachelor.application.entities.Search;
import tomaspolacok.bachelor.application.entities.SearchSellerBlacklist;
import tomaspolacok.bachelor.application.entities.UserSellerBlacklist;

public class SearchRequestMaker {
	
	
	public static int KEYWORD_LENGTH_MAX = 98;
	public static int KEYWORDS_LENGTH_MAX = 350;
	
	/**
	 * Creates FindItemsAdvancedRequest and fills it with item filters and other criteria
	 * @param search
	 * @return
	 */
	public static FindItemsAdvancedRequest createRequest(Search search) {
		FindItemsAdvancedRequest request = new FindItemsAdvancedRequest();
		request.setDescriptionSearch(search.getKeywordsDescription());
		request.getItemFilter().add(freeShippingFilter(search));
		request.getItemFilter().add(priceFilter(search, "max"));
		request.getItemFilter().add(priceFilter(search, "min"));
		
		if(search.getEbayCountry().getCategoryEnabled()) {
			for(Category cat: search.getCategories()) {
				request.getCategoryId().add(cat.getId().getId());
			}
		}
		if (search.getEbayCountry().getReturnsOnlyEnabled()) {
			request.getItemFilter().add(returnsAcceptedFilter(search));
		}
		if (search.getSellerSearch()) {
			request.getItemFilter().add(sellerFilter(search));
		} else if ( search.getBlacklistSellers().size() > 0 || search.getUser().getBlacklistSellers().size() > 0) {
			request.getItemFilter().add(sellerExcludeFilter(search));
		}
		
		if (search.getShippingTo().size() > 0) {
			request.getItemFilter().add(countryFilter(search));
		}
		if (search.getEbayCountry().getConditionEnabled() && search.getNewOnly()) {
			request.getItemFilter().add(conditionFilter(search));
		}
		
		request.getOutputSelector().add(OutputSelectorType.SELLER_INFO);
		PaginationInput pi = new PaginationInput();
		pi.setEntriesPerPage(100);
		pi.setPageNumber(1);
		request.setPaginationInput(pi);
		
		if (search.getEndTimeFrom() != null) {
			request.getItemFilter().add(timeFilter(search, "from"));
		}
		if (search.getEndTimeTo() != null) {
			request.getItemFilter().add(timeFilter(search, "to"));
		}
		return request;
	}
	
	/**
	 * Creates condition filter from search
	 * @param search
	 * @return
	 */
	public static ItemFilter conditionFilter(Search search) {
		ItemFilter itemFilter = new ItemFilter();
		itemFilter.setName(ItemFilterType.CONDITION);
		itemFilter.getValue().add(Integer.toString(tomaspolacok.bachelor.application.enums.Condition.NEW.getType()));
		return itemFilter;
	}
	
	/**
	 * Creates seller filter from search
	 * @param search
	 * @return
	 */
	public static ItemFilter sellerFilter(Search search) {
		ItemFilter itemFilter = new ItemFilter();
		itemFilter.setName(ItemFilterType.SELLER);
		for (String seller : search.getSellers()) {
			itemFilter.getValue().add(seller);
		}
		return itemFilter;
	}
	
	/**
	 * Creates seller exclude filter from search
	 * @param search
	 * @return
	 */
	public static ItemFilter sellerExcludeFilter(Search search) {
		ItemFilter itemFilter = new ItemFilter();
		itemFilter.setName(ItemFilterType.EXCLUDE_SELLER);
		for (SearchSellerBlacklist blacklist : search.getBlacklistSellers()) {
			itemFilter.getValue().add(blacklist.getSeller().getName());
		}
		
		for (UserSellerBlacklist blacklist : search.getUser().getBlacklistSellers()) {
			itemFilter.getValue().add(blacklist.getSeller().getName());
		}
		return itemFilter;
	}
	
	/**
	 * Creates country filter from search
	 * @param search
	 * @return
	 */
	public static ItemFilter countryFilter(Search search) {
		ItemFilter itemFilter = new ItemFilter();
		itemFilter.setName(ItemFilterType.AVAILABLE_TO);
		for (Country country : search.getShippingTo()) {
			itemFilter.getValue().add(country.getCode());
		}
		return itemFilter;
	}
	
	/**
	 * Creates time filter from search, type is either from or to
	 * @param search
	 * @param type
	 * @return
	 */
	public static ItemFilter timeFilter(Search search, String type) {
		DateFormat converter = new SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ss.SSS'Z'");
	    converter.setTimeZone(TimeZone.getTimeZone("GMT"));
		ItemFilter itemFilter = new ItemFilter();
		if (type == "from") {
			itemFilter.setName(ItemFilterType.END_TIME_FROM);
			itemFilter.getValue().add(converter.format(search.getEndTimeFrom()));
		} else {
			itemFilter.setName(ItemFilterType.END_TIME_TO);
			itemFilter.getValue().add(converter.format(search.getEndTimeTo()));
		}
		return itemFilter;
	}
	
	/**
	 * Adds modified time filter so item count gets reduced
	 * @param search
	 * @return
	 */
	public static ItemFilter startTimeFilter(Search search) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		DateFormat converter = new SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ss.SSS'Z'");
	    converter.setTimeZone(TimeZone.getTimeZone("GMT"));
		ItemFilter itemFilter = new ItemFilter();
		itemFilter.setName(ItemFilterType.MOD_TIME_FROM);
		itemFilter.getValue().add(converter.format(cal.getTime()));
		return itemFilter;
	}
	
	/**
	 * Creates price filter from search, type is either min or max determining minimal or maximal price
	 * @param search
	 * @param type
	 * @return
	 */
	public static ItemFilter priceFilter(Search search, String type) {
		ItemFilter itemFilter = new ItemFilter();
		if (type == "max") {
			itemFilter.setName(ItemFilterType.MAX_PRICE);
			if (search.getMaxPrice() == 0) {
				itemFilter.getValue().add(Integer.toString(Integer.MAX_VALUE));
			} else {
				itemFilter.getValue().add(Integer.toString(search.getMaxPrice()));
			}
		} else {
			itemFilter.setName(ItemFilterType.MIN_PRICE);
			itemFilter.getValue().add(Integer.toString(search.getMinPrice()));
		}
		return itemFilter;
	}
	
	/**
	 * Creates price filter from search, type is either min or max determining minimal or maximal price
	 * @param search
	 * @param type
	 * @return
	 */
	public static ItemFilter bidsFilter(Search search, String type) {
		ItemFilter itemFilter = new ItemFilter();
		if (type == "max") {
			itemFilter.setName(ItemFilterType.MAX_BIDS);
			if (search.getMaxBids() == 0) {
				itemFilter.getValue().add(Integer.toString(Integer.MAX_VALUE));
			} else {
				itemFilter.getValue().add(Integer.toString(search.getMaxBids()));
			}
		} else {
			itemFilter.setName(ItemFilterType.MIN_BIDS);
			itemFilter.getValue().add(Integer.toString(search.getMinBids()));
		}
		return itemFilter;
	}
	 
	/**
	 * Creates free shipping filter
	 * @param search
	 * @return
	 */
	public static ItemFilter freeShippingFilter(Search search) {
		ItemFilter itemFilter = new ItemFilter();
		itemFilter.setName(ItemFilterType.FREE_SHIPPING_ONLY);
		if (search.getFreeShippingOnly()) {
			itemFilter.getValue().add("true");
		} else {
			itemFilter.getValue().add("false");
		}
		return itemFilter;
	}
	
	/**
	 * Creates returns accepted filter
	 * @param search
	 * @return
	 */
	public static ItemFilter returnsAcceptedFilter(Search search) {
		ItemFilter itemFilter = new ItemFilter();
		itemFilter.setName(ItemFilterType.RETURNS_ACCEPTED_ONLY);
		if (search.getReturnsAcceptedOnly()) {
			itemFilter.getValue().add("true");
		} else {
			itemFilter.getValue().add("false");
		}
		return itemFilter;
	}
	
	/**
	 * Returns item filter for fixed price listing type
	 * @param search
	 * @return
	 */
	public static ItemFilter listingTypeFixedPrice(Search search) {
		ItemFilter itemFilter = new ItemFilter();
		itemFilter.setName(ItemFilterType.LISTING_TYPE);
		itemFilter.getValue().add(tomaspolacok.bachelor.application.enums.ListingType.FIXED_PRICE.getType());
		return itemFilter;
	}
	
	/**
	 * Returns item filter for auction listing types
	 * @param search
	 * @return
	 */
	public static ItemFilter listingTypeAuction(Search search) {
		ItemFilter itemFilter = new ItemFilter();
		itemFilter.setName(ItemFilterType.LISTING_TYPE);
		for (tomaspolacok.bachelor.application.enums.ListingType lt : search.getListingTypes()) {
			if (lt != tomaspolacok.bachelor.application.enums.ListingType.FIXED_PRICE) {
				itemFilter.getValue().add(lt.getType());
			}
		}
		return itemFilter;
	}
	
	/**
	 * Checks whether specific search has fixed price listing type selected
	 * @param search
	 * @return
	 */
	public static Boolean isFixedPriceSelected(Search search) {
		for (tomaspolacok.bachelor.application.enums.ListingType lt : search.getListingTypes()) {
			if (lt == tomaspolacok.bachelor.application.enums.ListingType.FIXED_PRICE) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks whether specific search has auction listing types selected
	 * @param search
	 * @return
	 */
	public static Boolean isAuctionSelected(Search search) {
		for (tomaspolacok.bachelor.application.enums.ListingType lt : search.getListingTypes()) {
			if (lt == tomaspolacok.bachelor.application.enums.ListingType.AUCTION || lt == tomaspolacok.bachelor.application.enums.ListingType.AUCTION_BUY_IT_NOW) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks whether keywords length has been violated
	 * @param keywords
	 * @return
	 */
	public static boolean keywordsLimitLengthViolated(Set<String> keywords) {
		int max = Integer.MIN_VALUE;
		for(String keyword : keywords) {
			// add 2 quotation marks
			int len = keyword.length() + 2;
			if (len > max) {
				max = len;
			}
		}
		return (max > KEYWORD_LENGTH_MAX);
	}
	
	/**
	 * Creates keyword filter, puts together keyword include and exclude strings
	 * @param search
	 * @return
	 */
	public static Boolean keywordsLengthLimitLengthViolated(Set<String> keywords) {
		for (String keyword : keywords) {
			if (keyword.length() > KEYWORDS_LENGTH_MAX) return true;
		}
		return false;
	}

}
