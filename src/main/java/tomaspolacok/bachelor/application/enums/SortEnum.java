package tomaspolacok.bachelor.application.enums;

public enum SortEnum {
	CURRENT_PRICE("sellingStatus.currentPrice"),
	BID_COUNT("sellingStatus.bidCount"),
	BUY_IT_NOW("buyItNowPrice"),
	SHIPPING_COST("shipping.shippingCost"),
	END_TIME("endTime");
	
	private String sort;
	
	SortEnum(String sort) {
		this.sort = sort;
	}
	
	public String getSort() {
		return sort;
	}
	
	public static SortEnum getSortEnum(String sort) {
		for(SortEnum se : values()){
	        if( se.sort.equals(sort)){
	            return se;
	        }
	    }
	    return null;
	}
}
