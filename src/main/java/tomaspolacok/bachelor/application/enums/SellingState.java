package tomaspolacok.bachelor.application.enums;

public enum SellingState {
	UNKNOWN("Unknown", "Unknown"),
	ACTIVE("Active","The listing is still live. It is also possible that the auction has recently ended, but eBay has not completed the final processing (e.g., the high bidder is still being determined)"),
	CANCELLED("Cancelled","The listing has been canceled by either the seller or eBay."),
	ENDED("Ended","The listing has ended and eBay has completed the processing of the sale (if any)."),
	ENDED_WITH_SALES("EndedWithSales","The listing has been ended with sales."),
	ENDED_WITHOUT_SALES("EndedWithoutSales","The listing has been ended without sales.");
	
	private final String type;
	private final String description;
	
	SellingState(String type, String description) {
		this.type = type;
		this.description = description;
	}
	
	public String getType() {
		return type;
	}
	
	public String getDescription() {
		return description;
	}
	
	public static SellingState getSellingState(String type) {
		for(SellingState ss : values()){
	        if( ss.type.equals(type)){
	            return ss;
	        }
	    }
	    return UNKNOWN;
	}
}
