package tomaspolacok.bachelor.application.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import lombok.Data;

@Data
@Entity
public class DisplayCount {
	
	public DisplayCount(int pageSize) {
		this.pageSize = pageSize;
	}
	
	public DisplayCount() {
	}
	
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
	
	@Column
	private int pageSize = 9;
	
	@Column
	private Boolean displayAll = false;
	
	/**
	 * Returns pageable with set variables
	 * @param page
	 * @return
	 */
	public Pageable getPageable(Integer page) {
		if (this.getDisplayAll()) {
			return PageRequest.of(page, Integer.MAX_VALUE);
		}
		return PageRequest.of(page, this.getPageSize());
	}
}
