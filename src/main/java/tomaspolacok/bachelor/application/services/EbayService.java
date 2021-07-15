package tomaspolacok.bachelor.application.services;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ebay.sdk.ApiContext;
import com.ebay.sdk.call.GetCategoriesCall;
import com.ebay.sdk.call.GetCategoryMappingsCall;
import com.ebay.sdk.call.GetItemCall;
import com.ebay.services.client.ClientConfig;
import com.ebay.services.client.FindingServiceClientFactory;
import com.ebay.services.finding.ErrorData;
import com.ebay.services.finding.FindItemsAdvancedRequest;
import com.ebay.services.finding.FindItemsAdvancedResponse;
import com.ebay.services.finding.FindingServicePortType;
import com.ebay.soap.eBLBaseComponents.CategoryMappingType;
import com.ebay.soap.eBLBaseComponents.DetailLevelCodeType;
import com.ebay.soap.eBLBaseComponents.GetCategoriesResponseType;
import com.ebay.soap.eBLBaseComponents.ItemType;
import com.ebay.soap.eBLBaseComponents.SiteCodeType;

import tomaspolacok.bachelor.application.entities.Bid;
import tomaspolacok.bachelor.application.entities.EbayItem;
import tomaspolacok.bachelor.application.entities.Picture;
import tomaspolacok.bachelor.application.entities.Search;
import tomaspolacok.bachelor.application.entities.SellingStatus;
import tomaspolacok.bachelor.application.enums.SellingState;
import tomaspolacok.bachelor.application.repositories.EbayItemRepository;
import tomaspolacok.bachelor.application.repositories.PictureRepository;
import tomaspolacok.bachelor.application.repositories.SellingStatusRepository;

@Service
public class EbayService {
	
	@Autowired
	CategoryService categoryService;
	@Autowired
	ApiContext apiContext;
	@Autowired
	SellingStatusRepository sellingStatusRepository;
	@Autowired
	EbayItemRepository itemRepository;
	@Autowired
	PictureRepository pictureRepostory;
	
	private Integer categoryErrorCode = 3;
	
	@Value("${ebay.finding.id}")
	private String applicationId;
    
    /**
     * Sents request for getting items on based criteria and returns reponse or throws exception if some error occurs
     * @param request
     * @param search
     * @return
     * @throws Exception
     */
    public FindItemsAdvancedResponse sendFindItemsAdvancedRequest(FindItemsAdvancedRequest request, Search search) throws Exception {
		ClientConfig config = new ClientConfig();
		config.setApplicationId(applicationId);
		config.setGlobalId(search.getEbayCountry().getGlobalId());
		FindingServicePortType serviceClient = FindingServiceClientFactory.getServiceClient(config);
		FindItemsAdvancedResponse response = serviceClient.findItemsAdvanced(request);
		Boolean updated = false;
		System.out.println(response.getAck().toString());
		if (response.getAck().toString() != "FAILURE") {
			System.out.println(response.getPaginationOutput().getTotalEntries());
		}
		if ( response.getAck().toString().compareTo("FAILURE") == 0) {
			for (ErrorData em : response.getErrorMessage().getError() ) {
				System.out.println(em.getMessage());
				//category error
				if (em.getErrorId() == categoryErrorCode) {
					updated = categoryService.checkCategoryVersion(search.getEbayCountry());
				} else {
					//some other problem with ebay -> throw exception
					throw new Exception("Something is not working, please try again later");
				}
			}
			//category hierarchy has been updated -> try sending request again
			if (updated) {
				FindItemsAdvancedResponse responseNew = serviceClient.findItemsAdvanced(request);
				if ( responseNew.getAck().toString().compareTo("FAILURE") == 0) {
					//category hierarchy updated but some error still occurs
					throw new Exception("Something is not working, please try again later");
				} else {
					return responseNew;
				}
			} else {
				 //category id must be corrupted
				 throw new Exception("Something wrong with the categories, please try to specify other categories");
			}
		}
		return response;
    }
    
    /**
     * Sends category request and returns category hierarchy or throws an exception if some error occurs
     * @param request
     * @return
     * @throws Exception
     */
	public GetCategoriesResponseType sendCategoryRequest(GetCategoriesCall request) throws Exception {
		request.setApiContext(apiContext);
		request.getCategories();
		GetCategoriesResponseType response = request.getResponse();
		return response;
	}
	
