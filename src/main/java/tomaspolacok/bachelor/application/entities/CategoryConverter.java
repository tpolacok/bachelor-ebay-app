package tomaspolacok.bachelor.application.entities;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import tomaspolacok.bachelor.application.repositories.CategoryRepository;
import tomaspolacok.bachelor.application.repositories.EbayCountryRepository;

@Component
public class CategoryConverter implements Converter<String, Category> {
	
	@Autowired
	EbayCountryRepository ebayCountryRepository;
	@Autowired
	CategoryRepository categoryRepository;
	
	@Override
	public Category convert(String id) {
		String s[] = id.split(" ");
		CategoryId ci = new CategoryId();
		ci.setEbayCountry(ebayCountryRepository.getOne(Integer.parseInt(s[0])));
		ci.setId(s[1]);
		return categoryRepository.getOne(ci);
	}
}
