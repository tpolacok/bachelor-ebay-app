package tomaspolacok.bachelor.application.entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
@Entity
public class Search {
	
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "search_id")
    private Long id;
	
	@Column
	@NotEmpty(message = "Must not be empty.")
	@Length(max = 25)
	private String name;
	
	@Length(max = 200)
	private String description;
	
	@ManyToOne
	private User user;
	
	@ManyToOne
	private EbayCountry ebayCountry;
	
	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinTable(name = "search_categories")
	private List<Category> categories = new ArrayList<>();
	
	@ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "search_country", joinColumns = @JoinColumn(name = "search_id"), inverseJoinColumns = @JoinColumn(name = "country_id"))
	private Set<Country> shippingTo;
	
	@ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "search_items", joinColumns = @JoinColumn(name = "search_id"), inverseJoinColumns = @JoinColumn(name = "ebayitem_id"))
	private List<EbayItem> items;
	
	@Column
	private String keywordsInput;
	
	@Column
	@ElementCollection
	private Set<String> keywordsQueries = new HashSet<>();;
	
	@Column(name = "active")
	private Boolean active;
	
	@Column(name = "enabled")
	private Boolean enabled;

	@Column(name = "removed")
	private Boolean removed;
	
	@Column(name = "end_time_to")
	@DateTimeFormat(pattern = "dd.MM.yyyy")
	private Date endTimeTo;
	
	@Column(name = "end_time_from")
	@DateTimeFormat(pattern = "dd.MM.yyyy")
	private Date endTimeFrom;
	
	@Column(name = "newOnly")
	private Boolean newOnly = false;
	
	@Column(name = "free_shipping")
	private Boolean freeShippingOnly = false;
	
	@Column(name = "returns_accepted")
	private Boolean returnsAcceptedOnly = false;
	
	@Column(name = "keywords_description")
	private Boolean keywordsDescription;
	
	@Column(name = "min_bids")
	@Min(0)
	private int minBids;
	
	@Column(name = "max_bids")
	@Min(0)
	private int maxBids;
	
	@Column(name = "min_price")
	@Min(0)
	private int minPrice;
	
	@Column(name = "max_price")
	@Min(0)
	private int maxPrice;
	
	@Min(1)
	private int refreshTime;
	
	@Column
	private Date lastRefreshed;
	
	@ElementCollection
	@Enumerated(EnumType.STRING)
	private List<tomaspolacok.bachelor.application.enums.ListingType> listingTypes = new ArrayList<>();
	
	@OneToMany
	private Set<SearchItemBlacklist> blacklistItems = new HashSet<>();
	
	@OneToMany
	private Set<SearchSellerBlacklist> blacklistSellers = new HashSet<>();
	
	@Column
	@ElementCollection
	private Set<String> sellers = new HashSet<>();
	
	@Column
	private Boolean sellerSearch;
	
	@OneToOne
	private SearchPreferences searchPreferences;
	
	@OneToOne
	private SearchPhoneNotificationSettings phoneNotificationSettings;
	

}
