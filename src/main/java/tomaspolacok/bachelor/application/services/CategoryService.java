package tomaspolacok.bachelor.application.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ebay.sdk.SiteIDUtil;
import com.ebay.sdk.call.GetCategoriesCall;
import com.ebay.sdk.call.GetCategoryMappingsCall;
import com.ebay.soap.eBLBaseComponents.CategoryMappingType;
import com.ebay.soap.eBLBaseComponents.CategoryType;
import com.ebay.soap.eBLBaseComponents.DetailLevelCodeType;
import com.ebay.soap.eBLBaseComponents.GetCategoriesResponseType;

import tomaspolacok.bachelor.application.entities.Category;
import tomaspolacok.bachelor.application.entities.CategoryId;
import tomaspolacok.bachelor.application.entities.EbayCountry;
import tomaspolacok.bachelor.application.repositories.CategoryRepository;
import tomaspolacok.bachelor.application.repositories.EbayCountryRepository;
import tomaspolacok.bachelor.application.repositories.SearchRepository;

@Service
public class CategoryService {
	
	@Autowired
	CategoryRepository categoryRepository;
	@Autowired
	EbayCountryRepository ebayCountryRepository;
	@Autowired
	SearchRepository searchRepository;
	@Autowired
	EbayService ebayService;
	
	/**
	 * Returns all categories for specific ebay site
	 * @param country
	 * @return
	 */
	public List<Category> getCategoriesByCountry(EbayCountry country) {
		return categoryRepository.findByEbayCountry(country.getSiteId());
	}
	
	/**
	 * Checks for category version of specific ebay site, returns true if category has been updated or false if it's up to date
	 * @param ebayCountry
	 * @return
	 */
	public boolean checkCategoryVersion(EbayCountry ebayCountry) throws Exception {
		GetCategoriesCall categoryCall =  buildCategoryCall(false, ebayCountry);
		GetCategoriesResponseType categoryResponse = ebayService.sendCategoryRequest(categoryCall);
		if (categoryResponse.getCategoryVersion() != ebayCountry.getCategoryVersion()) {
			categoryCall =  buildCategoryCall(true, ebayCountry);
			categoryResponse = ebayService.sendCategoryRequest(categoryCall);
			GetCategoryMappingsCall categoryMappingCall = buildCategoryMappingCall(ebayCountry);
			CategoryMappingType[] categoryMappings = ebayService.sendCategoryMappingRequest(categoryMappingCall);
			updateCategoryHierarchy(ebayCountry, categoryResponse, categoryMappings);
			return true;
		}
		return false;
	}
	
	/**
	 * Method to update category hierarchy if it needs to be updated (first checks for category version, if it differs it tries to fetch category hierarchy and mappings)
	 * @param ebayCountry
	 * @param categoryResponse
	 * @param categoryMappings
	 */
	private void updateCategoryHierarchy(EbayCountry ebayCountry, GetCategoriesResponseType categoryResponse, CategoryMappingType[] categoryMappings) {
		categoryRepository.updateCategoriesByCountrySetActive(false, ebayCountry);
		
		//Create Categories collection from response
		List<Category> categoriesUpdated = new ArrayList<>();
		//map<child category, parent category>
		Map<String, String> categoryIdToParentId = new HashMap<>();
		//Gets list of new level 1 categories, + map of level 1,2 categories with their parents
		for (CategoryType category : categoryResponse.getCategoryArray().getCategory()) {
			categoryIdToParentId.put(category.getCategoryID(), category.getCategoryParentID()[0]);
			if (category.getCategoryLevel() == 1) {
				Category cat = new Category();
				CategoryId id = new CategoryId();
				id.setEbayCountry(ebayCountry);
				id.setId(category.getCategoryID());
				cat.setId(id);
				cat.setName(category.getCategoryName());
				cat.setActive(true);
				categoriesUpdated.add(cat);
			}
		}
		Map<String, List<String>> categoryMap = new HashMap<>();
		//Gets map<old category, new categories>
		for (CategoryMappingType cmt : categoryMappings) {
			if (categoryMap.containsKey(cmt.getOldID())) {
				List<String> value = categoryMap.get(cmt.getOldID());
				value.add(cmt.getId());
				categoryMap.put(cmt.getOldID(), value);
			} else {
				List<String> value = new ArrayList<>();
				value.add(cmt.getId());
				categoryMap.put(cmt.getOldID(), value);
			}
		}
		categoryRepository.saveAll(categoriesUpdated);
		updateSearchCategories(ebayCountry, categoryIdToParentId, categoryMap);
		categoryRepository.deleteCategoriesWhereActive(false);
		
		ebayCountry.setCategoryVersion(categoryResponse.getCategoryVersion());
	}
	
	/**
	 * Updates categories in searches by provided mapping
	 * @param ebayCountry
	 * @param categoryIdToParentId
	 * @param categoryMap
	 */
	private void updateSearchCategories(EbayCountry ebayCountry, Map<String, String> categoryIdToParentId, Map<String, List<String>> categoryMap) {
		for (Map.Entry<String, List<String>> entry : categoryMap.entrySet()){
			//pick level 1 category out of all level 1 categories of mappedTo categories
			String idNew = entry.getValue().get(0);
			
			idNew = categoryIdToParentId.get(idNew);
			if (idNew != null) {
				categoryRepository.updateCategoriesWhereCountry(entry.getKey(), idNew, ebayCountry.getSiteId());
				categoryRepository.deleteCategoriesObsolote(entry.getKey(), ebayCountry.getSiteId());
			}
		}
	}
	
	/**
	 * Builds category request call which is then sent to eBay api
	 * @param requestFull
	 * @param ebayCountry
	 * @return
	 */
	private GetCategoriesCall buildCategoryCall(Boolean requestFull, EbayCountry ebayCountry) {
		GetCategoriesCall apiCall = new GetCategoriesCall();
		apiCall.setCategorySiteID(SiteIDUtil.fromNumericalID(ebayCountry.getSiteId()));
		if (requestFull) {
			DetailLevelCodeType[] ar = new DetailLevelCodeType[1];
			ar[0] = DetailLevelCodeType.RETURN_ALL;
			apiCall.setDetailLevel(ar);
			apiCall.setLevelLimit(2);
		}
		return apiCall;
	}
	
	/**
	 * Builds category mappings request call which is then sent to eBay API
	 * @param ebayCountry
	 * @return
	 */
	private GetCategoryMappingsCall buildCategoryMappingCall(EbayCountry ebayCountry) {
		GetCategoryMappingsCall apiCall = new GetCategoryMappingsCall();
		apiCall.setCategoryVersion(ebayCountry.getCategoryVersion());
		apiCall.setSite(SiteIDUtil.fromNumericalID(ebayCountry.getSiteId()));
		DetailLevelCodeType[] ar = new DetailLevelCodeType[1];
		ar[0] = DetailLevelCodeType.RETURN_ALL;
		apiCall.setDetailLevel(ar);
		return apiCall;
	}
	
}
