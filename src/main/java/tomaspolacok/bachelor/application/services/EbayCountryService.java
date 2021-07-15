package tomaspolacok.bachelor.application.services;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tomaspolacok.bachelor.application.entities.EbayCountry;
import tomaspolacok.bachelor.application.repositories.EbayCountryRepository;

@Service
public class EbayCountryService {

	@Autowired
	EbayCountryRepository ebayCountryRepository;
	
	@Autowired
	CategoryService categoryService;
	
	private final int CATEGORY_REFRESH_TIME = 1440;
	
	/**
	 * Checks and updates category versions of all eBay countries and tries to update if it's needed
	 */
	public void checkVersionAll() {
		List<EbayCountry> ebayCountries = ebayCountryRepository.findAll();
		Date now = new Date();
		for(EbayCountry ebayCountry : ebayCountries) {
			//Italy(101) categories not work
			if (ebayCountry.getSiteId() != 101) {
				if (ebayCountry.getLastRefreshed() == null || (now.getTime() - ebayCountry.getLastRefreshed().getTime()) / 1000 > CATEGORY_REFRESH_TIME * 60 ) {
					try {
						ebayCountry.setCategoryEnabled(true);
						categoryService.checkCategoryVersion(ebayCountry);
					} catch (Exception e) {
						e.printStackTrace();
						ebayCountry.setCategoryEnabled(false);
					}
					ebayCountry.setLastRefreshed(new Date());
				}
			}
		}
		ebayCountryRepository.saveAll(ebayCountries);
	}
}