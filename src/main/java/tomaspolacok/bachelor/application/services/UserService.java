package tomaspolacok.bachelor.application.services;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.google.common.hash.Hashing;

import tomaspolacok.bachelor.application.entities.DisplayCount;
import tomaspolacok.bachelor.application.entities.EbayCountry;
import tomaspolacok.bachelor.application.entities.EbayItem;
import tomaspolacok.bachelor.application.entities.EbaySeller;
import tomaspolacok.bachelor.application.entities.PictureCategory;
import tomaspolacok.bachelor.application.entities.Search;
import tomaspolacok.bachelor.application.entities.SearchPhoneNotificationSettings;
import tomaspolacok.bachelor.application.entities.User;
import tomaspolacok.bachelor.application.entities.UserItemBlacklist;
import tomaspolacok.bachelor.application.entities.UserPreferences;
import tomaspolacok.bachelor.application.entities.UserSellerBlacklist;
import tomaspolacok.bachelor.application.enums.ListingType;
import tomaspolacok.bachelor.application.exceptions.PhoneActivationCodeFailException;
import tomaspolacok.bachelor.application.repositories.DisplayCountRepository;
import tomaspolacok.bachelor.application.repositories.SearchPhoneNotificationSettingsRepository;
import tomaspolacok.bachelor.application.repositories.UserItemBlacklistRepository;
import tomaspolacok.bachelor.application.repositories.UserPreferencesRepository;
import tomaspolacok.bachelor.application.repositories.UserRepository;
import tomaspolacok.bachelor.application.repositories.UserSellerBlacklistRepository;

@Service
public class UserService {

	@Autowired
	private NotificationService notificationService;
	@Autowired
    private UserRepository userRepository;
	@Autowired
	private UserPreferencesRepository userPreferencesRepository;
	@Autowired
	private UserSellerBlacklistRepository sellerBanRepository;
	@Autowired
	private UserItemBlacklistRepository itemBanRepository;
	@Autowired
	private DisplayCountRepository displayCountRepository;
	@Autowired
	private SearchPhoneNotificationSettingsRepository phoneRepository;
	@Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	/**
	 * Default site for user preferences
	 */
	private final EbayCountry defaultSite = new EbayCountry(77);
	
	/**
	 * adds test account
	 */
	@PostConstruct
	public void testAccount() {
		if (userRepository.findByEmail("a@a") == null) {
			
			User u = new User();
			u.setEmail("a@a");
			u.setPassword(bCryptPasswordEncoder.encode("a"));
			u.setActive(true);
	        
	        UserPreferences up = new UserPreferences();
	        DisplayCount searchCount = new DisplayCount(9);
	        DisplayCount itemBanCount = new DisplayCount(36);
			DisplayCount sellerBanCount = new DisplayCount(36);
			DisplayCount archiveCount = new DisplayCount(15);
			
	        up.setSearchCount(searchCount);
			up.setItemBanCount(itemBanCount);
			up.setSellerBanCount(sellerBanCount);
			up.setArchiveCount(archiveCount);
			up.setListingTypes(Arrays.asList(ListingType.AUCTION));
			
			SearchPhoneNotificationSettings phoneSettings = new SearchPhoneNotificationSettings();
			up.setPhoneNotificationsDefaults(phoneSettings);
			
			up.setEbayCountry(defaultSite);
			
			displayCountRepository.saveAll(Arrays.asList(searchCount, itemBanCount, sellerBanCount, archiveCount));
			phoneRepository.save(phoneSettings);
	        userPreferencesRepository.save(up);
	        u.setUserPreferences(up);
	        userRepository.save(u);
		}
	}
	
