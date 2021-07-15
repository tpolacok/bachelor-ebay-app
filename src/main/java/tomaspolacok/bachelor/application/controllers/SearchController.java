package tomaspolacok.bachelor.application.controllers;

import java.util.Arrays;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import tomaspolacok.bachelor.application.entities.EbayCountry;
import tomaspolacok.bachelor.application.entities.Search;
import tomaspolacok.bachelor.application.entities.SearchPhoneNotificationSettings;
import tomaspolacok.bachelor.application.entities.User;
import tomaspolacok.bachelor.application.enums.ListingType;
import tomaspolacok.bachelor.application.exceptions.KeywordLengthViolationException;
import tomaspolacok.bachelor.application.exceptions.TooManyItemsException;
import tomaspolacok.bachelor.application.repositories.CountryRepository;
import tomaspolacok.bachelor.application.repositories.EbayCountryRepository;
import tomaspolacok.bachelor.application.repositories.EbayItemRepository;
import tomaspolacok.bachelor.application.repositories.SearchRepository;
import tomaspolacok.bachelor.application.services.CategoryService;
import tomaspolacok.bachelor.application.services.CurrencyService;
import tomaspolacok.bachelor.application.services.SearchService;
import tomaspolacok.bachelor.application.services.UserService;

@Controller
public class SearchController {
	
	@Autowired
	EbayCountryRepository ebayCountryRepository;
	@Autowired
	CountryRepository countryRepository;
	@Autowired
	SearchRepository searchRepository;
	@Autowired
	EbayItemRepository itemRepository;
	@Autowired
	UserService userService;
	@Autowired
	SearchService searchService;
	@Autowired
	CategoryService categoryService;
	@Autowired
	CurrencyService currencyService;
	
	/**
	 * Returns page of user's searches, where he can view/edit them and also add new searches
	 * @param pageable
	 * @return
	 */
	@GetMapping(value={"/search"})
	public ModelAndView search( @RequestParam(value = "size", required=false) Integer size,
								@RequestParam(value = "page", defaultValue="0", required=false) Integer page,
								@RequestParam(value = "all", required=false) Boolean all,
								HttpSession session){
        ModelAndView modelAndView = new ModelAndView();
        User user = userService.getLoggedUser();
        userService.updateDisplayCount(user.getUserPreferences().getSearchCount(), size, all);
        modelAndView.addObject("page", searchService.getAll(page));
        modelAndView.addObject("disabled", searchService.checkDisabled());
        modelAndView.addObject("displayCount", user.getUserPreferences().getSearchCount());
        modelAndView.addObject("sizes", Arrays.asList(6,9,12));
        modelAndView.setViewName("search");
        return modelAndView;
    }
	
	/**
	 * Returns page of details of specific search of user (items it contains)
	 * @param searchId
	 * @param pageable
	 * @param sort
	 * @return
	 */
	@GetMapping(value= {"/search/view/{searchId}"})
	public ModelAndView searchView(@PathVariable Long searchId,
								@RequestParam(value = "sort", required=false) String sort,
								@RequestParam(value = "dir", required=false) String dir,
								@RequestParam(value = "size", required=false) Integer size,
								@RequestParam(value = "page", defaultValue="0", required=false) Integer page,
								@RequestParam(value = "all", required=false) Boolean all,
								@RequestParam(value = "listing", defaultValue="false") Boolean changeListings,
								@RequestParam(value = "AUCTION_BUY_IT_NOW", required=false) Boolean auctionWithBIN,
								@RequestParam(value = "AUCTION", required=false) Boolean auction,
								@RequestParam(value = "FIXED_PRICE", required=false) Boolean fixedPrice
								) {
		ModelAndView modelAndView = new ModelAndView();
		Search search;
		try {
			search = searchService.getSearch(searchId);
		} catch (Exception e) {
			modelAndView.setViewName("redirect:/error");
	        return modelAndView;
		}
		searchService.updateItemDisplayPreferences(search, sort, dir, size, all, changeListings, auction, auctionWithBIN, fixedPrice);
		modelAndView.addObject("sizes", Arrays.asList(6,9,12));
		modelAndView.addObject("listingTypes", search.getListingTypes());
		modelAndView.addObject("search", search);
		modelAndView.addObject("dir", search.getSearchPreferences().getDirection().toString());
        modelAndView.addObject("page", searchService.getPageForSearchDisplay(search, page));
        modelAndView.setViewName("searchView");
        return modelAndView;
		
	}
	
