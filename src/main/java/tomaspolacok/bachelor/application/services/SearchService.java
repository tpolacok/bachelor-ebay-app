package tomaspolacok.bachelor.application.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.bpodgursky.jbool_expressions.Expression;
import com.bpodgursky.jbool_expressions.parsers.ExprParser;
import com.bpodgursky.jbool_expressions.rules.RuleSet;
import com.ebay.services.finding.FindItemsAdvancedRequest;
import com.ebay.services.finding.FindItemsAdvancedResponse;

import tomaspolacok.bachelor.application.entities.Category;
import tomaspolacok.bachelor.application.entities.DisplayCount;
import tomaspolacok.bachelor.application.entities.EbayCountry;
import tomaspolacok.bachelor.application.entities.EbayItem;
import tomaspolacok.bachelor.application.entities.EbaySeller;
import tomaspolacok.bachelor.application.entities.Search;
import tomaspolacok.bachelor.application.entities.SearchItemBlacklist;
import tomaspolacok.bachelor.application.entities.SearchPhoneNotificationSettings;
import tomaspolacok.bachelor.application.entities.SearchPreferences;
import tomaspolacok.bachelor.application.entities.SearchSellerBlacklist;
import tomaspolacok.bachelor.application.entities.User;
import tomaspolacok.bachelor.application.enums.ListingType;
import tomaspolacok.bachelor.application.enums.SortEnum;
import tomaspolacok.bachelor.application.exceptions.KeywordLengthViolationException;
import tomaspolacok.bachelor.application.exceptions.TooManyItemsException;
import tomaspolacok.bachelor.application.helper.SearchRequestMaker;
import tomaspolacok.bachelor.application.repositories.DisplayCountRepository;
import tomaspolacok.bachelor.application.repositories.EbayItemRepository;
import tomaspolacok.bachelor.application.repositories.SearchItemBlacklistRepository;
import tomaspolacok.bachelor.application.repositories.SearchPhoneNotificationSettingsRepository;
import tomaspolacok.bachelor.application.repositories.SearchPreferencesRepository;
import tomaspolacok.bachelor.application.repositories.SearchRepository;
import tomaspolacok.bachelor.application.repositories.SearchSellerBlacklistRepository;

@Service
public class SearchService {

	@Autowired
	private SearchRepository searchRepository;
	@Autowired
	private SearchSellerBlacklistRepository sellerBanRepository;
	@Autowired
	private SearchItemBlacklistRepository itemBanRepository;
	@Autowired
	private EbayItemRepository itemRepository;
	@Autowired
	private SearchPreferencesRepository searchPreferencesRepository;
	@Autowired
	private SearchPhoneNotificationSettingsRepository searchPhoneNotificationRepository;
	@Autowired
	private DisplayCountRepository displayCountRepository;
	@Autowired
	private UserService userService;
	@Autowired
	private EbayService ebayService;
	@Autowired
	private ItemService itemService;
	@Autowired
	private CurrencyService currencyService;
	@Autowired
	private EbayCountryService ebayCountryService;
	
	private final String KEYWORD_LENGTH_MAX_ERROR = "Maximum length of keyword query is 98.";
	private final String KEYWORDS_LENGTH_MAX_ERROR = "Maximum length of keyword query is 350.";
	private final int KEYWORD_LENGTH_MAX = 98;
	private final int KEYWORDS_LENGTH_MAX = 350;
	private final int MAX_ITEMS_FOUND = 100;
	
	/**
	 * Method removes blacklist of item for specific search
	 * @param item
	 * @param search
	 */
	public void deleteBlacklistItem(EbayItem item, Search search) {
    	SearchItemBlacklist sib = itemBanRepository.findBySearchAndItem(search, item);
    	search.getBlacklistItems().remove(sib);
		searchRepository.save(search);
		itemBanRepository.delete(sib);
	}
	
	/**
	 * Method removes blacklist of seller for specific search
	 * @param seller
	 * @param search
	 */
	public void deleteBlacklistSeller(EbaySeller seller, Search search) {
		SearchSellerBlacklist ssb = sellerBanRepository.findBySearchAndSeller(search, seller);
    	search.getBlacklistSellers().remove(ssb);
		searchRepository.save(search);
		sellerBanRepository.delete(ssb);
	}
	
