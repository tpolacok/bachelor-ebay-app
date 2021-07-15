package tomaspolacok.bachelor.application.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;

@Data
@Entity
public class Currency {
	
	@Id
	private String value;
	
	@Column
	private Double rateFromUSD;
	
	public Currency(String currency) {
		value = currency;
	}
	
	public Currency() {
	}
}