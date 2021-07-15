package tomaspolacok.bachelor.application.entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;
import tomaspolacok.bachelor.application.enums.Condition;
import tomaspolacok.bachelor.application.enums.ListingType;

@Data
@Entity
public class EbayItem {
	
	@Id
    @Column(name = "ebayitem_id")
    private Long id;
	
	@ManyToMany(mappedBy = "items", fetch = FetchType.LAZY)
	private List<Search> searches;
	
	@Enumerated(EnumType.STRING)
	private Condition condition;
	
	@Enumerated(EnumType.STRING)
	private ListingType listingType;

	@ManyToOne
	private Country country;
	
	@Column
	private String defaultPicture;
	
	@OneToMany
	private List<Picture> pictures = new ArrayList<>();
	
	@Transient
	private String description;
	
	@Transient
	private List<Bid> bids = new ArrayList<>();
	
	@Transient
	private Integer stepSize;
	
	@ManyToOne
	private Currency bidsCurrency;
	
	@ManyToOne
	private Currency currency;

	@Column
	private String primaryCategory;
	
	@Column(name = "returns_accepted")
	private Boolean returnsAccepted;
	
	@Column(name = "title")
	private String title;
	
	@Column(name = "subtitle")
	private String subtitle;
	
	@Column(name = "item_url")
	private String itemUrl;
	
	@ManyToOne
	private EbaySeller seller;
	
	@OneToOne
	private Shipping shipping;
	
	@OneToOne
	private SellingStatus sellingStatus;
	
	@Column(name = "buy_it_now")
	private Boolean buyItNow;
	
	@Column(name = "buy_it_now_price")
	private Double buyItNowPrice;
	
	@Column(name = "start_time")
	@DateTimeFormat(pattern = "dd.MM.yyyy")
	private Date startTime;
	
	@Column(name = "end_time")
	@DateTimeFormat(pattern = "dd.MM.yyyy")
	private Date endTime;

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EbayItem other = (EbayItem) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "EbayItem [id=" + id + "]";
	}
	
	
	
	

}
