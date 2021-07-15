package tomaspolacok.bachelor.application.enums;

public enum ShippingType {
	UNKNOWN("Unknown", "Unknown"),
	ACTIVE("Calculated","The calculated shipping model: The posted cost of shipping is based on the buyer-selected shipping service, chosen by the buyer from the different shipping services offered by the seller. The shipping costs are calculated by eBay and the shipping carrier, based on the buyer''s address. Any packaging and handling costs established by the seller are automatically rolled into the total."),
	CALCULATED_DOMESTIC_FLAT_INTERNATIONAL("CalculatedDomesticFlatInternational","The seller specified one or more calculated domestic shipping services and one or more flat international shipping services."),
	FLAT("Flat","The flat-rate shipping model: The seller establishes the cost of shipping and any shipping insurance, regardless of what any buyer-selected shipping service might charge the seller."),
	FLAT_DOMESTIC_CALCULATED_INTERNATIONAL("FlatDomesticCalculatedInternational","The seller specified one or more flat domestic shipping services and one or more calculated international shipping services."),
	FREE("Free","Free is used when the seller has declared that shipping is free for the buyer."),
	FREE_PICKUP("FreePickup","No shipping available, the buyer must pick up the item from the seller."),
	FREIGHT("Freight","The freight shipping model: the cost of shipping is determined by a third party, FreightQuote.com, based on the buyer's address (postal code)."),
	FREIGHT_FLAT("FreightFlat","The flat rate shipping model: the seller establishes the cost of freight shipping and freight insurance, regardless of what any buyer-selected shipping service might charge the seller."),
	NOTSPECIFIED("NotSpecified","The seller did not specify the shipping type.");
	
	private final String type;
	private final String description;
	
	ShippingType(String type, String description) {
		this.type = type;
		this.description = description;
	}
	
	public String getType() {
		return type;
	}
	
	public String getDescription() {
		return description;
	}
	
	public static ShippingType getShippingType(String type) {
		for(ShippingType st : values()){
	        if( st.type.equals(type)){
	            return st;
	        }
	    }
	    return UNKNOWN;
	}
}