	/**
	 * Method adds blacklist of item for specific search
	 * @param item
	 * @param search
	 * @param reason
	 */
	public void addBlacklistItem(EbayItem item, Search search, String reason) {
		SearchItemBlacklist sib = new SearchItemBlacklist();
		sib.setItem(item);
		sib.setSearch(search);
		sib.setReason(reason);
		if (!search.getBlacklistItems().contains(sib)) {
			search.getBlacklistItems().add(sib);
			itemBanRepository.save(sib);
			searchRepository.save(search);
		}
	}
	
	/**
	 * Method adds blacklist of seller for specific search
	 * @param seller
	 * @param search
	 * @param reason
	 */
	public void addBlacklistSeller(EbaySeller seller, Search search, String reason) {
		SearchSellerBlacklist ssb = new SearchSellerBlacklist();
		ssb.setSeller(seller);
		ssb.setSearch(search);
		ssb.setReason(reason);
		if (!search.getBlacklistSellers().contains(ssb)) {
			search.getBlacklistSellers().add(ssb);
			sellerBanRepository.save(ssb);
			searchRepository.save(search);
		}
	}
	
	/**
	 * Method deletes ebay item from specific search
	 * @param item
	 * @param search
	 */
	public void deleteItemFromSearch(EbayItem item, Search search) {
		searchRepository.deleteItemFromSearch(item.getId(), search.getId());
	}
	
	/**
	 * Method deletes ebay item from all searches of logged user
	 * @param item
	 */
	public void deleteItemFromSearchAll(EbayItem item) {
		User user = userService.getLoggedUser();
		searchRepository.deleteItemFromSearchAll(item.getId(), user.getId());
	}
	
	/**
	 * Method deletes ebay item from search by seller name
	 * @param seller
	 * @param search
	 */
	public void deleteItemFromSearchBySeller(EbaySeller seller, Search search) {
		//todo test
		searchRepository.deleteItemFromSearchBySeller(seller.getName(), search.getId());
	}
	
	/**
	 * Method deletes ebay item listed by seller from all searches of logged user
	 * @param seller
	 */
	public void deleteItemFromSearchBySellerAll(EbaySeller seller) {
		//todo test
		User user = userService.getLoggedUser();
		searchRepository.deleteItemFromSearchBySellerAll(seller.getName(), user.getId());
	}
	
	/**
	 * returns page of searches created by user
	 * @param page
	 * @return
	 */
	public Page<Search> getAll(Integer page) {
		User user = userService.getLoggedUser();
		return searchRepository.findAllByUser(user, user.getUserPreferences().getSearchCount().getPageable(page));
	}
	
	/**
	 * Returns search of user by id or throws exception if search is not found
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public Search getSearch(Long id) throws Exception {
		User user = userService.getLoggedUser();
		Search search = searchRepository.findByUserAndId(user, id);
		if (search == null) throw new Exception();
		return search;
	}
	
	/**
	 * Activates or deactivate search
	 * @param id
	 * @throws Exception
	 */
	public void activateSearch(Long id) throws Exception {
		Search search = getSearch(id);
		search.setActive(!search.getActive());
		searchRepository.save(search);
	}
	
	/**
	 * Removes search(search still exists but is no longer accessible by user)
	 * @param id
	 * @throws Exception
	 */
	public void removeSearch(Long id) throws Exception {
		Search search = getSearch(id);
		search.setRemoved(true);
		searchRepository.save(search);
	}
	
	/**
	 * Enables search
	 * @param search
	 * @param enable
	 */
	private void enableSearch(Search search, Boolean enable) {
		search.setEnabled(enable);
		searchRepository.save(search);
	}
	
