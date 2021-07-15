package tomaspolacok.bachelor.application.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import lombok.Data;

@Data
@Entity
public class UserPicture {

	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
	
	@ManyToOne
	private User user;
	
	@ManyToOne
	private Picture picture;
	
	@ManyToOne
	private PictureCategory category;
}