	@PostConstruct
	public void testAccount2() {
		if (userRepository.findByEmail("b@b") == null) {
			
			User u = new User();
			u.setEmail("b@b");
			u.setPassword(bCryptPasswordEncoder.encode("b"));
			u.setActive(true);
	        
	        UserPreferences up = new UserPreferences();
	        DisplayCount searchCount = new DisplayCount(9);
	        DisplayCount itemBanCount = new DisplayCount(36);
			DisplayCount sellerBanCount = new DisplayCount(36);
			DisplayCount archiveCount = new DisplayCount(15);
			
	        up.setSearchCount(searchCount);
			up.setItemBanCount(itemBanCount);
			up.setSellerBanCount(sellerBanCount);
			up.setArchiveCount(archiveCount);
			up.setListingTypes(Arrays.asList(ListingType.AUCTION));
			
			SearchPhoneNotificationSettings phoneSettings = new SearchPhoneNotificationSettings();
			up.setPhoneNotificationsDefaults(phoneSettings);
			
			up.setEbayCountry(defaultSite);
			
			displayCountRepository.saveAll(Arrays.asList(searchCount, itemBanCount, sellerBanCount, archiveCount));
			phoneRepository.save(phoneSettings);
	        userPreferencesRepository.save(up);
	        u.setUserPreferences(up);
	        userRepository.save(u);
		}
	}
	
	/**
	 * Activates account, returns true if the account has been activated, returns false if the account was already activated or link is invalid
	 * @param activationCode
	 * @return
	 */
	public Boolean activate(String activationCode) {
		User user = userRepository.findByActivationCode(activationCode);
		if (user != null && !user.getActive()) {
			user.setActive(true);
			userRepository.save(user);
			return true;
		}
		return false;
	}
	
	/**
	 * Sets default values of search( when creating new)
	 * @param search
	 * @return
	 */
	public void setDefaults(Search search) {
		UserPreferences up = getLoggedUser().getUserPreferences();
		search.setShippingTo(up.getShippingTo());
		search.setListingTypes(up.getListingTypes());
		search.setRefreshTime(up.getRefreshTime());
		search.setReturnsAcceptedOnly(up.getReturnsAcceptedOnly());
		search.setNewOnly(up.getNewOnly());
	}
	
	/**
	 * Checks whether password is equal to password in database
	 * @param password
	 * @return
	 */
	public boolean checkPassword(String password) {
		return bCryptPasswordEncoder.matches(password, getLoggedUser().getPassword());
	}
	
	/**
	 * Changes user's password
	 * @param password
	 * @return
	 */
	public void changePassword(String password) {
		User user = getLoggedUser();
		user.setPassword(bCryptPasswordEncoder.encode(password));
		userRepository.save(user);
	}
	
	/**
	 * Finds user by reset code
	 * @param resetCode
	 * @return
	 */
	public User findUserByResetCode(String resetCode) {
		if (resetCode == null || resetCode == "") {
			return null;
		}
		return userRepository.findByResetCode(resetCode);
	}
	
	/**
	 * Sends password reset email to user
	 * @param user
	 */
	public void sendPasswordChangeRequest(User user, String host) {
		user.setResetCode(generateResetCode(user.getPassword() + user.getEmail()));
		userRepository.save(user);
		notificationService.prepareAndSendResetEmail(user, host);
	}
	

	/**
	 * Removes reset code, changes password
	 * @param user
	 * @param password
	 */
	public void resetPasswordByRequest(User user, String password) {
		user.setResetCode(null);
		user.setPassword(bCryptPasswordEncoder.encode(password));
		userRepository.save(user);
	}
	
	
	/**
	 * Gets user's user preferences
	 * @return
	 */
	public UserPreferences getUserPreferences() {
		UserPreferences up = getLoggedUser().getUserPreferences();
		up.setSessionTime(up.getSessionTime()/60);
		return up;
	}
	