	/**
	 * Returns page with a form for creation of new search
	 * @param search
	 * @return
	 */
	@GetMapping(value={"/search/create"})
	public ModelAndView searchCreateGet(@RequestParam(value = "ebaySiteIdDog", required = false) Integer ebaySiteId, @ModelAttribute(value="search") Search searchNew){
		ModelAndView modelAndView = new ModelAndView();
		EbayCountry ebayCountry;
		Search search;
		if (ebaySiteId == null) {
			ebayCountry = userService.getLoggedUser().getUserPreferences().getEbayCountry();
			search = new Search();
			search.setEbayCountry(ebayCountry);
			userService.setDefaults(search);
		} else {
			ebayCountry = ebayCountryRepository
							.findById(ebaySiteId)
							.orElse(null);
			search = searchNew;
			search.setEbayCountry(ebayCountry);
			System.out.println("here");
			if (ebayCountry == null) {
				modelAndView.setViewName("error");
				modelAndView.addObject("errorMessage", "Ebay country not found.");
				return modelAndView;
			}
		}
		modelAndView.addObject("search", search);
		modelAndView = setUpModelAndView(modelAndView, search);
		modelAndView.addObject("update", false);
        modelAndView.setViewName("searchForm");
        return modelAndView;
    }
	
	/**
	 * Returns page with a form for creation of new search when ebay site is changed
	 * @param search
	 * @return
	 */
	@GetMapping(value={"/search/create/{ebaySiteId}"})
	public ModelAndView searchCreateGetEbay(@PathVariable Integer ebaySiteId, @ModelAttribute(value="search") Search search){
		ModelAndView modelAndView = new ModelAndView();
		EbayCountry ebayCountry;
		if (searchRepository.checkExists(search.getId())) {
			modelAndView.setViewName("error");
			modelAndView.addObject("errorMessage", "Error with search");
			return modelAndView;
		}
		ebayCountry = ebayCountryRepository
						.findById(ebaySiteId)
						.orElse(null);
		if (ebayCountry == null) {
			modelAndView.setViewName("error");
			modelAndView.addObject("errorMessage", "Ebay country not found.");
			return modelAndView;
		}
		search.setEbayCountry(ebayCountry);
		search.getCategories().clear();
		modelAndView.addObject("search", search);
		modelAndView = setUpModelAndView(modelAndView, search);
		modelAndView.addObject("update", false);
        modelAndView.setViewName("searchForm");
        return modelAndView;
    }
	
	
	/**
	 * Method to set up all the things needed in search form
	 * @param modelAndView
	 * @param search
	 * @return
	 */
	private ModelAndView setUpModelAndView(ModelAndView modelAndView, Search search) {
		modelAndView.addObject("categories", categoryService.getCategoriesByCountry(search.getEbayCountry()));
		modelAndView.addObject("ebayCountries", ebayCountryRepository.findAll());
		modelAndView.addObject("countries", countryRepository.findAll());
		modelAndView.addObject("listingtypesall", Arrays.asList(ListingType.values()));
		modelAndView.addObject("currency", currencyService.getCurrency(search.getEbayCountry().getCurrency(), userService.getLoggedUser().getUserPreferences().getCurrency()));
		return modelAndView;
	}
	
	/**
	 * Handles verification of posted search, returns errors if there is problem with configuration or if there was problem passing the request created from it(to ebay)
	 * @param search
	 * @param bindingResult
	 * @return
	 */
	@PostMapping(value={"/search/create"})
	public ModelAndView searchCreatePost(@Valid @ModelAttribute(value="search") Search search, BindingResult bindingResult){
		ModelAndView modelAndView = new ModelAndView();
		bindingResult = validate(bindingResult, search);
		if (bindingResult.hasErrors()) {
			modelAndView = setUpModelAndView(modelAndView, search);
			modelAndView.addObject("update", false);
			modelAndView.setViewName("searchForm");
        } else {
        	searchService.initSearch(search);
        	try {
				searchService.searchFindItems(search, false);
				searchService.setDefaultSettings(search);
        	} catch (TooManyItemsException e) {
        		modelAndView.addObject("errorMessage", e.getMessage());
        		modelAndView = setUpModelAndView(modelAndView, search);
				modelAndView.addObject("update", false);
				modelAndView.setViewName("searchForm");
				return modelAndView;
        	} catch (Exception e) {
        		e.printStackTrace();
				modelAndView.addObject("errorMessage", e.getMessage());
				modelAndView.setViewName("error");
				return modelAndView;
			}
			modelAndView.setViewName("redirect:/search/view/" + search.getId());
        }
        return modelAndView;
    }
	
