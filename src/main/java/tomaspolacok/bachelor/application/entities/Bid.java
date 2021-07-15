package tomaspolacok.bachelor.application.entities;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

@Data
public class Bid {
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Column
	private int rating;
	
	@Column
	Boolean automatic;
	
	@Column
	private Double price;
	
}
