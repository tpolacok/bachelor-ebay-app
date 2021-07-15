package tomaspolacok.bachelor.application.services;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import tomaspolacok.bachelor.application.entities.Currency;
import tomaspolacok.bachelor.application.entities.EbayItem;
import tomaspolacok.bachelor.application.entities.Search;
import tomaspolacok.bachelor.application.entities.User;
import tomaspolacok.bachelor.application.repositories.CurrencyRepository;

@Service
public class CurrencyService {

	public Currency defaultCurrency = new Currency("USD");
	
	private final String resourceUrl = "https://api.exchangeratesapi.io/latest?base=USD";
	
	@Autowired
	CurrencyRepository currencyRepository;
	@Autowired
	UserService userService;
	
	/**
	 * Returns desired currency if it's set, else returns default currency
	 * @param defaultCurrency
	 * @param desiredCurrency
	 * @return
	 */
	public Currency getCurrency(Currency defaultCurrency, Currency desiredCurrency) {
		if (desiredCurrency == null) {
			return defaultCurrency;
		}
		return desiredCurrency;
	}
	
	/**
	 * Updates currency rates from USD to other supported currencies
	 */
	@Scheduled(fixedDelay = 1440000)
	private void updateCurrencyRates() {
		List<Currency> supportedCurrencies = currencyRepository.findAll();
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> response = restTemplate.getForEntity(resourceUrl , String.class);
		ObjectMapper mapper = new ObjectMapper();
		try {
			JsonNode root = mapper.readTree(response.getBody());
			JsonNode rates = root.path("rates");
			for(Currency currency : supportedCurrencies) {
				JsonNode currencyRate = rates.path(currency.getValue());
				
				Currency cur = new Currency(currency.getValue());
				cur.setRateFromUSD(currencyRate.asDouble());
				currencyRepository.save(cur);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Converts list of item prices&currencies to desired currency/ search country currency
	 * @param items
	 * @param search
	 * @param user
	 */
	public List<EbayItem> convertItems(List<EbayItem> items, Search search, User user) {
		Currency desiredCurrency = getCurrency(search.getEbayCountry().getCurrency(), user.getUserPreferences().getCurrency());
		Double rate = getCurrencyRate(defaultCurrency, desiredCurrency);
		for (EbayItem item : items) {
			convertItemWithRate(item, desiredCurrency, rate);
		}
		return items;
	}

	/**
	 * Converts item to given currency by given rate
	 * @param item
	 * @param desiredCurrency
	 * @param rate
	 */
	public void convertItemWithRate(EbayItem item, Currency desiredCurrency, Double rate) {
		if (desiredCurrency != null) {
			item.setCurrency(desiredCurrency);
			item.getSellingStatus().setCurrentPrice(item.getSellingStatus().getCurrentPrice() * rate);
			if (item.getBuyItNow()) {
				item.setBuyItNowPrice(item.getBuyItNowPrice() * rate);
			}
			if (item.getShipping().getShippingCost() != null) {
				item.getShipping().setCurrency(desiredCurrency);
				item.getShipping().setShippingCost(item.getShipping().getShippingCost() * rate);
			}
		}
	}
	
	/**
	 * Returns currency rate for input currency/user currency
	 * @param currency
	 * @param user
	 * @return
	 */
	public Double getCurrencyRate(Currency currency, User user) {
		if (user.getUserPreferences().getCurrency() == null) {
			return 1.0;
		} else {
			return getCurrencyRate(currency, user.getUserPreferences().getCurrency());
		}
	}
	
	/**
	 * Returns currency rate from default currency to desired
	 * @param desiredCurrency
	 * @return
	 */
	public Double getCurrencyRate(Currency desiredCurrency) {
		return getCurrencyRate(defaultCurrency, desiredCurrency);
	}
	
	/**
	 * Returns currency rate between 2 input currencies
	 * @param from
	 * @param to
	 * @return
	 */
	public Double getCurrencyRate(Currency from, Currency to) {
		Currency rateFrom = currencyRepository.findById(from.getValue())
				.orElse(null);
		Currency rateTo = currencyRepository.findById(to.getValue())
				.orElse(null);
		if (rateFrom == null || rateTo == null) return 1.0;
		return rateTo.getRateFromUSD() / rateFrom.getRateFromUSD();
	}

}
