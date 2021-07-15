package tomaspolacok.bachelor.application.entities;

import javax.persistence.*;

import lombok.Data;

@Data
@Entity
public class Country {
	@Id
	@Column(name = "code")
	private String code;
	
	@Column(name = "name")
	private String name;
	
}