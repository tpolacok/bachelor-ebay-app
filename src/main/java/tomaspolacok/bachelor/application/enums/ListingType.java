package tomaspolacok.bachelor.application.enums;

public enum ListingType {
	AUCTION("Auction", "Auction",
			"Competitive-bid online auction format. Buyers engage in competitive bidding, although Buy It Now may be offered as long as no valid bids have been placed."
	),
	AUCTION_BUY_IT_NOW("AuctionWithBIN", "Auction with Buy it now",
			"Same as Auction format, but Buy It Now is enabled."
	),
	FIXED_PRICE("FixedPrice", "Fixed price",
			"Retrieve matching fixed price items only. Excludes Store Inventory format items."
	);

	private final String type;
	private final String name;
	private final String description;
	
	ListingType(String type, String name, String description) {
		this.type = type;
		this.name = name;
		this.description = description;
	}
	
	public String getType() {
		return type;
	}

	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public static ListingType getListingType(String type) {
		for(ListingType lt : values()){
	        if(lt.type.compareTo(type) == 0){
	            return lt;
	        }
	    }
	    return ListingType.FIXED_PRICE;
	}

}