	/**
	 * Handles deletion of search
	 * @param searchId
	 * @return
	 */
	@PostMapping("/search/delete/{searchId}")
	public ModelAndView searchDelete(@PathVariable Long searchId) {
	    ModelAndView modelAndView = new ModelAndView();		
	    try {
			searchService.removeSearch(searchId);
		} catch (Exception e) {
			modelAndView.setViewName("redirect:/error");
	        return modelAndView;
		}
        modelAndView.setViewName("redirect:/search");
	    return modelAndView;
	}
	
	/**
	 * Handles activation/ deactivation of search
	 * @param searchId
	 * @return
	 */
	@PostMapping("/search/activate/{searchId}")
	public ModelAndView searchActivate(@PathVariable Long searchId, @RequestParam(value = "in", defaultValue="false", required=false) Boolean inView) {
		ModelAndView modelAndView = new ModelAndView();
	    try {
			searchService.activateSearch(searchId);
		} catch (Exception e) {
			modelAndView.setViewName("redirect:/error");
	        return modelAndView;
		}
	    if(inView) {
	    	modelAndView.setViewName("redirect:/search/view/" + searchId);
	    } else {
	    	modelAndView.setViewName("redirect:/search");
	    }
	    return modelAndView;
	}
	
	/**
	 * Returns page with form to edit already existing search
	 * @param searchId
	 * @return
	 */
	@GetMapping("/search/edit/{searchId}")
	public ModelAndView searchEdit(@PathVariable Long searchId, @RequestParam(value = "ebaySiteId", required = false) Integer ebaySiteId) {
		ModelAndView modelAndView = new ModelAndView();
	    Search search;
		try {
			search = searchService.getSearch(searchId);
		} catch (Exception e) {
			modelAndView.setViewName("redirect:/error");
	        return modelAndView;
		}
		EbayCountry ebayCountry;
		if (ebaySiteId != null) {
			ebayCountry = ebayCountryRepository
							.findById(ebaySiteId)
							.orElse(null);
			if (ebayCountry == null) {
				modelAndView.setViewName("error");
				modelAndView.addObject("errorMessage", "Ebay country not found.");
			}
			searchService.changeSearchEbayCountry(search, ebayCountry);
		}
		modelAndView.addObject("search", search);
		searchService.prepareSearchForUpdate(search);
		modelAndView = setUpModelAndView(modelAndView, search);
		modelAndView.addObject("update", true);
        modelAndView.setViewName("searchForm");
	    return modelAndView;
	}
	
	/**
	 * Handles validation of edited search
	 * @param searchId
	 * @param search
	 * @param bindingResult
	 * @return
	 */
	@PostMapping("/search/edit/{searchId}")
	public ModelAndView searchSave(@PathVariable Long searchId, @Valid @ModelAttribute(value="search") Search search, BindingResult bindingResult) {
		ModelAndView modelAndView = new ModelAndView();
		Search searchTest;
		try {
			searchTest = searchService.getSearch(searchId);
			if (searchId.compareTo(search.getId()) != 0 ) {
				throw new Exception();
			}
		} catch (Exception e) {
			e.printStackTrace();
			modelAndView.setViewName("redirect:/error");
	        return modelAndView;
		}
		bindingResult = validate(bindingResult, search);
	    if (bindingResult.hasErrors()) {
	    	modelAndView = setUpModelAndView(modelAndView, search);
			modelAndView.addObject("update", true);
			modelAndView.setViewName("searchForm");
        } else {
        	search = searchService.updateSearch(searchTest, search);
        	try {
				searchService.searchFindItems(search, false);
				searchService.updatePreferences(search);
        	} catch (TooManyItemsException e) {
        		modelAndView.addObject("errorMessage", e.getMessage());
        		modelAndView = setUpModelAndView(modelAndView, search);
				modelAndView.addObject("update", true);
				modelAndView.setViewName("searchForm");
				return modelAndView;
        	} catch (Exception e) {
        		e.printStackTrace();
				modelAndView.addObject("errorMessage", e.getMessage());
				modelAndView.setViewName("error");
				return modelAndView;
			}
        	modelAndView.setViewName("redirect:/search/view/" + search.getId());
        }
	    return modelAndView;
	}
	
