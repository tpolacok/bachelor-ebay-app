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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import tomaspolacok.bachelor.application.entities.UserPreferences;
import tomaspolacok.bachelor.application.enums.ListingType;
import tomaspolacok.bachelor.application.helper.PasswordChange;
import tomaspolacok.bachelor.application.repositories.CountryRepository;
import tomaspolacok.bachelor.application.repositories.CurrencyRepository;
import tomaspolacok.bachelor.application.repositories.EbayCountryRepository;
import tomaspolacok.bachelor.application.services.UserService;

@Controller
public class SettingsController {
	
	@Autowired
	CountryRepository countryRepository;
	@Autowired
	CurrencyRepository currencyRepository;
	@Autowired
	EbayCountryRepository ebayCountryRepository;
	@Autowired
	UserService userService;
	
	
	/**
	 * Post request for adding phone number
	 * @param phoneNumber
	 * @return
	 */
	@PostMapping(value={"/phone/{phoneNumber}"})
	public @ResponseBody String phoneNumber(@PathVariable String phoneNumber) {
		userService.addPhoneNumber(phoneNumber);
		return "success";
	}
	
	/**
	 * Post request for activating phone number
	 * @param activationLink
	 * @return
	 */
	@PostMapping(value={"/phone/activate/{activationLink}"})
	public @ResponseBody String activatePhoneNumber(@PathVariable String activationLink) {
		try {
			userService.activatePhoneNumber(activationLink);
		} catch (Exception e) {
			return "failure";
		}
		return "success";
	}
	
	/**
	 * Get request for settings to change user preferences or password
	 * @param session
	 * @return
	 */
	@GetMapping(value={"/settings"})
	public ModelAndView settings(HttpSession session) {
		ModelAndView modelAndView = new ModelAndView();
		
		modelAndView.addObject("user", userService.getLoggedUser());
		modelAndView.addObject("userPreferences", userService.getUserPreferences());
		modelAndView.addObject("password", new PasswordChange());
		setUpModelAndView(modelAndView);
		modelAndView.setViewName("settings");
		return modelAndView;
	}
	
	/**
	 * Post request to change user's prefereneces
	 * @param preferences
	 * @param bindingResult
	 * @param session
	 * @return
	 */
	@PostMapping(value={"/user/preferences"})
	public ModelAndView preferences(@Valid @ModelAttribute(value="userPreferences") UserPreferences preferences, BindingResult bindingResult, HttpSession session) {
		ModelAndView modelAndView = new ModelAndView();
		if (bindingResult.hasErrors()) {
			setUpModelAndView(modelAndView);
			modelAndView.addObject("password", new PasswordChange());
			modelAndView.addObject("user", userService.getLoggedUser());
			modelAndView.setViewName("settings");
		} else {
			userService.saveUserPreferences(preferences, session);
			modelAndView.setViewName("redirect:/settings?preferencesSuccess=true");
		}
		return modelAndView;
	}
	
	/**
	 * Post request for changing password
	 * @param passwordChange
	 * @param bindingResult
	 * @param session
	 * @return
	 */
	@PostMapping(value={"/user/password"})
	private ModelAndView changePassword(@Valid @ModelAttribute(value="password") PasswordChange passwordChange, BindingResult bindingResult) {
		ModelAndView modelAndView = new ModelAndView();
		if (passwordChange.getPasswordNew().compareTo(passwordChange.getPasswordNewSecond()) != 0) {
			bindingResult.rejectValue("passwordNewSecond", "error.password", "New passwords must match.");
		}
		if (!userService.checkPassword(passwordChange.getPasswordOld())) {
			bindingResult.rejectValue("passwordOld", "error.password", "Invalid password.");
		}
		if (bindingResult.hasErrors()) {
			setUpModelAndView(modelAndView);
			modelAndView.addObject("userPreferences", userService.getUserPreferences());
			modelAndView.addObject("user", userService.getLoggedUser());
			modelAndView.setViewName("settings");			
		} else {
			userService.changePassword(passwordChange.getPasswordNew());
			modelAndView.setViewName("redirect:/settings?passwordSuccess=true");
		}
		return modelAndView;
	}
	
	/**
	 * Get request for privacy site
	 * @return
	 */
	@GetMapping(value= {"/privacy"})
	private ModelAndView privacyPolicy() {
		return new ModelAndView("privacy");
	}
	
	/**
	 * Adds all the needed objects to model
	 * @param modelAndView
	 * @param session
	 * @return
	 */
	private ModelAndView setUpModelAndView(ModelAndView modelAndView) {
		modelAndView.addObject("ebayCountries", ebayCountryRepository.findAll());
		modelAndView.addObject("countries", countryRepository.findAll());
		modelAndView.addObject("listingtypesall", Arrays.asList(ListingType.values()));
		modelAndView.addObject("currencies", currencyRepository.findAll());
		return modelAndView;
	}
	
}
