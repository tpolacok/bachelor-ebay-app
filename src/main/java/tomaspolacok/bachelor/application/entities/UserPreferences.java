package tomaspolacok.bachelor.application.entities;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.validation.constraints.Min;

import lombok.Data;

@Data
@Entity
public class UserPreferences {
	
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
	
	@ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "user_preference_country", joinColumns = @JoinColumn(name = "user_preference_id"), inverseJoinColumns = @JoinColumn(name = "country_id"))
	private Set<Country> shippingTo = new HashSet<>();

	@ElementCollection
	@Enumerated(EnumType.STRING)
	private List<tomaspolacok.bachelor.application.enums.ListingType> listingTypes;
	
	@ManyToOne
	private EbayCountry ebayCountry;
	
	@Min(2)
	@Column
	private int sessionTime = 3600;
	
	@Column
	private Boolean permanentLogin = false;
	
	@Min(10)
	@Column
	private int refreshTime = 60;
	
	@Column
	private Boolean newOnly = false;
	
	@Column
	private Boolean freeShippingOnly = false;
	
	@Column
	private Boolean returnsAcceptedOnly = false;
	
	@ManyToOne
	private Currency currency = null;
	
	@OneToOne
	private DisplayCount searchCount;
	
	@OneToOne
	private DisplayCount itemBanCount;
	
	@OneToOne
	private DisplayCount sellerBanCount;
	
	@OneToOne
	private DisplayCount archiveCount;
	
	@OneToOne
	private PictureCategory pictureCategory = null;
	
	@OneToOne
	private SearchPhoneNotificationSettings phoneNotificationsDefaults;
	
	
}
