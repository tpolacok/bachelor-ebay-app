package tomaspolacok.bachelor.application.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import lombok.Data;

@Data
@Entity
public class EbayCountry {
	@Id
	@Column(name = "ebay_site_id")
	private Integer siteId;
	
	@Column(name = "ebay_global_id")
	private String globalId;
	
	@Column(name = "site_name")
	private String siteName;
	
	@Column
	private Date lastRefreshed;
	
	@ManyToOne
	private Currency currency;
	
	@Column(name = "category_enabled")
	private Boolean categoryEnabled;
	
	@Column(name = "condition_enabled")
	private Boolean conditionEnabled;
	
	@Column(name = "returns_only_enabled")
	private Boolean returnsOnlyEnabled;
	
	@Column(name = "category_version")
	private String categoryVersion;
	
	public EbayCountry() {
	};
	public EbayCountry(Integer id) {
		siteId = id;
	}

}
