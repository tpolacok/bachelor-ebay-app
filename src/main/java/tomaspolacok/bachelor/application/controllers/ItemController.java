package tomaspolacok.bachelor.application.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

import tomaspolacok.bachelor.application.entities.EbayItem;
import tomaspolacok.bachelor.application.entities.PictureCategory;
import tomaspolacok.bachelor.application.entities.Search;
import tomaspolacok.bachelor.application.repositories.PictureCategoryRepository;
import tomaspolacok.bachelor.application.services.CurrencyService;
import tomaspolacok.bachelor.application.services.ItemService;
import tomaspolacok.bachelor.application.services.SearchService;
import tomaspolacok.bachelor.application.services.UserService;

@Controller
public class ItemController {
	
	@Autowired
	PictureCategoryRepository pictureCategoryRepository;
	@Autowired
	ItemService itemService;
	@Autowired
	UserService userService;
	@Autowired
	SearchService searchService;
	@Autowired
	CurrencyService currencyService;
	
	
	/**
	 * Servlet used to return item's detail page as the request comes from search view page
	 * @param itemId
	 * @return
	 */
	@GetMapping(value="/item/view/{searchId}/{itemId}")
	public ModelAndView itemDetailGet(@PathVariable Long itemId, @PathVariable Long searchId) {
		EbayItem item;
		ModelAndView modelAndView = new ModelAndView();
		Search search;
		try {
			search = searchService.getSearch(searchId);
		} catch (Exception e) {
			search = null;
		}
		try {
			item = itemService.getItemForDisplay(itemId, search);
		} catch (Exception e) {
			modelAndView.setViewName("redirect:/error");
			return modelAndView;
		}
		setUpModelAndView(modelAndView, item);
		modelAndView.setViewName("itemDetail");
		return modelAndView;
	}
	
	/**
	 * Servlet used to return item's detail page
	 * @param itemId
	 * @return
	 */
	@GetMapping(value="/item/view/{itemId}")
	public ModelAndView itemDetailGetSingle(@PathVariable Long itemId) {
		EbayItem item;
		ModelAndView modelAndView = new ModelAndView();
		try {
			item = itemService.getItemForDisplay(itemId, null);
		} catch (Exception e) {
			modelAndView.setViewName("redirect:/error");
			return modelAndView;
		}
		setUpModelAndView(modelAndView, item);
		modelAndView.setViewName("itemDetail");
		return modelAndView;
	}
	
	/**
	 * Loads model with some objects needed for displaying item
	 * @param modelAndView
	 * @param item
	 */
	private void setUpModelAndView(ModelAndView modelAndView, EbayItem item) {
		List<PictureCategory> categories = pictureCategoryRepository.findByUser(userService.getLoggedUser());
		List<String> displayPictures = new ArrayList<>();
		modelAndView.addObject("categories", categories);
		modelAndView.addObject("images", displayPictures);
		modelAndView.addObject("item", item);
	}

}