	/**
	 * Initiates search, sets user and other parameters
	 * @param search
	 */
	public void initSearch(Search search) {
		User user = userService.getLoggedUser();
		search.setUser(user);
		search.setActive(true);
		search.setEnabled(true);
		search.setRemoved(false);
		if (!search.getSellers().isEmpty()) {
			search.setSellerSearch(true);
		} else {
			search.setSellerSearch(false);
		}
		setConvertedValues(user, search);
	}
	
	/**
	 * Converts min/max prices in user has preferred currency
	 * @param search
	 */
	public void setConvertedValues(User user, Search search) {
		if (user.getUserPreferences().getCurrency() != null) {
			Double rate = currencyService.getCurrencyRate(user.getUserPreferences().getCurrency(), search.getEbayCountry().getCurrency());
			search.setMinPrice((int)(search.getMinPrice() * rate) );
			search.setMaxPrice((int)(search.getMaxPrice() * rate) );
		}
	}
	
	/**
	 * Prepares search for updating (converts ebay site currency to user currency)
	 * @param search
	 */
	public void prepareSearchForUpdate(Search search) {
		Double rate = currencyService.getCurrencyRate(search.getEbayCountry().getCurrency(), search.getUser());
		search.setMinPrice((int)(search.getMinPrice() * rate));
		search.setMaxPrice((int)(search.getMaxPrice() * rate));
	}
	
	/**
	 * Changes ebay country of search
	 * @param search
	 * @param ebayCountry
	 */
	public void changeSearchEbayCountry(Search search, EbayCountry ebayCountry) {
		if (search.getEbayCountry() != ebayCountry) {
			search.setCategories(new ArrayList<Category>());
			search.setEbayCountry(ebayCountry);
		}
	}
	
	/**
	 * Saves and enables new search
	 * @param search
	 */
	public void saveSearch(Search search) {
		searchRepository.save(search);
	}
	
	
	/**
	 * Checks whether there are any disabled searches
	 * @return
	 */
	public Boolean checkDisabled() {
		return searchRepository.checkDisabled();
	}
	
	/**
	 * Updates search upon change
	 * @param searchOriginal
	 * @param searchUpdated
	 * @return
	 */
	public Search updateSearch(Search searchOriginal, Search searchUpdated) {
		searchUpdated.setBlacklistItems(searchOriginal.getBlacklistItems());
		searchUpdated.setBlacklistSellers(searchOriginal.getBlacklistSellers());
		searchUpdated.setEnabled(true);
		searchUpdated.setActive(searchOriginal.getActive());
		searchUpdated.setRemoved(false);
		searchUpdated.setUser(searchOriginal.getUser());
		searchUpdated.setItems(searchOriginal.getItems());
		searchUpdated.setSearchPreferences(searchOriginal.getSearchPreferences());
		searchUpdated.setPhoneNotificationSettings(searchOriginal.getPhoneNotificationSettings());
		setConvertedValues(userService.getLoggedUser(), searchUpdated);
		if (!searchUpdated.getSellers().isEmpty()) {
			searchUpdated.setSellerSearch(true);
		} else {
			searchUpdated.setSellerSearch(false);
		}
		return searchUpdated;
	}
	
	public void updatePreferences(Search search) {
		search.getSearchPreferences().getListingTypes().clear();
		for (ListingType lt : search.getListingTypes()) {
			search.getSearchPreferences().getListingTypes().add(lt);
		}
		searchPreferencesRepository.save(search.getSearchPreferences());
		
	}
	
	/**
	 * Returns page of search's items to display
	 * @param search
	 * @param page
	 * @return
	 */
	public Page<EbayItem> getPageForSearchDisplay(Search search, Integer page) {
		Page<EbayItem> itemPage = getSearchItems(search, page);
		currencyService.convertItems(itemPage.getContent(), search, search.getUser());
		return itemPage;
	}
	
	/**
	 * Returns given page for items in given search
	 * @param search
	 * @param page
	 * @return
	 */
	public Page<EbayItem> getSearchItems(Search search, Integer page) {
		return itemRepository.findAllBySearch(search, getPageableForItems(search, page), search.getSearchPreferences().getListingTypes());
	}
	
