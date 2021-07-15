package tomaspolacok.bachelor.application.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import lombok.Data;

@Data
@Entity
public class Picture {

	@Id
    private String id;
	
	@Column
	private String googleId;
	
	@Column
	private String downloadLink;
	
	@Column
	private String ebayLink;
	
	@ManyToOne
	private EbayItem item;
	
}