	/**
	 * Sends category mapping request and returns category hierarchy or throws an exception if some error occurs
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public CategoryMappingType[] sendCategoryMappingRequest(GetCategoryMappingsCall request) throws Exception {
		request.setApiContext(apiContext);
		request.getCategoryMappings();
		CategoryMappingType[] response = request.getReturnedCategoryMapping();
		return response;
	}
	
	/**
	 * Method parses html page from ebay listing to get pictures urls and bid history, no API function for bid history exists
	 * 
	 * @param item
	 * @return returns item with urls of images and bid history, if there is any
	 * @throws Exception
	 */
	public EbayItem processEbayItem(EbayItem item) throws Exception{
		List<Bid> bidsList = new ArrayList<>();
		if (item.getSellingStatus().getBidCount() != null && item.getSellingStatus().getBidCount() > 0) {
			Document doc = Jsoup.connect("https://www.ebay.com/bfl/viewbids/" + item.getId()).get();
			Element table = doc.select("table.ui-component-table_wrapper").first();
			Elements rows = table.select("tr");
			for (int i = 1; i < rows.size(); ++i) {
				Elements tds = rows.get(i).select("td");
				Bid bid = new Bid();
				bid.setRating(getRatingFromName(tds.get(0).text(), (i == (rows.size() - 1))));
				bid.setPrice(getValueFromPrice(tds.get(1).text()));
				bid.setAutomatic(getAutomaticFromRow(rows.get(i)));
				bidsList.add(bid);
			}
			if (bidsList.size() > 0) {
				item.setStepSize( ((Double)( bidsList.get(0).getPrice() - bidsList.get(bidsList.size() - 1).getPrice() )).intValue());
			}
			item.setBids(bidsList);
		}
		return item;
	}
	
	/**
	 * Updates item, called when viewing item's detail
	 * @param item
	 * @throws Exception
	 */
	public void updateItem(EbayItem item) throws Exception{
		ItemType updatedItem = getItem(item);
		if (updatedItem.getListingDetails().getConvertedBuyItNowPrice() != null) {
			item.setBuyItNowPrice(updatedItem.getListingDetails().getConvertedBuyItNowPrice().getValue());
		}
		SellingStatus status = item.getSellingStatus();
		status.setCurrentPrice(updatedItem.getSellingStatus().getConvertedCurrentPrice().getValue());
		if (updatedItem.getSellingStatus().getBidCount() != null) {
			status.setBidCount(updatedItem.getSellingStatus().getBidCount() );
		}
		if (updatedItem.getSellingStatus().getListingStatus().toString() == "COMPLETED") {
			status.setSellingState(SellingState.ENDED);
		}
//		item.setDescription(updatedItem.getDescription());
		
		String idPrefix = item.getId() + "_";
		Integer increment = 0;
		if (item.getPictures().size() == 0) {
			List<Picture> pictures = new ArrayList<>();
			for (String link : updatedItem.getPictureDetails().getPictureURL()) {
				Picture picture = new Picture();
				picture.setItem(item);
				picture.setEbayLink(link);
				picture.setDownloadLink(null);
				picture.setGoogleId(null);
				picture.setId(idPrefix + increment);
				pictures.add(picture);
				increment++;
			}
			item.setPictures(pictures);
			pictureRepostory.saveAll(pictures);
		}
		sellingStatusRepository.save(status);
		itemRepository.save(item);
	}
	
	
	/**
	 * Sends GetItem eBay API call to get information about new item
	 * @param item
	 * @return
	 * @throws Exception
	 */
	private ItemType getItem(EbayItem item) throws Exception {
		GetItemCall itemCall = new GetItemCall();
		itemCall.setSite(SiteCodeType.US);
		itemCall.setApiContext(apiContext);
		DetailLevelCodeType[] ar = new DetailLevelCodeType[1];
		ar[0] = DetailLevelCodeType.RETURN_ALL;
		itemCall.setDetailLevel(ar);
		ItemType updatedItem = itemCall.getItem(item.getId().toString());
		return updatedItem;
	}
	
	/**
	 * Checks whether bid was an automatic bid (automatic bids are hidden by default so they have special class
	 * @param element
	 * @return
	 */
	private Boolean getAutomaticFromRow(Element element) {
		if (element.hasClass("ui-component-table_tr_detailinfo")) {
			return false;
		}
		return true;
	}
	
	/**
	 * Parses value from table row
	 * @param value
	 * @return
	 */
	private Double getValueFromPrice(String value) {
		int numberStartPosition = 0;
		for (int i = 0; i < value.length(); i++){
		    if (Character.isDigit(value.charAt(i))) {
		    	numberStartPosition = i;
		    	break;
		    }
		}
		StringBuilder stringBuilder  = new StringBuilder(value.substring(numberStartPosition));
		for (int i = 0; i < stringBuilder.length(); ++i) {
			if ((stringBuilder.charAt(i) == ',' || stringBuilder.charAt(i) == '.') && i != stringBuilder.length() - 3) {
				stringBuilder.setCharAt(i, ' ');
			}
			if (stringBuilder.charAt(i) == ',' && i == stringBuilder.length() - 3) {
				stringBuilder.setCharAt(i, '.');
			}
		}
		String number = stringBuilder.toString().replaceAll("\\s","");
		return Double.parseDouble(number);
	}
	
	/**
	 * Parses rating from table row
	 * @param value
	 * @param isLast
	 * @return
	 */
	private int getRatingFromName(String value, Boolean isLast) {
		int numberStartPosition = 0;
		for (int i = 0; i < value.length(); i++){
		    if (value.charAt(i) == '(' && Character.isDigit(value.charAt(i + 1))) {
		    	numberStartPosition = i + 1;
		    	break;
		    }
		}
		if (numberStartPosition == 0) {
			if (isLast) {
				//last
				return 0;
			} else {
				//private
				return -1;
			}
		}
		return Integer.parseInt(value.substring(numberStartPosition, value.length() - 1));
	}
	
}
