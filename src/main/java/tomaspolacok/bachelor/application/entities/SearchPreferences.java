package tomaspolacok.bachelor.application.entities;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import org.springframework.data.domain.Sort.Direction;

import lombok.Data;
import tomaspolacok.bachelor.application.enums.ListingType;
import tomaspolacok.bachelor.application.enums.SortEnum;

@Data
@Entity
public class SearchPreferences {
	
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
	
	@Column
	private int pageSize = 9;
	
	@Enumerated(EnumType.STRING)
	private Direction direction = Direction.ASC;
	
	@Enumerated(EnumType.STRING)
	private SortEnum sortBy = SortEnum.END_TIME;
	
	@Column
	private Boolean displayAll = false;
	
	@ElementCollection
	@Enumerated(EnumType.STRING)
	private Set<ListingType> listingTypes = new HashSet<>();
	
	@OneToOne
	private DisplayCount itemCount;
	
	@OneToOne
	private DisplayCount sellerBanCount;
	
	@OneToOne
	private DisplayCount itemBanCount;
	
	public void setListingTypes(Boolean auction, Boolean auctionWithBIN, Boolean fixedPrice) {
		if (auction == null) {
			listingTypes.remove(ListingType.AUCTION);
		} else {
			listingTypes.add(ListingType.AUCTION);
		}
		if (auctionWithBIN == null) {
			listingTypes.remove(ListingType.AUCTION_BUY_IT_NOW);
		} else {
			listingTypes.add(ListingType.AUCTION_BUY_IT_NOW);
		}
		if (fixedPrice == null) {
			listingTypes.remove(ListingType.FIXED_PRICE);
		} else {
			listingTypes.add(ListingType.FIXED_PRICE);
		}
	}

}
