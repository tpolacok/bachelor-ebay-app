package tomaspolacok.bachelor.application.entities;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class IntegerConverter implements Converter<String, Integer> {

	
	@Override
	public Integer convert(String id) {
		Integer i;
		try {
			i = Integer.parseInt(id);
		} catch (Exception e) {
			return 0;
		}
		return i;
	}
}
