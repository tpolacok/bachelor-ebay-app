package tomaspolacok.bachelor.application.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import lombok.Data;

@Data
@Entity
public class SearchSellerBlacklist {

	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
	
	@ManyToOne
	private Search search;
	
	@ManyToOne
	private EbaySeller seller;
	
	@Column
	private String reason;

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SearchSellerBlacklist other = (SearchSellerBlacklist) obj;
		if (search == null) {
			if (other.search != null)
				return false;
		} else if (!search.getId().equals(other.search.getId()))
			return false;
		if (seller == null) {
			if (other.seller != null)
				return false;
		} else if (!seller.getName().equals(other.seller.getName()))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((seller == null) ? 0 : seller.hashCode());
		return result;
	}
	
	
	
}