package tomaspolacok.bachelor.application.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;

@Data
@Entity
public class EbaySeller {
	
	@Id
    @Column(name = "name")
    private String name;
	
	@Column(name = "feedback_score")
	private Long feedbackScore;
	
	@Column(name = "feedback_positive_percent")
	private Double feedbackPositivePercent;
	
	@Column(name = "top_rated")
	private Boolean topRated;
}