	/**
	 * Validation of new or edited search
	 * @param bindingResult
	 * @param search
	 * @return
	 */
	private BindingResult validate (BindingResult bindingResult, Search search) {
		if (search.getListingTypes().size() == 0) {
			bindingResult.rejectValue("listingTypes", "error.search", "At least one listing type must be selected.");
		}
		if (search.getMaxBids() <= search.getMinBids() && search.getMaxBids() != 0) {
			bindingResult.rejectValue("maxBids", "error.search", "Max bids can't be <= to min bids.");
		}
		if (search.getMaxPrice() <= search.getMinPrice() && search.getMaxPrice() != 0) {
			bindingResult.rejectValue("maxPrice", "error.search", "Max price can't be <= to min price.");
		}
		if (search.getCategories().size() == 0 && search.getKeywordsInput() == null) {
			bindingResult.rejectValue("categories", "error.search", "There must be atleast 1 category or keyword selected.");
		}
		try {
			searchService.processKeywords(search);
		} catch (KeywordLengthViolationException e) {
			bindingResult.rejectValue("keywordsInput", "error.search", e.getMessage());
		} catch (Exception e) {
			bindingResult.rejectValue("keywordsInput", "error.search", "Parsing of keywords failed");
		}
		
		if ((search.getEndTimeFrom() != null &&  search.getEndTimeTo() != null) && search.getEndTimeTo().before(search.getEndTimeFrom())) {
			bindingResult.rejectValue("endTimeFrom", "error.search", "End time from cant be later than end time to.");
		}
		return bindingResult;
	}
	
	/**
	 * Returns page with blacklisted items belonging to search
	 * @param searchId
	 * @param pageable
	 * @return
	 */
	@GetMapping("/search/blacklist/items/{searchId}")
	public ModelAndView blacklistItems( @PathVariable Long searchId,
										@RequestParam(value = "size", required=false) Integer size,
										@RequestParam(value = "page", defaultValue="0", required=false) Integer page,
										@RequestParam(value = "all", required=false) Boolean all) {
		ModelAndView modelAndView = new ModelAndView();
		Search search;
		try {
			search = searchService.getSearch(searchId);
		} catch (Exception e) {
			modelAndView.setViewName("redirect:/error");
	        return modelAndView;
		}
		userService.updateDisplayCount(search.getSearchPreferences().getItemBanCount(), size, all);
		modelAndView.addObject("page", searchService.getBlacklistedItems(search, page));
		modelAndView.addObject("search", search);
		modelAndView.addObject("sizes", Arrays.asList(12,24,36));
		modelAndView.addObject("displayCount", search.getSearchPreferences().getItemBanCount());
		modelAndView.addObject("global", false);
		modelAndView.setViewName("blacklistItems");
		return modelAndView;
	}
	
	/**
	 * Returns page of blacklisted sellers belonging to search
	 * @param searchId
	 * @param pageable
	 * @return
	 */
	@GetMapping("/search/blacklist/sellers/{searchId}")
	public ModelAndView blacklistSellers(	@PathVariable Long searchId,
											@RequestParam(value = "size", required=false) Integer size,
											@RequestParam(value = "page", defaultValue="0", required=false) Integer page,
											@RequestParam(value = "all", required=false) Boolean all) {
		ModelAndView modelAndView = new ModelAndView();
		Search search;
		try {
			search = searchService.getSearch(searchId);
		} catch (Exception e) {
			modelAndView.setViewName("redirect:/error");
	        return modelAndView;
		}
		userService.updateDisplayCount(search.getSearchPreferences().getSellerBanCount(), size, all);
		modelAndView.addObject("page", searchService.getBlacklistedSellers(search, page));
		modelAndView.addObject("search", search);
		modelAndView.addObject("sizes", Arrays.asList(12,24,36));
		modelAndView.addObject("displayCount", search.getSearchPreferences().getSellerBanCount());
		modelAndView.addObject("global", false);
		
		modelAndView.setViewName("blacklistSellers");
		return modelAndView;
	}
	
	/**
	 * Updates notification settings for search
	 * @param searchId
	 * @param notificationEnabled
	 * @param auctionEnabled
	 * @param auctionBINEnabled
	 * @param fixedEnabled
	 * @return
	 */
	 @PostMapping("/search/notification/{searchId}")
		public @ResponseBody String setNotificationSettings(@PathVariable Long searchId,
												    @RequestParam(value="notifications") Boolean notificationEnabled,
												    @RequestParam(value="auction") Boolean auctionEnabled,
												    @RequestParam(value="buyItNow") Boolean auctionBINEnabled,
												    @RequestParam(value="fixed") Boolean fixedEnabled) {
			Search search;
			try {
				search = searchService.getSearch(searchId);
			} catch (Exception e) {
				return "failure";
			}
			searchService.updateSearchNotificationSettings(new SearchPhoneNotificationSettings(notificationEnabled, auctionEnabled, auctionBINEnabled, fixedEnabled), search);
			return "success";
		}

}
