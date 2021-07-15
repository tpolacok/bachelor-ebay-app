package tomaspolacok.bachelor.application.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;

import lombok.Data;

@Data
@Embeddable
public class CategoryId implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@ManyToOne
	private EbayCountry ebayCountry;
	
	@Column(name = "ebay_category_id")
	private String id;

	@Override
	public String toString() {
		return ebayCountry.getSiteId() + " " + id;
	}
	
	
}