	/**
	 * Saves new user preferences and sets session inactive time
	 * @param userPreferences
	 */
	public void saveUserPreferences(UserPreferences userPreferences, HttpSession session) {
		User user = getLoggedUser();
		UserPreferences up = user.getUserPreferences();
		up.setFreeShippingOnly(userPreferences.getFreeShippingOnly());
		up.setListingTypes(userPreferences.getListingTypes());
		up.setNewOnly(userPreferences.getNewOnly());
		up.setRefreshTime(userPreferences.getRefreshTime());
		up.setReturnsAcceptedOnly(userPreferences.getReturnsAcceptedOnly());
		up.setShippingTo(userPreferences.getShippingTo());
		up.setCurrency(userPreferences.getCurrency());
		up.setEbayCountry(userPreferences.getEbayCountry());
		up.setSessionTime(userPreferences.getSessionTime()*60);
		up.setPermanentLogin(userPreferences.getPermanentLogin());
		up.getPhoneNotificationsDefaults().setPhoneDefaults(userPreferences.getPhoneNotificationsDefaults());
		
		if (up.getPermanentLogin()) {
			session.setMaxInactiveInterval(-1);
		} else {
			session.setMaxInactiveInterval(up.getSessionTime());
		}
		
		
		phoneRepository.save(userPreferences.getPhoneNotificationsDefaults());
		userRepository.save(user);
	}
	
	/**
	 * Updated given displayCount parameters
	 * @param displayCount
	 * @param size
	 * @param all
	 */
	public void updateDisplayCount(DisplayCount displayCount, Integer size, Boolean all) {
		if (all != null && all == true) {
			displayCount.setDisplayAll(true);
		}
		if (size != null) {
			if (size > 0) {
				displayCount.setDisplayAll(false);
				displayCount.setPageSize(size);
			}
		}
		displayCountRepository.save(displayCount);
	}
	
	/**
	 * Sets user's default archive view category
	 * @param category
	 */
	public void updateArchiveCategory(PictureCategory category) {
		User u = getLoggedUser();
		UserPreferences up = u.getUserPreferences();
		up.setPictureCategory(category);
		userPreferencesRepository.save(up);
	}

	/**
	 * Finds user by email
	 * @param email
	 * @return
	 */
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    /**
     * Method gets logged user
     * @return
     */
    public User getLoggedUser() {
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = this.findUserByEmail(auth.getName());
		return user;
    }
    
    /**
     * Returns page of user-item blacklist mapping
     * @param page
     * @return
     */
    public Page<UserItemBlacklist> getBlacklistedItems(Integer page) {
		return itemBanRepository.findBlacklistItem(getLoggedUser(), getLoggedUser().getUserPreferences().getItemBanCount().getPageable(page));
	}
	
    /**
     * Returns page of user-seller blacklist mapping
     * @param page
     * @return
     */
	public Page<UserSellerBlacklist> getBlacklistedSellers(Integer page) {
		return sellerBanRepository.findBlacklistSeller(getLoggedUser(), getLoggedUser().getUserPreferences().getSellerBanCount().getPageable(page));
	}
    
	/**
	 * Adds user blacklist for item
	 * @param item
	 * @param reason
	 */
    public void addBlacklistItem(EbayItem item, String reason) {
		User user = getLoggedUser();
		UserItemBlacklist uib = new UserItemBlacklist();
		uib.setUser(user);
		uib.setItem(item);
		uib.setReason(reason);
		if (!user.getBlacklistItems().contains(uib)) {
			user.getBlacklistItems().add(uib);
			itemBanRepository.save(uib);
			userRepository.save(user);
		}
    }
    
    /**
     * Deletes user blacklist for item
     * @param item
     */
    public void deleteBlacklistItem(EbayItem item) {
    	User user = getLoggedUser();
    	UserItemBlacklist uib = itemBanRepository.findByUserAndItem(user, item);
		user.getBlacklistItems().remove(uib);
		userRepository.save(user);
		itemBanRepository.delete(uib);
    }
    
    /**
     * Adds user blacklist for seller
     * @param seller
     * @param reason
     */
    public void addBlacklistSeller(EbaySeller seller, String reason) {
    	User user = getLoggedUser();
		UserSellerBlacklist usb = new UserSellerBlacklist();
		usb.setUser(user);
		usb.setSeller(seller);
		usb.setReason(reason);
		
		if (!user.getBlacklistSellers().contains(usb)) {
			user.getBlacklistSellers().add(usb);
			sellerBanRepository.save(usb);
			userRepository.save(user);
		}
    }
    
