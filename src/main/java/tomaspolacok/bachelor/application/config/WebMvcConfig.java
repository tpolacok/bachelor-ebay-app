package tomaspolacok.bachelor.application.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.ebay.sdk.ApiContext;
import com.ebay.sdk.ApiCredential;
import com.ebay.sdk.SiteIDUtil;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import tomaspolacok.bachelor.application.main.EbayAuctionManager;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
	
	@Value("${google.app-id}")
    private String appId;
	@Value("${google.service.path}")
    private String serviceAccount;
	
	@Value("${ebay.trading.site}")
	private String siteCode;
	@Value("${ebay.trading.url}")
	private String url;
	@Value("${ebay.trading.token}")
    private String token;
	
	/**
	 * Creates instance of password encodes
	 * @return
	 */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        return bCryptPasswordEncoder;
    }
    
    /**
     * Creates instance of service for handling Google Drive services
     * @return
     * @throws IOException
     */
    @Bean
    public Drive getDriveService() throws IOException{
		InputStream in = EbayAuctionManager.class.getResourceAsStream(serviceAccount);
		HttpTransport httpTransport = new NetHttpTransport();
		JacksonFactory jsonFactory = new JacksonFactory();
		GoogleCredential credentials = GoogleCredential.fromStream(in)
		        .createScoped(Collections.singletonList(DriveScopes.DRIVE_FILE));
		Drive service = new Drive.Builder(httpTransport, jsonFactory, null)
				.setApplicationName(appId)
				.setHttpRequestInitializer(credentials)
				.build();
		return service;
	}
    
    /**
     * Creates instance of Api Context for handling communication for eBay Trading API
     * @return
     */
    @Bean
    public ApiContext getApiContext() {
	      ApiContext apiContext = new ApiContext();
	      ApiCredential cred = apiContext.getApiCredential();
	      cred.seteBayToken(token);
	      apiContext.setApiServerUrl(url);
	      apiContext.setSite(SiteIDUtil.fromNumericalID(Integer.parseInt(siteCode)));
	      return apiContext;
	}
    
}