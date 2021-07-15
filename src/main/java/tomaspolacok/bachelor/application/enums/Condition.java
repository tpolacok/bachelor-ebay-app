package tomaspolacok.bachelor.application.enums;

public enum Condition {
	NOT_SPECIFIED(-2, "Not specified", "Not specified"),
	UNKNOWN(-1, "Unknown", "Unknown"),
	NEW(1000, "Brand new", "A brand-new, unused, unopened, unworn, undamaged item. Most categories support this condition (as long as condition is an applicable concept)."),
	NEW_OTHER(1500, "New other", "New other (see details)', 'A brand-new new, unused item with no signs of wear. Packaging may be missing or opened. The item may be a factory second or have defects."),
	NEW_DEFECTS(1750, "New with defects", "New with defects', 'A brand-new, unused, and unworn item. The item may have cosmetic defects, and/or may contain mismarked tags (e.g., incorrect size tags from the manufacturer). Packaging may be missing or opened. The item may be a new factory second or irregular."),
	MANUFACTURER_REFURBISHED(2000, "Manufacturer refurbished", "Manufacturer refurbished', 'An item in excellent condition that has been professionally restored to working order by a manufacturer or manufacturer-approved vendor. The item may or may not be in the original packaging."),
	SELLER_REFURBISHED(2500, "Seller refurbished", "Seller refurbished', 'An item that has been restored to working order by the eBay seller or a third party who is not approved by the manufacturer. This means the seller indicates that the item is in full working order and is in excellent condition. The item may or may not be in original packaging."),
	LIKE_NEW(2750, "Like new", "This enumeration value indicates that the inventory item is in ''like-new'' condition. In other words, the item has been opened, but very lightly used if used at all. This condition typically applies to books or DVDs."),
	USED(3000, "Used", "An item that has been used previously. The item may have some signs of cosmetic wear, but is fully operational and functions as intended. This item may be a floor model or store return that has been used. Most categories support this condition (as long as condition is an applicable concept)."),
	VERY_GOOD(4000, "Very good", "An item that is used but still in very good condition. No obvious damage to the cover or jewel case. No missing or damaged pages or liner notes. The instructions (if applicable) are included in the box. May have very minimal identifying marks on the inside cover. Very minimal wear and tear."),
	GOOD(5000, "Good", "An item in used but good condition. May have minor external damage including scuffs, scratches, or cracks but no holes or tears. For books, liner notes, or instructions, the majority of pages have minimal damage or markings and no missing pages."),
	ACCEPTABLE(6000, "Acceptable", "An item with obvious or significant wear, but still operational. For books, liner notes, or instructions, the item may have some damage to the cover but the integrity is still intact. Instructions and/or box may be missing. For books, possible writing in margins, etc., but no missing pages or anything that would compromise the legibility or understanding of the text."),
	PARTS_NOT_WORKING(7000, "Parts not working", "For parts or not working', 'An item that does not function as intended and is not fully operational. This includes items that are defective in ways that render them difficult to use, items that require service or repair, or items missing essential components. Supported in categories where parts or non-working items are of interest to people who repair or collect related items.");

	private final Integer type;
	private final String state;
	private final String description;
	
	
	Condition(Integer type, String state, String description) {
		this.type = type;
		this.state = state;
		this.description = description;
	}
	
	public String getState() {
		return state;
	}
	
	public Integer getType() {
		return type;
	}
	
	public String getDescription() {
		return description;
	}
	
	public static Condition getCondition(Integer type) {
		for(Condition st : values()){
	        if(st.type.compareTo(type) == 0){
	            return st;
	        }
	    }
	    return UNKNOWN;
	}
}