    /**
     * Deletes user blacklist for seller
     * @param seller
     */
    public void deleteBlacklistSeller(EbaySeller seller) {
    	User user = getLoggedUser();
		UserSellerBlacklist usb = sellerBanRepository.findByUserAndSeller(user, seller);
		user.getBlacklistSellers().remove(usb);
		userRepository.save(user);
		sellerBanRepository.delete(usb);
    }
    /**
     * Updates user
     * @param user
     */
    public void updateUser(User user) {
    	userRepository.save(user);
    }
    
    /**
     * Saves new user with USER role and default preferences
     * @param user
     */
    public void saveUser(User user, String host) {
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        user.setActive(false);
        user.setActivationCode(generateActivationCode((user.getEmail() + user.getPassword())));

        UserPreferences up = new UserPreferences();
        DisplayCount searchCount = new DisplayCount(9);
        DisplayCount itemBanCount = new DisplayCount(36);
		DisplayCount sellerBanCount = new DisplayCount(36);
		DisplayCount archiveCount = new DisplayCount(15);
        up.setSearchCount(searchCount);
		up.setItemBanCount(itemBanCount);
		up.setSellerBanCount(sellerBanCount);
		up.setArchiveCount(archiveCount);
		
		SearchPhoneNotificationSettings phoneSettings = new SearchPhoneNotificationSettings();
		up.setPhoneNotificationsDefaults(phoneSettings);
		
		up.setEbayCountry(defaultSite);
		
		displayCountRepository.saveAll(Arrays.asList(searchCount, itemBanCount, sellerBanCount, archiveCount));
		phoneRepository.save(phoneSettings);
		userPreferencesRepository.save(up);
        user.setUserPreferences(up);
        userRepository.save(user);
        notificationService.prepareAndSendActivationEmail(user, host);
    }
    
    /**
     * Adds phone number to user and generates and sends activation code
     * @param phoneNumber
     */
    public void addPhoneNumber(String phoneNumber) {
    	User user = getLoggedUser();
    	user.setPhoneNumber(phoneNumber);
    	user.setPhoneActivationCode(generatePhoneActivationCode());
    	user.setPhoneActivated(false);
    	notificationService.sendPhoneActivationSms(user);
    	userRepository.save(user);
    	
    }
    
    /**
     * Validates user's phone activation code input
     * @param activationCode
     */
    public void activatePhoneNumber(String activationCode) throws Exception{
    	User user = getLoggedUser();
    	if (user.getPhoneActivationCode().compareTo(activationCode) != 0) {
    		throw new PhoneActivationCodeFailException("");
    	} else {
    		user.setPhoneActivated(true);
    		userRepository.save(user);
    	}
    }
    
    /**
     * Generates code for phone activation
     * @return
     */
    private String generatePhoneActivationCode() {
    	Random rand = new Random();
    	StringBuilder code = new StringBuilder();
    	for (int i = 0; i < 6; ++i) {
    		Boolean number = rand.nextBoolean();
    		if (number) {
    			code.append((char)(rand.nextInt(10) + 48));
    		} else {
    			code.append((char)(rand.nextInt(26) + 65));
    		}
    	}
    	return code.toString();
    }
    
    
    /**
     * Generates activation code for user
     * @param code
     * @return
     */
    private String generateActivationCode(String code) {
    	Random rand = new Random();
    	while(true) {
    		String hash = generateHash(code);
    		code += rand.nextInt(1000000);
    		Boolean exists = userRepository.activationCodeExists(hash);
    		if (!exists) {
    			return hash;
    		}
    	}
    }
    
    /**
     * Generates reset code for user
     * @param code
     * @return
     */
    private String generateResetCode(String code) {
    	Random rand = new Random();
    	while(true) {
    		String hash = generateHash(code);
    		code = rand.nextInt(1000000) + code;
    		Boolean exists = userRepository.resetCodeExists(hash);
    		if (!exists) {
    			return hash;
    		}
    	}
    }
    
    /**
     * Generates hash from string
     * @param str
     * @return
     */
    private String generateHash(String str) {
    	return Hashing.sha256()
    			  .hashString(str, StandardCharsets.UTF_8)
    			  .toString();
    }

}