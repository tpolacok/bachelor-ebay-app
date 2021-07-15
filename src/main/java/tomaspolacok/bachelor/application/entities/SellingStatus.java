package tomaspolacok.bachelor.application.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;

import lombok.Data;
import tomaspolacok.bachelor.application.enums.SellingState;

@Data
@Entity
public class SellingStatus {
	
	@Id
    @Column(name = "selling_status_id")
    private Long id;
	
	@Column(name = "bid_count")
	private Integer bidCount;
	
	@Column(name = "current_price")
	private Double currentPrice;
	
	@Enumerated(EnumType.STRING)
	private SellingState sellingState;

}
