package tomaspolacok.bachelor.application.controllers;

import java.util.Arrays;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import tomaspolacok.bachelor.application.entities.EbayItem;
import tomaspolacok.bachelor.application.entities.PictureCategory;
import tomaspolacok.bachelor.application.entities.User;
import tomaspolacok.bachelor.application.repositories.PictureCategoryRepository;
import tomaspolacok.bachelor.application.services.ArchiveService;
import tomaspolacok.bachelor.application.services.ItemService;
import tomaspolacok.bachelor.application.services.UserService;

@Controller
public class ArchiveController {
	
	@Autowired
	PictureCategoryRepository pictureCategoryRepository;
	@Autowired
	ArchiveService archiveService;
	@Autowired
	UserService userService;
	@Autowired
	ItemService itemService;

	/**
	 * Returns page with archived pictures for user
	 * @param size
	 * @param page
	 * @param all
	 * @param allCategories
	 * @param categoryId
	 * @return
	 */
	@GetMapping(value={"/archive"})
	public ModelAndView archived( @RequestParam(value = "size", required=false) Integer size,
								  @RequestParam(value = "page", defaultValue="0", required=false) Integer page,
								  @RequestParam(value = "all", required=false) Boolean all,
								  @RequestParam(value = "allCategories", required=false) Boolean allCategories,
								  @RequestParam(value = "category", required=false) Long categoryId){
		ModelAndView modelAndView = new ModelAndView();
		PictureCategory category = null;
		if (categoryId != null) {
			try {
				category = pictureCategoryRepository.getOne(categoryId);
			} catch (EntityNotFoundException e ) {
				modelAndView.setViewName("redirect:/error");
		        return modelAndView;
			}
			userService.updateArchiveCategory(category);
		}
		if (allCategories != null && allCategories) {
			userService.updateArchiveCategory(null);
		}
		User user = userService.getLoggedUser();
		userService.updateDisplayCount(user.getUserPreferences().getArchiveCount(), size, all);
		modelAndView.addObject("page", archiveService.getPictures(page));
		modelAndView.addObject("categories", archiveService.getUserCategories());
		modelAndView.addObject("displayCount", user.getUserPreferences().getArchiveCount());
		modelAndView.addObject("selectedCategory", user.getUserPreferences().getPictureCategory());
        modelAndView.addObject("sizes", Arrays.asList(15,30,45));
        modelAndView.setViewName("archive");
        return modelAndView;
    }
	
	/**
	 * Deletes picture from user archive
	 * @param pictureGoogleId
	 * @return
	 */
	@PostMapping(value= {"/archive/delete/all/{pictureGoogleId}"})
	public ModelAndView deletePicture(@PathVariable String pictureGoogleId){
		ModelAndView modelAndView = new ModelAndView();
		archiveService.archiveDelete(pictureGoogleId);	
		modelAndView.setViewName("redirect:/archive/");
		return modelAndView;
	}
	
	/**
	 * Deletes picture from user's category
	 * @param categoryId
	 * @param pictureGoogleId
	 * @return
	 */
	@PostMapping(value= {"/archive/delete/{categoryId}/{pictureGoogleId}"})
	public ModelAndView deletePictureFromCategory(@PathVariable Long categoryId, @PathVariable String pictureGoogleId){
		ModelAndView modelAndView = new ModelAndView();
		archiveService.archiveDeleteByCategory(categoryId, pictureGoogleId);	
		modelAndView.setViewName("redirect:/archive");
		return modelAndView;
	}
	
	/**
	 * Adds new category for archiving pictures
	 * @param categoryName
	 * @return
	 */
	@PostMapping(value= {"/category/add"})
	public @ResponseBody String addCategory(@RequestParam("categoryName") String categoryName) {
		PictureCategory cat = archiveService.saveCategory(categoryName);
		return cat.getId().toString();
	}
	
	/**
	 * Archives pictures of some item to specific category
	 * @param pictureIDs
	 * @param itemId
	 * @param categoryId
	 * @return
	 */
	@PostMapping(value= {"/archive"})
	public @ResponseBody String addPicture(@RequestParam(value="pictures[]") String[] pictureIDs,
									 @RequestParam(value="itemId") Long itemId,
									 @RequestParam(value="categoryId") Long categoryId ) {
		try {
			EbayItem item = itemService.getItem(itemId);
			PictureCategory category = pictureCategoryRepository
					.findById(categoryId)
					.orElse(null);
			if (item == null || category == null) throw new Exception();
			for (String pictureUrl : pictureIDs) {
				archiveService.addPicture(item, pictureUrl, category);
			}
		} catch (Exception e) {
			return "error";
		}
		return "success";
	}

	/**
	 * Retrieves image by its id
	 * @param imageId
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/images/{imageId}", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
	@Cacheable("pictureResponse")
	public byte[] getImageResource(@PathVariable String imageId) throws Exception {
		return archiveService.getPicture(imageId);
	}

}
