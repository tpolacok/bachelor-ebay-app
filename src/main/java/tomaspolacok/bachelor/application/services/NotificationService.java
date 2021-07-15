package tomaspolacok.bachelor.application.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.twilio.sdk.Twilio;
import com.twilio.sdk.fetcher.lookups.v1.PhoneNumberFetcher;
import com.twilio.sdk.resource.api.v2010.account.Message;
import com.twilio.sdk.type.PhoneNumber;

import tomaspolacok.bachelor.application.entities.EbayItem;
import tomaspolacok.bachelor.application.entities.Search;
import tomaspolacok.bachelor.application.entities.SearchPhoneNotificationSettings;
import tomaspolacok.bachelor.application.entities.User;


@Service
public class NotificationService {
	
	@Value("${spring.mail.username}")
    private String sender;
	@Value("${twilio.account.sid}")
    private String twilioAccountSid;
	@Value("${twilio.auth.token}")
    private String twilioAuthToken;
	@Value("${twilio.number}")
    private String twilioNumber;
	
	/**
	 * MAX SMS count for each user(so application wont send too many messages)
	 */
	private final Integer MAX_SMS = 3;
	
	@Autowired
	private JavaMailSender mailSender;
	@Autowired
	private TemplateEngine templateEngine;
	@Autowired
	UserService userService;
	@Autowired
	CurrencyService currencyService;
	
    @Autowired
    public NotificationService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }
    
    /**
     * Notify user of new items
     * @param itemList
     * @param search
     */
    public void notify(List<EbayItem> itemList, Search search) {
    	prepareAndSendItemEmail(itemList, search);
    	prepareAndSendItemSms(itemList, search);
    }
    
    /**
     * Sends sms about new items if it's allowed
     * @param itemList
     * @param search
     */
    public void prepareAndSendItemSms(List<EbayItem> itemList, Search search) {
    	User user = search.getUser();
    	SearchPhoneNotificationSettings searchNotifications = search.getPhoneNotificationSettings();
    	if (!searchNotifications.getNotificationsAuction() || !user.getPhoneActivated() || user.getSmsSentCount() == MAX_SMS) return;
    	//count items matching notification criteria
    	int auctionOnlyCount = 0;
    	int auctionWithBINCount = 0;
    	int fixedCount = 0;
    	for (EbayItem item : itemList) {
    		if (item.getSellingStatus().getBidCount() == null) {
    			fixedCount++;
    		} else {
    			if(item.getBuyItNow()) {
    				auctionWithBINCount++;
    			} else {
    				auctionOnlyCount++;
    			}
    		}
    	}
    	StringBuilder builder = new StringBuilder(search.getName() + " new items:");
    	
    	if (searchNotifications.getNotificationsAuction()) {
    		if (auctionOnlyCount > 0)
    			builder.append(" " + auctionOnlyCount + " " + tomaspolacok.bachelor.application.enums.ListingType.AUCTION.getName() + "|");
    	}
    	if (searchNotifications.getNotificationsAuctionBuyItNow()) {
    		if (auctionWithBINCount > 0)
    			builder.append(" " + auctionOnlyCount + " " + tomaspolacok.bachelor.application.enums.ListingType.AUCTION_BUY_IT_NOW.getName() + "|");
    	}
    	if (searchNotifications.getNotificationsBuyItNow()) {
    		if (fixedCount > 0)
    			builder.append(" " + auctionOnlyCount + " " + tomaspolacok.bachelor.application.enums.ListingType.FIXED_PRICE.getName() + "|");
    	}
    	Twilio.init(twilioAccountSid, twilioAuthToken);
    	Message.create(twilioAccountSid, new PhoneNumber(user.getPhoneNumber()), new PhoneNumber(twilioNumber), builder.toString())
    		   .execute();
    	user.setSmsSentCount(user.getSmsSentCount() + 1);
    	
    }
    
    /**
     * Sends phone activation sms for user
     * @param user
     */
    public void sendPhoneActivationSms(User user) {
    	PhoneNumberFetcher phone = new PhoneNumberFetcher(new PhoneNumber(user.getPhoneNumber()));
    	if (user.getSmsSentCount() >= 1) return;
    	Twilio.init(twilioAccountSid, twilioAuthToken);
    	Message.create(twilioAccountSid, new PhoneNumber(user.getPhoneNumber()), new PhoneNumber(twilioNumber), "Phone activation code: " + user.getPhoneActivationCode())
		   .execute();
    	user.setSmsSentCount(user.getSmsSentCount() + 1);
    }

 
    /**
     * Send mail about new items
     * @param itemList
     * @param search
     */
    public void prepareAndSendItemEmail(List<EbayItem> itemList, Search search) {
    	MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom(sender);
            messageHelper.setTo(search.getUser().getEmail());
            messageHelper.setSubject(itemList.size() + " new items in your search " + search.getName());
            String content = buildItemEmail(itemList, search);
            messageHelper.setText(content, true);
        };
        try {
            mailSender.send(messagePreparator);
        } catch (MailException e) {
        	e.printStackTrace();
        }
    }
    
    /**
     * Builds email using template
     * @param itemList
     * @param search
     * @return
     */
    private String buildItemEmail(List<EbayItem> itemList, Search search) {
        Context context = new Context();
        context.setVariable("items", currencyService.convertItems(itemList, search, search.getUser()));
        return templateEngine.process("itemEmail", context);
    }
    
    /**
     * Prepare and send activation email
     * @param user
     * @param host
     */
    public void prepareAndSendActivationEmail(User user, String host) {
    	MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom(sender);
            messageHelper.setTo(user.getEmail());
            messageHelper.setSubject("Activation email");
            String content = buildActivationEmail(user, host);
            messageHelper.setText(content, true);
        };
        try {
            mailSender.send(messagePreparator);
        } catch (MailException e) {
        	e.printStackTrace();
        }
    }
    
    /**
     * Builds activation email using template
     * @param user
     * @param host
     * @return
     */
    private String buildActivationEmail(User user, String host) {
        Context context = new Context();
        String activationLink = "http://" + host + "/activate/" + user.getActivationCode();
        context.setVariable("activationLink", activationLink);
        return templateEngine.process("userActivationEmail", context);
    }
    
    /**
     * Prepares and sends reset email
     * @param user
     * @param host
     */
    public void prepareAndSendResetEmail(User user, String host) {
    	MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom(sender);
            messageHelper.setTo(user.getEmail());
            messageHelper.setSubject("Reset password");
            String content = buildResetEmail(user, host);
            messageHelper.setText(content, true);
        };
        try {
            mailSender.send(messagePreparator);
        } catch (MailException e) {
        	e.printStackTrace();
        }
    }
    
    /**
     * Builds reset email from template
     * @param user
     * @param host
     * @return
     */
    private String buildResetEmail(User user, String host) {
        Context context = new Context();
        String resetLink = "http://" + host + "/user/password/reset/" + user.getResetCode();
        context.setVariable("resetLink", resetLink);
        return templateEngine.process("passwordResetEmail", context);
    }

}
