package tomaspolacok.bachelor.application.entities;

import javax.persistence.*;

import lombok.Data;

@Data
@Entity
public class Category {
	
	@EmbeddedId
    private CategoryId id;

    @Column(name = "name")
    private String name;
    
    @Column(name = "active")
    private Boolean active;
    
}
