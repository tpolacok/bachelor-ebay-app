package tomaspolacok.bachelor.application.entities;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

import org.hibernate.validator.constraints.Length;

import lombok.Data;

@Data
@Entity
@Table(name = "app_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "user_id")
    private Long id;
    
    @Column(name = "email")
    @Email(message = "*Please provide a valid Email")
    @NotEmpty(message = "*Please provide an email")
    private String email;
    
    @Column(name = "password")
    @Length(min = 5, message = "*Your password must have at least 5 characters")
    @NotEmpty(message = "*Please provide your password")
    private String password;  
    
    @Column(name = "active")
    private Boolean active;
    
    @Column
    private String activationCode;
    
    @Column
    private String resetCode;
    
    @Column
    private String phoneActivationCode;
    
    @Column
    private String phoneNumber;
    
    @Column
    private Boolean phoneActivated = false;
    
    @Column
    private Integer smsSentCount = 0;
    
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "user_notified_items", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "item_id"))
    private Set<EbayItem> notifiedItems = new HashSet<>();

	@OneToMany
	private Set<UserSellerBlacklist> blacklistSellers = new HashSet<>();; 
	
	@OneToMany
	private Set<UserItemBlacklist> blacklistItems; 
	
	@OneToMany
	private List<PictureCategory> pictureCategories; 
	
	@OneToMany
	private List<UserPicture> userPictures;
	
	@OneToOne
	UserPreferences userPreferences;

}