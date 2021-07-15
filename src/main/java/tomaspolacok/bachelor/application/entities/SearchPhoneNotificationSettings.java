package tomaspolacok.bachelor.application.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name="NOTIFICATION_SETTINGS")
public class SearchPhoneNotificationSettings {
	
	public SearchPhoneNotificationSettings(Boolean enabled, Boolean auction, Boolean auctionBIN, Boolean fixed) {
		notificationsEnabled = enabled;
		notificationsAuction = auction;
		notificationsAuctionBuyItNow = auctionBIN;
		notificationsBuyItNow = fixed;
	}
	
	public SearchPhoneNotificationSettings() {
	}
	
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

	@Column
	private Boolean notificationsEnabled = false;
	
	@Column
	private Boolean notificationsAuction = false;
	
	@Column
	private Boolean notificationsAuctionBuyItNow = false;
	
	@Column
	private Boolean notificationsBuyItNow = false;
	
	/**
	 * Sets phone settings
	 * @param other
	 */
	public void setPhoneDefaults(SearchPhoneNotificationSettings other) {
		this.notificationsAuction = other.getNotificationsAuction();
		this.notificationsAuctionBuyItNow = other.getNotificationsAuctionBuyItNow();
		this.notificationsBuyItNow = other.getNotificationsBuyItNow();
		this.notificationsEnabled = other.getNotificationsEnabled();
	}
	
}
