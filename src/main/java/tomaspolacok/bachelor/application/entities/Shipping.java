package tomaspolacok.bachelor.application.entities;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import lombok.Data;

@Data
@Entity
public class Shipping {
	
	@Id
    @Column(name = "shipping_id")
    private Long id;
	
	@Column(name = "expedited_shipping")
	private Boolean expeditedShipping;
	
	@Column(name = "one_day_shipping")
	private Boolean oneDayShipping;
	
	@Column(name = "handling_time")
	private Integer handlingTime;
	
	@Column(name = "shipping_cost")
	private Double shippingCost;
	
	@ManyToOne
	private Currency currency;
	
	@Enumerated(EnumType.STRING)
	private tomaspolacok.bachelor.application.enums.ShippingType shippingType;
}