	/**
	 * Returns Page of search item blacklist objects, used for viewing blacklisted items in specific search
	 * @param search
	 * @param page
	 * @return
	 */
	public Page<SearchItemBlacklist> getBlacklistedItems(Search search, Integer page) {
		return itemBanRepository.findBlacklistItemsBySearch(search, search.getSearchPreferences().getItemBanCount().getPageable(page));
	}
	
	/**
	 * Returns Page of search seller blacklist objects, used for viewing blacklisted items in specific search
	 * @param search
	 * @param page
	 * @return
	 */
	public Page<SearchSellerBlacklist> getBlacklistedSellers(Search search, Integer page) {
		return sellerBanRepository.findBlacklistItemsBySearch(search, search.getSearchPreferences().getSellerBanCount().getPageable(page));
	}
	
	/**
	 * Prepares new search
	 * @return
	 */
	public Search getNewSearch() {
		User u = userService.getLoggedUser();
		Search search = new Search();
		search.setEbayCountry(u.getUserPreferences().getEbayCountry());
		return search;
	}
	
	@Scheduled(fixedDelay = 60000)	
	private void refresh() {
		//TODO
//		ebayCountryService.checkVersionAll();
		List<Search> searches = searchRepository.findAllToRefresh();
		Date now = new Date();
		for (Search search : searches) {
			Long seconds = (now.getTime() - search.getLastRefreshed().getTime())/1000;
			if (seconds > search.getRefreshTime() * 60) {
				try {
					System.out.println("updating " + search.getName());
					Search searchToUpdate = searchRepository.getSearchToRefresh(search.getId());
					searchFindItems(searchToUpdate, true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Set search preferences if there are none
	 * @param search
	 */
	public void setPreferences(Search search) {
		SearchPreferences sp = search.getSearchPreferences();
		if (sp == null) {
			sp = new SearchPreferences();
			DisplayCount itemCount = new DisplayCount(9);
			DisplayCount itemBanCount = new DisplayCount(36);
			DisplayCount sellerBanCount = new DisplayCount(36);
			sp.setItemCount(itemCount);
			sp.setItemBanCount(itemBanCount);
			sp.setSellerBanCount(sellerBanCount);
			for (ListingType lt : search.getListingTypes()) {
				sp.getListingTypes().add(lt);
			}
			displayCountRepository.saveAll(Arrays.asList(itemCount, itemBanCount, sellerBanCount));
			searchPreferencesRepository.save(sp);
			search.setSearchPreferences(sp);
			searchRepository.save(search);
		}
	}
	
	/**
	 * Set search notifications preferences if there are none
	 * @param search
	 */
	public void setNotification(Search search) {
		if (search.getPhoneNotificationSettings() == null) {
			User user = userService.getLoggedUser();
			
			SearchPhoneNotificationSettings phoneNotificationSettings = new SearchPhoneNotificationSettings();
			phoneNotificationSettings.setPhoneDefaults(user.getUserPreferences().getPhoneNotificationsDefaults());
			
			searchPhoneNotificationRepository.save(phoneNotificationSettings);
			search.setPhoneNotificationSettings(phoneNotificationSettings);
			searchRepository.save(search);
		}
	}
	
	/**
	 * 
	 * @param search
	 * @param page
	 * @return
	 */
	private Pageable getPageableForItems(Search search, Integer page) {
		SearchPreferences sp = search.getSearchPreferences();
		if (sp.getItemCount().getDisplayAll()) {
			return PageRequest.of(page, Integer.MAX_VALUE, sp.getDirection(), sp.getSortBy().getSort());
		}
		return PageRequest.of(page, sp.getItemCount().getPageSize(), sp.getDirection(), sp.getSortBy().getSort());
	}
	
	/**
	 * Updates search notification settings
	 * @param phoneNotificationSettings
	 * @param search
	 */
	public void updateSearchNotificationSettings(SearchPhoneNotificationSettings phoneNotificationSettings, Search search) {
		SearchPhoneNotificationSettings settings = search.getPhoneNotificationSettings();
		settings.setNotificationsEnabled(phoneNotificationSettings.getNotificationsEnabled());
		settings.setNotificationsAuction(phoneNotificationSettings.getNotificationsAuction());
		settings.setNotificationsAuctionBuyItNow(phoneNotificationSettings.getNotificationsAuctionBuyItNow());
		settings.setNotificationsBuyItNow(phoneNotificationSettings.getNotificationsBuyItNow());
		search.setPhoneNotificationSettings(settings);
		searchPhoneNotificationRepository.save(settings);
		searchRepository.save(search);
	}
	
	/**
	 * Updates search preferences
	 * @param search
	 */
	public void updateItemDisplayPreferences(Search search, String sort, String dir, Integer size, Boolean all, Boolean changeListings, Boolean auction, Boolean auctionWithBIN, Boolean fixedPrice) {
		SearchPreferences sp = search.getSearchPreferences();
		if (sort != null) {
			SortEnum se = SortEnum.getSortEnum(sort);
			if (se != null) {
				sp.setSortBy(se);
			}
		}
		if (dir != null) {
			sp.setDirection(Direction.fromString(dir));
		}
		if(changeListings) {
			sp.setListingTypes(auction, auctionWithBIN, fixedPrice);
		}
		userService.updateDisplayCount(sp.getItemCount(), size, all);
		searchPreferencesRepository.save(sp);
	}
	
	/**
	 * Sets other default settings --- preferences and phone notifications after search has been successfuly created
	 * @param search
	 */
	public void setDefaultSettings(Search search) {
		setPreferences(search);
		setNotification(search);
	}
	
	/**
	 * Method creates FindItemsAdvancedRequest request object and passes it to ebay service layer where it gets sent to ebay api
	 * All the filters are set here by using helper class SearchRequestMaker based on specified details in creation/updating of search
	 * @param search
	 * @param automatic
	 */
	public void searchFindItems(Search search, Boolean automatic) throws Exception {
		
		FindItemsAdvancedRequest request = SearchRequestMaker.createRequest(search);
		List<FindItemsAdvancedResponse> responses = new ArrayList<>();
	
		fixedPriceListings(request, responses, search, automatic);
		auctionListings(request, responses, search, automatic);
		
		itemService.processEbayResponse(responses, search, automatic);
	}
	
	/**
	 * Creates requests for fixed price listings by adding keywords from keyword group
	 * @param request
	 * @param responses
	 * @param search
	 * @param automatic
	 * @param listingType
	 * @throws Exception
	 */
	private void fixedPriceListings(FindItemsAdvancedRequest request, List<FindItemsAdvancedResponse> responses, Search search, Boolean automatic) throws Exception {
		if (SearchRequestMaker.isFixedPriceSelected(search)) {
			ListingType listingType = ListingType.FIXED_PRICE;
			request.getItemFilter().add(SearchRequestMaker.listingTypeFixedPrice(search));
			//time filter for fixed price listings(so reduce too many results)
//			request.getItemFilter().add(SearchRequestMaker.startTimeFilter(search));
			if (search.getKeywordsQueries().size() == 0) {
				FindItemsAdvancedResponse response = sendRequestAndGetResponse(request, search, automatic, listingType);
				responses.add(response);
			} else {
				for (String keywords : search.getKeywordsQueries()) {
					request.setKeywords(keywords);
					FindItemsAdvancedResponse response = sendRequestAndGetResponse(request, search, automatic, listingType);
					responses.add(response);
				}
			}
			//remove starting filter + listing type filter
//			request.getItemFilter().remove(request.getItemFilter().size() - 1);
			request.getItemFilter().remove(request.getItemFilter().size() - 1);
		}
	}
	
	/**
	 * Creates requests for auction listings by adding keywords from keyword group
	 * @param request
	 * @param responses
	 * @param search
	 * @param automatic
	 * @param listingType
	 * @throws Exception
	 */
	private void auctionListings(FindItemsAdvancedRequest request, List<FindItemsAdvancedResponse> responses, Search search, Boolean automatic) throws Exception {
		if (SearchRequestMaker.isAuctionSelected(search)) {
			ListingType listingType = ListingType.AUCTION;
			request.getItemFilter().add(SearchRequestMaker.listingTypeAuction(search));
			request.getItemFilter().add(SearchRequestMaker.bidsFilter(search, "max"));
			request.getItemFilter().add(SearchRequestMaker.bidsFilter(search, "min"));
			
			if (search.getKeywordsQueries().size() == 0) {
				FindItemsAdvancedResponse response = sendRequestAndGetResponse(request, search, automatic, listingType);
				responses.add(response);
			} else {
				for (String keywords : search.getKeywordsQueries()) {
					request.setKeywords(keywords);
					FindItemsAdvancedResponse response = sendRequestAndGetResponse(request, search, automatic, listingType);
					responses.add(response);
				}
			}
		}
	}
	
	/**
	 * Parses and processes keywords to split them into groups
	 * @param keywords
	 * @throws Exception
	 */
	public void processKeywords(Search search) throws Exception{
		Expression<String> parsedKeywords = ExprParser.parse(search.getKeywordsInput());
		Expression<String> dnfKeywords = RuleSet.toDNF(parsedKeywords);
		Set<String> keywords = new HashSet<>();
		if(dnfKeywords.getExprType() == "variable") {
			keywords.add(dnfKeywords.toString());
		} else if (dnfKeywords.getExprType() == "and") {
			String result = parseConjunction(dnfKeywords);
			if (result.length() > KEYWORDS_LENGTH_MAX) {
				throw new KeywordLengthViolationException(KEYWORDS_LENGTH_MAX_ERROR);
			}
			keywords.add(result);
		} else {
			for( Expression<String> keywordQuery : dnfKeywords.getChildren()) {
				if (keywordQuery.getExprType() == "or") {
					keywords.add(parseConjunction(dnfKeywords));
					break;
				} else if (keywordQuery.getExprType() == "variable") {
					keywords.add(keywordQuery + "");
				} else {
					String result = parseConjunction(keywordQuery);
					if (result.length() > KEYWORDS_LENGTH_MAX) {
						throw new KeywordLengthViolationException(KEYWORDS_LENGTH_MAX_ERROR);
					}
					keywords.add(result);
				}
			}
		}
		search.setKeywordsQueries(keywords);
	}
	
	/**
	 * Parses conjunction into literals
	 * @param exp
	 * @return
	 * @throws Exception
	 */
	private String parseConjunction(Expression<String> exp) throws Exception{
		StringBuilder builder = new StringBuilder();
		for (Expression<String> keyword : exp.getChildren()) {
    		if(builder.length() > 0) {
    			builder.append(" ");
			}
    		String kw = keyword.getAllK().toArray()[0].toString();
    		if (kw.length() > KEYWORD_LENGTH_MAX) {
    			throw new KeywordLengthViolationException(KEYWORD_LENGTH_MAX_ERROR);
    		}
    		if (keyword.getExprType() == "variable") {
    			builder.append(kw);
    		} else if (keyword.getExprType() == "not") {
    			builder.append("-" + kw);
    		}
    	}
		return builder.toString();
	}
	
	
	
	/**
	 * Sends request to eBay API and returns Response object or throws exception if error occurs
	 * @param request
	 * @param search
	 * @param automatic
	 * @param listingType
	 * @return
	 * @throws Exception
	 */
	private FindItemsAdvancedResponse sendRequestAndGetResponse(FindItemsAdvancedRequest request, Search search, Boolean automatic, ListingType listingType) throws Exception{
		FindItemsAdvancedResponse response = ebayService.sendFindItemsAdvancedRequest(request, search);	
		if (response.getPaginationOutput().getTotalEntries() > MAX_ITEMS_FOUND) {
			if (!automatic) {
				throw new TooManyItemsException("Too many results in " + listingType.getName() + " items, please specify your search query more." + 
												"\nSearch keywords:\"" + request.getKeywords() + "\"" +
												"\nMax items = " + MAX_ITEMS_FOUND + ", Found items = " + response.getPaginationOutput().getTotalEntries());
			} else {
				enableSearch(search, false);
			}
		}
		return response;
	}
	
}